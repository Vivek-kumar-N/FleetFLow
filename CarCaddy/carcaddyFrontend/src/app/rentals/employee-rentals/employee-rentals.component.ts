import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { CustomerService } from '../../services/customer.service';
import { CarService } from '../../services/car.service';
import { AuthService } from '../../services/auth.service';
import { Booking, Customer, Car } from '../../models/models';
import { presentOrFutureDate, endAfterStart } from '../customer-rentals/customer-rentals.component';

@Component({
  selector: 'app-employee-rentals',
  templateUrl: './employee-rentals.component.html',
  styleUrls: ['./employee-rentals.component.css']
})
export class EmployeeRentalsComponent implements OnInit {
  bookings: Booking[] = [];
  filteredBookings: Booking[] = [];
  customers: Customer[] = [];
  availableCars: Car[] = [];
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  selectedBooking: Booking | null = null;
  submitting = false;
  estimatedFare = 0;
  customerDiscount = 0;

  // Availability check — create form
  availabilityMsg = '';
  availabilityOk: boolean | null = null;
  checkingAvailability = false;

  // Availability check — modify form
  modifyAvailabilityMsg = '';
  modifyAvailabilityOk: boolean | null = null;
  checkingModifyAvailability = false;

  today = new Date().toISOString().split('T')[0];

  showCreateModal = false;
  showReturnModal = false;
  showModifyModal = false;

  bookingForm: FormGroup;
  returnForm: FormGroup;
  modifyForm: FormGroup;

  categories = ['Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];

  constructor(
    private bookingService: BookingService,
    private customerService: CustomerService,
    private carService: CarService,
    private fb: FormBuilder
  ) {
    this.bookingForm = this.fb.group({
      customerId:     ['', Validators.required],
      model:          ['', Validators.required],
      category:       ['Sedan', Validators.required],
      startDate:      ['', [Validators.required, presentOrFutureDate()]],
      endDate:        ['', Validators.required],
      passengerCount: [1, [Validators.required, Validators.min(1), Validators.max(10)]]
    }, { validators: endAfterStart() });

    this.returnForm = this.fb.group({
      mileageAtReturn: [0, [Validators.required, Validators.min(0)]],
      damaged:         [false],
      damageNotes:     ['']
    });

    this.modifyForm = this.fb.group({
      startDate: ['', presentOrFutureDate()],
      endDate:   [''],
      model:     [''],
      category:  ['']
    }, { validators: endAfterStart() });
  }

  ngOnInit(): void {
    this.loadBookings();
    this.customerService.getAllCustomers().subscribe({
      next: d => this.customers = d.filter(c => !c.blacklisted), error: () => {}
    });
    this.carService.getAvailableCars().subscribe({
      next: d => this.availableCars = d, error: () => {}
    });
    this.bookingForm.valueChanges.subscribe(() => {
      this.calculateFare();
      this.availabilityMsg = '';
      this.availabilityOk = null;
    });
  }

  loadBookings(): void {
    this.loading = true;
    this.bookingService.getAllBookings().subscribe({
      next: d => { this.bookings = d; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load bookings.'; }
    });
  }

  loadAll(): void { this.statusFilter = ''; this.loadBookings(); }

  applyFilters(): void {
    this.filteredBookings = this.bookings.filter(b =>
      !this.statusFilter || b.bookingStatus === this.statusFilter
    );
  }

  onCustomerChange(): void {
    const cid = this.bookingForm.value.customerId;
    if (cid) {
      this.customerService.getLoyaltyDiscount(cid).subscribe({
        next: (d: any) => { this.customerDiscount = d.discountPercent || 0; this.calculateFare(); },
        error: () => {}
      });
    }
  }

  calculateFare(): void {
    const { startDate, endDate, model } = this.bookingForm.value;
    if (!startDate || !endDate || !model) { this.estimatedFare = 0; return; }
    if (new Date(endDate) <= new Date(startDate)) { this.estimatedFare = 0; return; }

    const days = Math.max(1, Math.ceil(
      (new Date(endDate).getTime() - new Date(startDate).getTime()) / 86400000
    ));

    this.carService.getCarsByModel(model).subscribe({
      next: (cars) => {
        if (cars.length === 0) { this.estimatedFare = 0; return; }
        const rate = cars[0].rentalRatePerDay || 0;
        if (rate === 0) { this.estimatedFare = 0; return; }
        const base = days * rate;
        this.estimatedFare = base - (base * this.customerDiscount / 100);
      },
      error: () => { this.estimatedFare = 0; }
    });
  }

  checkAvailability(): void {
    const { model, startDate, endDate } = this.bookingForm.value;
    if (!model || !startDate || !endDate) return;
    if (this.bookingForm.hasError('endBeforeStart')) return;

    this.checkingAvailability = true;
    this.availabilityMsg = '';
    this.availabilityOk = null;

    this.carService.getCarsByModel(model).subscribe({
      next: (cars) => {
        if (cars.length === 0) {
          this.availabilityMsg = `No cars found with model "${model}". Please try a different model.`;
          this.availabilityOk = false;
          this.checkingAvailability = false;
          return;
        }
        const availableCars = cars.filter(c => c.status === 'AVAILABLE');
        if (availableCars.length === 0) {
          this.availabilityMsg = `All "${model}" cars are currently rented or in maintenance.`;
          this.availabilityOk = false;
          this.checkingAvailability = false;
          return;
        }
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
                  this.availabilityMsg = `No "${model}" cars available for ${startDate} to ${endDate}. Try different dates or model.`;
                  this.availabilityOk = false;
                }
              }
            },
            error: () => {
              checked++;
              if (checked === availableCars.length) {
                this.checkingAvailability = false;
                this.availabilityMsg = 'Could not check availability. Please try again.';
              }
            }
          });
        }
      },
      error: () => {
        this.checkingAvailability = false;
        this.availabilityMsg = 'Could not check availability. Please try again.';
      }
    });
  }

  checkModifyAvailability(): void {
    const { startDate, endDate } = this.modifyForm.value;
    if (!startDate || !endDate || !this.selectedBooking) return;
    if (this.modifyForm.hasError('endBeforeStart')) return;

    this.checkingModifyAvailability = true;
    this.modifyAvailabilityMsg = '';
    this.modifyAvailabilityOk = null;

    const regNo = this.selectedBooking.car?.registrationNumber;
    if (!regNo) { this.checkingModifyAvailability = false; return; }

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

  openCreateModal(): void {
    this.bookingForm.reset({ category: 'Sedan', passengerCount: 1 });
    this.estimatedFare = 0;
    this.customerDiscount = 0;
    this.availabilityMsg = '';
    this.availabilityOk = null;
    this.showCreateModal = true;
    this.error = '';
    this.success = '';
  }

  openReturnModal(b: Booking): void {
    this.selectedBooking = b;
    this.returnForm.reset({ mileageAtReturn: b.car?.mileage || 0, damaged: false });
    this.showReturnModal = true;
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
    this.showReturnModal = false;
    this.showModifyModal = false;
    this.selectedBooking = null;
    this.submitting = false;
    this.availabilityMsg = '';
    this.availabilityOk = null;
    this.modifyAvailabilityMsg = '';
    this.modifyAvailabilityOk = null;
  }

  createBooking(): void {
    if (this.bookingForm.invalid) { this.bookingForm.markAllAsTouched(); return; }
    this.submitting = true;
    this.bookingService.createBooking(this.bookingForm.value).subscribe({
      next: (b: any) => {
        this.success = `Booking created! Car: ${b.car?.model || ''} (${b.car?.registrationNumber || 'allocated'}). Fare: ₹${b.totalFare?.toFixed(0)}${b.discount ? ' (' + b.discount + '% discount)' : ''}`;
        this.closeModals();
        this.loadBookings();
      },
      error: (err: any) => {
        this.submitting = false;
        const msg = AuthService.parseError(err);
        if (msg.toLowerCase().includes('no available cars') || msg.toLowerCase().includes('no cars')) {
          this.error = `No "${this.bookingForm.value.model}" cars are available for the selected dates. Please try a different model or dates.`;
        } else {
          this.error = msg;
        }
      }
    });
  }

  returnCar(): void {
    if (!this.selectedBooking) return;
    this.submitting = true;
    this.bookingService.returnCar(this.selectedBooking.bookingId!, this.returnForm.value).subscribe({
      next: () => { this.success = 'Car returned successfully!'; this.closeModals(); this.loadBookings(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
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
    if (v.category)  req.category  = v.category;
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
    if (!confirm(`Cancel booking #${b.bookingId}?`)) return;
    this.bookingService.cancelBooking(b.bookingId!).subscribe({
      next: () => { this.success = 'Booking cancelled.'; this.loadBookings(); },
      error: (err: any) => this.error = AuthService.parseError(err)
    });
  }

  canReturn(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'ACTIVE'; }
  canCancel(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'MODIFIED'; }
  canModify(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED'; }

  get f() { return this.bookingForm.controls; }
  get mf() { return this.modifyForm.controls; }
  get rf() { return this.returnForm.controls; }
}
