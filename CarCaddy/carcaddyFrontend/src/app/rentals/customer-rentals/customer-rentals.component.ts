import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { BookingService } from '../../services/booking.service';
import { CustomerService } from '../../services/customer.service';
import { CarService } from '../../services/car.service';
import { Booking, Car } from '../../models/models';

// ── Custom Validators ──────────────────────────────────────────────────────

/** Start date must be today or in the future */
export function presentOrFutureDate(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const selected = new Date(control.value);
    return selected < today ? { pastDate: true } : null;
  };
}

/** End date must be after start date (cross-field validator on the group) */
export function endAfterStart(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const start = group.get('startDate')?.value;
    const end   = group.get('endDate')?.value;
    if (!start || !end) return null;
    return new Date(end) <= new Date(start) ? { endBeforeStart: true } : null;
  };
}

@Component({
  selector: 'app-customer-rentals',
  templateUrl: './customer-rentals.component.html',
  styleUrls: ['./customer-rentals.component.css']
})
export class CustomerRentalsComponent implements OnInit {
  bookings: Booking[] = [];
  availableCars: Car[] = [];
  customerId = '';
  loyaltyDiscount = 0;
  loading = true;
  error = '';
  success = '';
  activeTab = 'active';
  estimatedFare = 0;
  submitting = false;
  selectedBooking: Booking | null = null;

  // Availability check state
  availabilityMsg = '';
  availabilityOk: boolean | null = null;
  checkingAvailability = false;

  // Modify availability check state
  modifyAvailabilityMsg = '';
  modifyAvailabilityOk: boolean | null = null;
  checkingModifyAvailability = false;

  showCreateModal = false;
  showModifyModal = false;

  bookingForm: FormGroup;
  modifyForm: FormGroup;

  categories = ['Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];

  today = new Date().toISOString().split('T')[0]; // for min date on inputs

  constructor(
    public authService: AuthService,
    private bookingService: BookingService,
    private customerService: CustomerService,
    private carService: CarService,
    private fb: FormBuilder
  ) {
    this.bookingForm = this.fb.group({
      model:          ['', Validators.required],
      category:       ['Sedan', Validators.required],
      startDate:      ['', [Validators.required, presentOrFutureDate()]],
      endDate:        ['', Validators.required],
      passengerCount: [1, [Validators.required, Validators.min(1), Validators.max(10)]]
    }, { validators: endAfterStart() });

    this.modifyForm = this.fb.group({
      startDate: ['', presentOrFutureDate()],
      endDate:   [''],
      model:     ['']
    }, { validators: endAfterStart() });
  }

  ngOnInit(): void {
    this.carService.getAvailableCars().subscribe({ next: d => this.availableCars = d, error: () => {} });
    const username = this.authService.getUsername();
    this.customerService.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customerId = customer.customerId!;
        this.loadBookings();
        this.customerService.getLoyaltyDiscount(this.customerId).subscribe({
          next: (d: any) => { this.loyaltyDiscount = d.discountPercent || 0; },
          error: () => {}
        });
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });

    // Recalculate fare + check availability whenever dates or model change
    this.bookingForm.valueChanges.subscribe(() => {
      this.calculateFare();
      this.availabilityMsg = '';
      this.availabilityOk = null;
    });
  }

  loadBookings(): void {
    if (!this.customerId) return;
    this.bookingService.getBookingsByCustomer(this.customerId).subscribe({
      next: d => this.bookings = d, error: () => {}
    });
  }

  calculateFare(): void {
    const { startDate, endDate, model } = this.bookingForm.value;
    if (!startDate || !endDate || !model) { this.estimatedFare = 0; return; }
    if (new Date(endDate) <= new Date(startDate)) { this.estimatedFare = 0; return; }

    const days = Math.max(1, Math.ceil(
      (new Date(endDate).getTime() - new Date(startDate).getTime()) / 86400000
    ));

    // Fetch actual rate from the car with this model
    this.carService.getCarsByModel(model).subscribe({
      next: (cars) => {
        if (cars.length === 0) { this.estimatedFare = 0; return; }
        // Use the first car's rate (all cars of same model should have same rate)
        const rate = cars[0].rentalRatePerDay || 0;
        if (rate === 0) { this.estimatedFare = 0; return; }
        const base = days * rate;
        this.estimatedFare = base - (base * this.loyaltyDiscount / 100);
      },
      error: () => { this.estimatedFare = 0; }
    });
  }

  /** Called when user clicks "Check Availability" or on date/model blur */
  checkAvailability(): void {
    const { model, startDate, endDate } = this.bookingForm.value;
    if (!model || !startDate || !endDate) return;
    if (this.bookingForm.hasError('endBeforeStart')) return;

    this.checkingAvailability = true;
    this.availabilityMsg = '';
    this.availabilityOk = null;

    // Get all cars of this model, then check availability for each
    this.carService.getCarsByModel(model).subscribe({
      next: (cars) => {
        if (cars.length === 0) {
          this.availabilityMsg = `No cars found with model "${model}". Please try a different model.`;
          this.availabilityOk = false;
          this.checkingAvailability = false;
          return;
        }

        // Check availability for each car using the backend endpoint
        const availableCars = cars.filter(c => c.status === 'AVAILABLE');
        if (availableCars.length === 0) {
          this.availabilityMsg = `All "${model}" cars are currently rented or in maintenance. Please try a different model.`;
          this.availabilityOk = false;
          this.checkingAvailability = false;
          return;
        }

        // Check date-based availability for at least one car
        let checked = 0;
        let foundAvailable = false;

        for (const car of availableCars) {
          this.bookingService.checkAvailability(car.registrationNumber, startDate, endDate).subscribe({
            next: (available) => {
              checked++;
              if (available) foundAvailable = true;
              if (checked === availableCars.length) {
                this.checkingAvailability = false;
                if (foundAvailable) {
                  this.availabilityMsg = `✓ Cars available for "${model}" on selected dates!`;
                  this.availabilityOk = true;
                } else {
                  this.availabilityMsg = `No "${model}" cars available for ${startDate} to ${endDate}. Please choose different dates or model.`;
                  this.availabilityOk = false;
                }
              }
            },
            error: () => {
              checked++;
              if (checked === availableCars.length) {
                this.checkingAvailability = false;
                this.availabilityMsg = 'Could not check availability. Please try again.';
                this.availabilityOk = null;
              }
            }
          });
        }
      },
      error: () => {
        this.checkingAvailability = false;
        this.availabilityMsg = 'Could not check availability. Please try again.';
        this.availabilityOk = null;
      }
    });
  }

  openCreateModal(): void {
    this.bookingForm.reset({ category: 'Sedan', passengerCount: 1 });
    this.estimatedFare = 0;
    this.availabilityMsg = '';
    this.availabilityOk = null;
    this.showCreateModal = true;
    this.error = '';
    this.success = '';
  }

  openModifyModal(b: Booking): void {
    this.selectedBooking = b;
    this.modifyAvailabilityMsg = '';
    this.modifyAvailabilityOk = null;
    this.modifyForm.patchValue({ startDate: b.startDate, endDate: b.endDate });
    this.showModifyModal = true;
  }

  closeModals(): void {
    this.showCreateModal = false;
    this.showModifyModal = false;
    this.selectedBooking = null;
    this.submitting = false;
    this.availabilityMsg = '';
    this.availabilityOk = null;
    this.modifyAvailabilityMsg = '';
    this.modifyAvailabilityOk = null;
  }

  /** Check availability for the modify form using the existing booking's car */
  checkModifyAvailability(): void {
    const { startDate, endDate } = this.modifyForm.value;
    if (!startDate || !endDate || !this.selectedBooking) return;
    if (this.modifyForm.hasError('endBeforeStart')) return;

    this.checkingModifyAvailability = true;
    this.modifyAvailabilityMsg = '';
    this.modifyAvailabilityOk = null;

    const regNo = this.selectedBooking.car?.registrationNumber;
    if (!regNo) { this.checkingModifyAvailability = false; return; }

    // Use the existing booking's car registration to check availability
    // The backend excludes the current booking from overlap check
    this.bookingService.checkAvailability(regNo, startDate, endDate).subscribe({
      next: (available) => {
        this.checkingModifyAvailability = false;
        if (available) {
          this.modifyAvailabilityMsg = `✓ Car available for the new dates!`;
          this.modifyAvailabilityOk = true;
        } else {
          this.modifyAvailabilityMsg = `Car is not available for ${startDate} to ${endDate}. Please choose different dates.`;
          this.modifyAvailabilityOk = false;
        }
      },
      error: () => {
        this.checkingModifyAvailability = false;
        this.modifyAvailabilityMsg = 'Could not check availability. Please try again.';
        this.modifyAvailabilityOk = null;
      }
    });
  }

  createBooking(): void {
    if (this.bookingForm.invalid || !this.customerId) {
      this.bookingForm.markAllAsTouched();
      return;
    }
    this.submitting = true;
    const req = { ...this.bookingForm.value, customerId: this.customerId };
    this.bookingService.createBooking(req).subscribe({
      next: (b: any) => {
        this.success = `Booking created! Car: ${b.car?.model || 'allocated'} (${b.car?.registrationNumber}). Fare: ₹${b.totalFare?.toFixed(0)}`;
        this.closeModals();
        this.loadBookings();
      },
      error: (err: any) => {
        this.submitting = false;
        const msg = AuthService.parseError(err);
        // Make backend error messages user-friendly
        if (msg.toLowerCase().includes('no available cars') || msg.toLowerCase().includes('no cars')) {
          this.error = `No "${this.bookingForm.value.model}" cars are available for the selected dates. Please try a different model or dates.`;
        } else if (msg.toLowerCase().includes('blacklisted')) {
          this.error = 'Your account has been restricted. Please contact support.';
        } else {
          this.error = msg;
        }
      }
    });
  }

  modifyBooking(): void {
    if (!this.selectedBooking) return;
    if (this.modifyForm.hasError('endBeforeStart')) {
      this.error = 'End date must be after start date.';
      return;
    }
    this.submitting = true;
    const v = this.modifyForm.value;
    const req: any = {};
    if (v.startDate) req.startDate = v.startDate;
    if (v.endDate)   req.endDate   = v.endDate;
    if (v.model)     req.model     = v.model;
    this.bookingService.modifyBooking(this.selectedBooking.bookingId!, req).subscribe({
      next: () => { this.success = 'Booking modified!'; this.closeModals(); this.loadBookings(); },
      error: (err: any) => {
        this.submitting = false;
        const msg = AuthService.parseError(err);
        if (msg.toLowerCase().includes('dates not available') || msg.toLowerCase().includes('no cars')) {
          this.error = 'Selected dates are not available. Please choose different dates.';
        } else {
          this.error = msg;
        }
      }
    });
  }

  cancelBooking(b: Booking): void {
    if (!confirm('Cancel this booking?')) return;
    this.bookingService.cancelBooking(b.bookingId!).subscribe({
      next: () => { this.success = 'Booking cancelled.'; this.loadBookings(); },
      error: (err: any) => this.error = AuthService.parseError(err)
    });
  }

  get activeBookings() {
    return this.bookings.filter(b =>
      b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'ACTIVE' || b.bookingStatus === 'MODIFIED'
    );
  }
  get historyBookings() {
    return this.bookings.filter(b =>
      b.bookingStatus === 'COMPLETED' || b.bookingStatus === 'CANCELLED'
    );
  }
  canModify(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED'; }
  canCancel(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'MODIFIED'; }

  get bf() { return this.bookingForm.controls; }
  get mf() { return this.modifyForm.controls; }
}
