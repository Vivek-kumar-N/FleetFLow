import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { BookingService } from '../../services/booking.service';
import { CustomerService } from '../../services/customer.service';
import { CarService } from '../../services/car.service';
import { Booking, Car } from '../../models/models';

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

  showCreateModal = false;
  showModifyModal = false;

  bookingForm: FormGroup;
  modifyForm: FormGroup;

  categories = ['Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];

  constructor(
    public authService: AuthService,
    private bookingService: BookingService,
    private customerService: CustomerService,
    private carService: CarService,
    private fb: FormBuilder
  ) {
    this.bookingForm = this.fb.group({
      model: ['', Validators.required],
      category: ['Sedan', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      passengerCount: [1, [Validators.required, Validators.min(1), Validators.max(10)]]
    });
    this.modifyForm = this.fb.group({
      startDate: [''],
      endDate: [''],
      model: ['']
    });
  }

  ngOnInit(): void {
    this.carService.getAvailableCars().subscribe({ next: d => this.availableCars = d, error: () => {} });
    const username = this.authService.getUsername();
    this.customerService.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customerId = customer.customerId!;
        this.loadBookings();
        this.customerService.getLoyaltyDiscount(this.customerId).subscribe({ next: (d: any) => { this.loyaltyDiscount = d.discountPercent || 0; }, error: () => {} });
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
    this.bookingForm.valueChanges.subscribe(() => this.calculateFare());
  }

  loadBookings(): void {
    if (!this.customerId) return;
    this.bookingService.getBookingsByCustomer(this.customerId).subscribe({ next: d => this.bookings = d, error: () => {} });
  }

  calculateFare(): void {
    const { startDate, endDate, model } = this.bookingForm.value;
    if (startDate && endDate && model) {
      const days = Math.max(1, Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / 86400000));
      const car = this.availableCars.find(c => c.model.toLowerCase().includes(model.toLowerCase()));
      const rate = car?.rentalRatePerDay || 75;
      const base = days * rate;
      this.estimatedFare = base - (base * this.loyaltyDiscount / 100);
    }
  }

  openCreateModal(): void { this.bookingForm.reset({ category: 'Sedan', passengerCount: 1 }); this.estimatedFare = 0; this.showCreateModal = true; this.error = ''; this.success = ''; }
  openModifyModal(b: Booking): void { this.selectedBooking = b; this.modifyForm.patchValue({ startDate: b.startDate, endDate: b.endDate }); this.showModifyModal = true; }
  closeModals(): void { this.showCreateModal = false; this.showModifyModal = false; this.selectedBooking = null; this.submitting = false; }

  createBooking(): void {
    if (this.bookingForm.invalid || !this.customerId) { this.bookingForm.markAllAsTouched(); return; }
    this.submitting = true;
    const req = { ...this.bookingForm.value, customerId: this.customerId };
    this.bookingService.createBooking(req).subscribe({
      next: (b: any) => { this.success = `Booking created! Car: ${b.car?.model || 'allocated'}. Fare: ₹${b.totalFare?.toFixed(0)}`; this.closeModals(); this.loadBookings(); },
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
    this.bookingService.modifyBooking(this.selectedBooking.bookingId!, req).subscribe({
      next: () => { this.success = 'Booking modified!'; this.closeModals(); this.loadBookings(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  cancelBooking(b: Booking): void {
    if (!confirm('Cancel this booking?')) return;
    this.bookingService.cancelBooking(b.bookingId!).subscribe({
      next: () => { this.success = 'Booking cancelled.'; this.loadBookings(); },
      error: (err: any) => this.error = AuthService.parseError(err)
    });
  }

  get activeBookings() { return this.bookings.filter(b => b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'ACTIVE' || b.bookingStatus === 'MODIFIED'); }
  get historyBookings() { return this.bookings.filter(b => b.bookingStatus === 'COMPLETED' || b.bookingStatus === 'CANCELLED'); }
  canModify(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED'; }
  canCancel(b: Booking): boolean { return b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'MODIFIED'; }
}
