import { Component, OnInit } from '@angular/core';
import { CarService } from '../../services/car.service';
import { BookingService } from '../../services/booking.service';
import { MaintenanceService } from '../../services/maintenance.service';
import { Car, Booking, Maintenance } from '../../models/models';

@Component({
  selector: 'app-employee-dashboard',
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {
  availableCars: Car[] = [];
  activeBookings: Booking[] = [];
  upcomingMaintenance: Maintenance[] = [];
  maintenanceCars: Car[] = [];
  loading = true;

  constructor(
    private carService: CarService,
    private bookingService: BookingService,
    private maintenanceService: MaintenanceService
  ) {}

  ngOnInit(): void {
    this.carService.getAvailableCars().subscribe({ next: d => this.availableCars = d, error: () => {} });
    this.bookingService.getActiveBookings().subscribe({ next: d => { this.activeBookings = d; this.loading = false; }, error: () => { this.loading = false; } });
    this.maintenanceService.getUpcoming().subscribe({ next: d => this.upcomingMaintenance = d.slice(0, 5), error: () => {} });
    this.carService.getCarsByStatus('MAINTENANCE').subscribe({ next: d => this.maintenanceCars = d, error: () => {} });
  }
}
