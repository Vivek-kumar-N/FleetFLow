import { Component, OnInit } from '@angular/core';
import { ReportsService } from '../../services/reports.service';
import { DashboardStats } from '../../models/models';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  stats: DashboardStats = { totalEmployees: 0, totalCars: 0, totalCustomers: 0, totalRentals: 0, totalMaintenanceRecords: 0 };
  fleetHealth: any = {};
  topCustomers: any[] = [];
  minimalBookingsCars: any = {};
  incomeByModel: any = {};
  maintenanceCostByModel: any = {};
  carUtilization: any = {};
  loyaltyAnalytics: any = {};
  loading = true;
  error = '';

  constructor(private reportsService: ReportsService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.reportsService.getDashboardStats().subscribe({ next: d => { this.stats = d; this.loading = false; }, error: () => { this.loading = false; this.error = 'Failed to load stats.'; } });
    this.reportsService.getFleetHealthReport().subscribe({ next: d => this.fleetHealth = d, error: () => {} });
    this.reportsService.getCustomersWithMaxBookings().subscribe({ next: d => this.topCustomers = d.slice(0, 5), error: () => {} });
    this.reportsService.getCarsWithMinimalBookings().subscribe({ next: d => this.minimalBookingsCars = d, error: () => {} });
    this.reportsService.getIncomeByCarModel().subscribe({ next: d => this.incomeByModel = d, error: () => {} });
    this.reportsService.getMaintenanceCostByCarModel().subscribe({ next: d => this.maintenanceCostByModel = d, error: () => {} });
    this.reportsService.getCarUtilizationReport().subscribe({ next: d => this.carUtilization = d, error: () => {} });
    this.reportsService.getCustomerLoyaltyAnalytics().subscribe({ next: d => this.loyaltyAnalytics = d, error: () => {} });
  }

  get incomeEntries(): [string, number][] { return Object.entries(this.incomeByModel || {}).map(([k, v]) => [k, Number(v)]); }
  get maintenanceEntries(): [string, number][] { return Object.entries(this.maintenanceCostByModel || {}).map(([k, v]) => [k, Number(v)]); }
  get utilizationEntries(): [string, number][] { return Object.entries(this.carUtilization || {}).map(([k, v]) => [k, Number(v)]); }
  get minimalEntries(): [string, number][] { return Object.entries(this.minimalBookingsCars || {}).map(([k, v]) => [k, Number(v)]); }

  getPercent(val: any, entries: any[]): number {
    const max = Math.max(...entries.map(e => Number(e[1])));
    return max > 0 ? (Number(val) / max) * 100 : 0;
  }
}
