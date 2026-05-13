import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { CustomerService } from '../../services/customer.service';
import { CarService } from '../../services/car.service';
import { AuthService } from '../../services/auth.service';
import { Booking, Customer, Car } from '../../models/models';

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
    private fb: FormBuilder
  ) {
    this.bookingForm = this.fb.group({
      customerId: ['', Validators.required],
      model: ['', Validators.required],
      category: ['Sedan', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      passengerCount: [1, [Validators.required, Validators.min(1), Validators.max(10)]]
    });
    this.modifyForm = this.fb.group({
      startDate: [''],
      endDate: [''],
      model: [''],
      category: ['']
    });
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
    if (startDate && endDate && model) {
      const days = Math.max(1, Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / 86400000));
      const car = this.availableCars.find(c => c.model.toLowerCase().includes(model.toLowerCase()));
      const rate = car?.rentalRatePerDay || 75;
      const base = days * rate;
      this.estimatedFare = base - (base * this.customerDiscount / 100);
    }
  }

  openCreateModal(): void {
    this.bookingForm.reset({ category: 'Sedan', passengerCount: 1 });
    this.estimatedFare = 0; this.customerDiscount = 0;
    this.showCreateModal = true; this.error = ''; this.success = '';
  }
  openModifyModal(b: Booking): void {
    this.selectedBooking = b;
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
        this.success = `Booking created! Car: ${b.car?.registrationNumber || 'allocated'}. Fare: ₹${b.totalFare?.toFixed(0)} (${b.discount || 0}% discount)`;
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
      next: () => { this.success = 'Booking modified!'; this.closeModals(); this.loadBookings(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
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

  cancelBooking(b: Booking): void {
    if (!confirm(`Cancel booking #${b.bookingId}?`)) return;
    this.bookingService.cancelBooking(b.bookingId!).subscribe({
      next: () => { this.success = 'Booking cancelled.'; this.loadBookings(); },
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
