import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { CustomerService } from '../../services/customer.service';
import { CarService } from '../../services/car.service';
import { AuthService } from '../../services/auth.service';
import { MaintenanceService } from '../../services/maintenance.service';
import { Booking, Customer, Car } from '../../models/models';
import { presentOrFutureDate, endAfterStart } from '../customer-rentals/customer-rentals.component';

@Component({
  selector: 'app-admin-rentals',
  templateUrl: './admin-rentals.component.html',
  styleUrls: ['./admin-rentals.component.css']
})
export class AdminRentalsComponent implements OnInit {
  bookings: Booking[] = [];
  filteredBookings: Booking[] = [];
  customers: Customer[] = [];
  availableCars: Car[] = [];
  selectedBooking: Booking | null = null;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  searchTerm = '';
  submitting = false;
  estimatedFare = 0;
  customerDiscount = 0;

  availabilityMsg = '';
  availabilityOk: boolean | null = null;
  checkingAvailability = false;
  today = new Date().toISOString().split('T')[0];

  modifyAvailabilityMsg = '';
  modifyAvailabilityOk: boolean | null = null;
  checkingModifyAvailability = false;

  showDetailModal = false;
  showCreateModal = false;
  showModifyModal = false;
  showReturnModal = false;

  statuses = ['CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'MODIFIED'];
  categories = ['Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];

  bookingForm: FormGroup;
  modifyForm: FormGroup;
  returnForm: FormGroup;

  constructor(
    private bookingService: BookingService,
    private customerService: CustomerService,
    private carService: CarService,
    private maintenanceService: MaintenanceService,
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
    this.modifyForm = this.fb.group({
      startDate: ['', presentOrFutureDate()],
      endDate:   [''],
      model:     [''],
      category:  ['']
    }, { validators: endAfterStart() });
    this.returnForm = this.fb.group({
      mileageAtReturn: [0, [Validators.required, Validators.min(0)]],
      damaged: [false],
      damageNotes: ['']
    });
  }

  ngOnInit(): void {
    this.loadBookings();
    this.customerService.getAllCustomers().subscribe({ next: d => this.customers = d.filter(c => !c.blacklisted), error: () => {} });
    this.carService.getAvailableCars().subscribe({ next: d => this.availableCars = d, error: () => {} });
    this.bookingForm.valueChanges.subscribe(() => this.calculateFare());
  }

  loadBookings(): void {
    this.loading = true;
    this.bookingService.getAllBookings().subscribe({
      next: d => { this.bookings = d; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load bookings.'; }
    });
  }

  applyFilters(): void {
    this.filteredBookings = this.bookings.filter(b => {
      const s = this.searchTerm.toLowerCase();
      const matchSearch = !s || String(b.bookingId).includes(s) || (b.customer?.customerName || '').toLowerCase().includes(s) || (b.car?.registrationNumber || '').toLowerCase().includes(s);
      const matchStatus = !this.statusFilter || b.bookingStatus === this.statusFilter;
      return matchSearch && matchStatus;
    });
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

    // Fetch actual rate from the car with this model (not from available cars list)
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
        const availableCars = cars.filter(c => c.status !== 'MAINTENANCE');
        if (availableCars.length === 0) {
          this.availabilityMsg = `All "${model}" cars are currently in maintenance.`;
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

  /** Check availability for the modify form — uses new model if provided, else existing car */
  checkModifyAvailability(): void {
    const { startDate, endDate, model } = this.modifyForm.value;
    if (!startDate || !endDate || !this.selectedBooking) return;
    if (this.modifyForm.hasError('endBeforeStart')) return;

    this.checkingModifyAvailability = true;
    this.modifyAvailabilityMsg = '';
    this.modifyAvailabilityOk = null;

    if (model && model.trim()) {
      // New model requested — check all non-maintenance cars of that model
      this.carService.getCarsByModel(model).subscribe({
        next: (cars) => {
          const carsToCheck = cars.filter(c => c.status !== 'MAINTENANCE');
          if (carsToCheck.length === 0) {
            this.checkingModifyAvailability = false;
            this.modifyAvailabilityMsg = `No "${model}" cars available (all in maintenance).`;
            this.modifyAvailabilityOk = false;
            return;
          }
          let checked = 0; let found = false;
          for (const car of carsToCheck) {
            this.bookingService.checkAvailability(car.registrationNumber, startDate, endDate).subscribe({
              next: (available) => {
                checked++;
                if (available) found = true;
                if (checked === carsToCheck.length) {
                  this.checkingModifyAvailability = false;
                  this.modifyAvailabilityMsg = found
                    ? `✓ "${model}" cars available for the new dates!`
                    : `No "${model}" cars available for ${startDate} to ${endDate}.`;
                  this.modifyAvailabilityOk = found;
                }
              },
              error: () => { checked++; if (checked === carsToCheck.length) { this.checkingModifyAvailability = false; } }
            });
          }
        },
        error: () => { this.checkingModifyAvailability = false; }
      });
    } else {
      // Same car — check existing car's availability (backend excludes current booking)
      const regNo = this.selectedBooking.car?.registrationNumber;
      if (!regNo) { this.checkingModifyAvailability = false; return; }
      this.bookingService.checkAvailability(regNo, startDate, endDate).subscribe({
        next: (available) => {
          this.checkingModifyAvailability = false;
          this.modifyAvailabilityMsg = available
            ? `✓ Car available for the new dates!`
            : `Car is not available for ${startDate} to ${endDate}. Please choose different dates.`;
          this.modifyAvailabilityOk = available;
        },
        error: () => { this.checkingModifyAvailability = false; this.modifyAvailabilityMsg = 'Could not check availability.'; }
      });
    }
  }

  openCreateModal(): void {
    this.bookingForm.reset({ category: 'Sedan', passengerCount: 1 });
    this.estimatedFare = 0; this.customerDiscount = 0;
    this.availabilityMsg = ''; this.availabilityOk = null;
    this.showCreateModal = true; this.error = ''; this.success = '';
  }
  openModifyModal(b: Booking): void {
    this.selectedBooking = b;
    this.modifyAvailabilityMsg = '';
    this.modifyAvailabilityOk = null;
    this.modifyForm.patchValue({ startDate: b.startDate, endDate: b.endDate });
    this.showModifyModal = true;
  }
  openReturnModal(b: Booking): void {
    this.selectedBooking = b;
    this.returnForm.reset({ mileageAtReturn: b.car?.mileage || 0, damaged: false });
    this.showReturnModal = true;
  }
  viewDetail(b: Booking): void { this.selectedBooking = b; this.showDetailModal = true; }
  closeModals(): void {
    this.showDetailModal = false; this.showCreateModal = false;
    this.showModifyModal = false; this.showReturnModal = false;
    this.selectedBooking = null; this.submitting = false;
  }

  createBooking(): void {
    if (this.bookingForm.invalid) { this.bookingForm.markAllAsTouched(); return; }
    this.submitting = true;
    this.bookingService.createBooking(this.bookingForm.value).subscribe({
      next: (b: any) => {
        this.success = `Booking created! Allocated Car: ${b.car?.model || ''} (${b.car?.registrationNumber || 'allocated'}). Fare: ₹${b.totalFare?.toFixed(0)} (${b.discount || 0}% discount)`;
        this.closeModals(); this.loadBookings();
      },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  modifyBooking(): void {
    if (!this.selectedBooking) return;
    this.submitting = true;
    const v = this.modifyForm.value;
    const req: any = {};
    if (v.startDate) req.startDate = v.startDate;
    if (v.endDate) req.endDate = v.endDate;
    if (v.model) req.model = v.model;
    if (v.category) req.category = v.category;
    this.bookingService.modifyBooking(this.selectedBooking.bookingId!, req).subscribe({
      next: (b: any) => {
        const carInfo = b?.car ? `${b.car.model} (${b.car.registrationNumber})` : 'car';
        this.success = `Booking #${this.selectedBooking?.bookingId} modified! Allocated: ${carInfo}. New fare: ₹${b?.totalFare?.toFixed(0) || ''}`;
        this.closeModals(); this.loadBookings();
      },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  returnCar(): void {
    if (!this.selectedBooking) return;
    this.submitting = true;
    const returnData = this.returnForm.value;
    const booking = this.selectedBooking;

    this.bookingService.returnCar(booking.bookingId!, returnData).subscribe({
      next: (b: any) => {
        // Backend already auto-creates maintenance record — no duplicate needed
        const carInfo = `${booking.car?.model || ''} (${booking.car?.registrationNumber || ''})`;
        this.success = `Car ${carInfo} returned successfully! Booking #${booking.bookingId} completed.`
          + (returnData.damaged ? ' Emergency maintenance record created automatically.' : '');
        this.closeModals();
        this.loadBookings();
      },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  cancelBooking(b: Booking): void {
    if (!confirm(`Cancel booking #${b.bookingId} for ${b.customer?.customerName || 'customer'}?`)) return;
    this.bookingService.cancelBooking(b.bookingId!).subscribe({
      next: () => {
        this.success = `Booking #${b.bookingId} cancelled successfully. Car ${b.car?.model || ''} (${b.car?.registrationNumber || ''}) is now available.`;
        this.loadBookings();
      },
      error: (err: any) => this.error = AuthService.parseError(err)
    });
  }

  loadActive(): void { this.bookingService.getActiveBookings().subscribe({ next: d => { this.bookings = d; this.applyFilters(); }, error: () => {} }); }
  loadCompleted(): void { this.bookingService.getCompletedBookings().subscribe({ next: d => { this.bookings = d; this.applyFilters(); }, error: () => {} }); }
  loadAll(): void { this.statusFilter = ''; this.loadBookings(); }

  canReturn(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'ACTIVE'; }
  canCancel(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'MODIFIED'; }
  canModify(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED'; }

  get f() { return this.bookingForm.controls; }
}
