import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CustomerService } from '../../services/customer.service';
import { BookingService } from '../../services/booking.service';
import { CarService } from '../../services/car.service';
import { Customer, Booking, Car } from '../../models/models';

@Component({
  selector: 'app-customer-dashboard',
  templateUrl: './customer-dashboard.component.html',
  styleUrls: ['./customer-dashboard.component.css']
})
export class CustomerDashboardComponent implements OnInit {
  customer: Customer | null = null;
  activeBookings: Booking[] = [];
  availableCars: Car[] = [];
  loyaltyDiscount = 0;
  freeRentalEligible = false;
  loading = true;
  Math = Math;
  // customerId stored after linking username -> customer
  customerId = '';

  constructor(
    public authService: AuthService,
    private customerService: CustomerService,
    private bookingService: BookingService,
    private carService: CarService
  ) {}

  ngOnInit(): void {
    // Load available cars for all customers
    this.carService.getAvailableCars().subscribe({ next: d => this.availableCars = d, error: () => {} });

    // Fetch customer profile directly by logged-in username
    const username = this.authService.getUsername();
    this.customerService.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customer = customer;
        this.customerId = customer.customerId!;
        this.loadCustomerData();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadCustomerData(): void {
    if (!this.customerId) return;
    this.bookingService.getBookingsByCustomer(this.customerId).subscribe({
      next: (bookings) => {
        this.activeBookings = bookings.filter(b => b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'ACTIVE');
      },
      error: () => {}
    });
    this.customerService.getLoyaltyDiscount(this.customerId).subscribe({
      next: (d: any) => this.loyaltyDiscount = d.discountPercent || 0,
      error: () => {}
    });
    this.customerService.isEligibleForFreeRental(this.customerId).subscribe({
      next: (d: any) => this.freeRentalEligible = d === true || d?.eligible === true,
      error: () => {}
    });
  }

  get loyaltyTier(): string {
    const pts = this.customer?.loyaltyPoints || 0;
    if (pts >= 500) return 'Platinum';
    if (pts >= 200) return 'Gold';
    if (pts >= 50) return 'Silver';
    return 'Bronze';
  }

  get tierColor(): string {
    const t = this.loyaltyTier;
    if (t === 'Platinum') return 'bg-dark';
    if (t === 'Gold') return 'bg-warning text-dark';
    if (t === 'Silver') return 'bg-secondary';
    return 'bg-danger';
  }
}
