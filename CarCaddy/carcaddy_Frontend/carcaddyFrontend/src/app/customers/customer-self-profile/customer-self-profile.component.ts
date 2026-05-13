import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { CustomerService } from '../../services/customer.service';
import { BookingService } from '../../services/booking.service';
import { Customer, Booking } from '../../models/models';

@Component({
  selector: 'app-customer-self-profile',
  templateUrl: './customer-self-profile.component.html',
  styleUrls: ['./customer-self-profile.component.css']
})
export class CustomerSelfProfileComponent implements OnInit {
  customer: Customer | null = null;
  bookings: Booking[] = [];
  loyaltyDiscount = 0;
  freeRentalEligible = false;
  loading = true;
  error = '';
  success = '';
  activeTab = 'profile';
  Math = Math;

  profileForm: FormGroup;
  submitting = false;

  constructor(
    public authService: AuthService,
    private customerService: CustomerService,
    private bookingService: BookingService,
    private fb: FormBuilder
  ) {
    this.profileForm = this.fb.group({
      customerName: ['', [Validators.required, Validators.minLength(2)]],
      contactNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      drivingLicense: ['', Validators.required],
      occupation: [''],
      address: ['', Validators.required],
      emailId: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    const username = this.authService.getUsername();
    this.customerService.getAllCustomers().subscribe({
      next: (customers) => {
        const found = customers.find(c => c.emailId === username || c.customerId === username);
        if (found) {
          this.customer = found;
          this.profileForm.patchValue(found);
          this.loadCustomerData(found.customerId!);
        }
        this.loading = false;
      },
      error: () => { this.loading = false; this.error = 'Failed to load profile.'; }
    });
  }

  loadCustomerData(id: string): void {
    this.bookingService.getBookingsByCustomer(id).subscribe({ next: d => this.bookings = d, error: () => {} });
    this.customerService.getLoyaltyDiscount(id).subscribe({ next: (d: any) => this.loyaltyDiscount = d.discountPercent || 0, error: () => {} });
    this.customerService.isEligibleForFreeRental(id).subscribe({ next: (d: any) => this.freeRentalEligible = d === true || d?.eligible === true, error: () => {} });
  }

  updateProfile(): void {
    if (!this.customer || this.profileForm.invalid) return;
    this.submitting = true;
    this.customerService.updateCustomer(this.customer.customerId!, this.profileForm.value).subscribe({
      next: () => { this.success = 'Profile updated!'; this.submitting = false; },
      error: (err: any) => { this.submitting = false; this.error = err.error || 'Failed to update.'; }
    });
  }

  get activeBookings() { return this.bookings.filter(b => b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'ACTIVE'); }
  get completedBookings() { return this.bookings.filter(b => b.bookingStatus === 'COMPLETED'); }

  get loyaltyTier(): string {
    const pts = this.customer?.loyaltyPoints || 0;
    if (pts >= 500) return 'Platinum';
    if (pts >= 200) return 'Gold';
    if (pts >= 50) return 'Silver';
    return 'Bronze';
  }
}
