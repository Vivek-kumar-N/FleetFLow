import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CustomerService } from '../../services/customer.service';
import { BookingService } from '../../services/booking.service';
import { MaintenanceService } from '../../services/maintenance.service';
import { Maintenance } from '../../models/models';

@Component({
  selector: 'app-customer-maintenance',
  templateUrl: './customer-maintenance.component.html',
  styleUrls: ['./customer-maintenance.component.css']
})
export class CustomerMaintenanceComponent implements OnInit {
  maintenanceRecords: Maintenance[] = [];
  rentedCarReg = '';
  loading = true;
  error = '';

  constructor(
    private authService: AuthService,
    private customerService: CustomerService,
    private bookingService: BookingService,
    private maintenanceService: MaintenanceService
  ) {}

  ngOnInit(): void {
    const username = this.authService.getUsername();
    this.customerService.getAllCustomers().subscribe({
      next: (customers) => {
        const found = customers.find(c => c.emailId === username || c.customerId === username);
        if (found) {
          this.bookingService.getBookingsByCustomer(found.customerId!).subscribe({
            next: (bookings) => {
              const active = bookings.find(b => b.bookingStatus === 'ACTIVE' || b.bookingStatus === 'CONFIRMED');
              if (active?.car?.registrationNumber) {
                this.rentedCarReg = active.car.registrationNumber;
                this.maintenanceService.getByCarRegistration(this.rentedCarReg).subscribe({
                  next: (d: Maintenance[]) => { this.maintenanceRecords = d; this.loading = false; },
                  error: () => { this.loading = false; }
                });
              } else {
                this.loading = false;
              }
            },
            error: () => { this.loading = false; }
          });
        } else {
          this.loading = false;
        }
      },
      error: () => { this.loading = false; this.error = 'Failed to load data.'; }
    });
  }
}
