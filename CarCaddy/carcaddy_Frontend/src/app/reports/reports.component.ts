import { Component, OnInit } from '@angular/core';
import { ReportsService } from '../services/reports.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  activeTab = 'revenue';
  loading = false;
  error = '';

  startDate = '';
  endDate = '';

  // Revenue
  revenue: number = 0;
  incomeByModel: { [key: string]: number } = {};

  // Fleet
  carUtilization: { [key: string]: number } = {};
  carsMinimalBookings: { [key: string]: number } = {};
  fleetHealth: any = {};
  profitability: { [key: string]: number } = {};

  // Customers
  topCustomers: any[] = [];
  loyaltyAnalytics: any = {};

  // Maintenance
  maintenanceCostByModel: { [key: string]: number } = {};
  maintenanceCostByPeriod: { [key: string]: number } = {};

  constructor(
    private reportsService: ReportsService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const today = new Date();
    const monthAgo = new Date(today);
    monthAgo.setMonth(monthAgo.getMonth() - 1);
    this.endDate = today.toISOString().split('T')[0];
    this.startDate = monthAgo.toISOString().split('T')[0];

    this.generateReports();
  }

  generateReports(): void {
    this.loading = true;
    this.error = '';

    // Revenue
    this.reportsService.getRevenueForPeriod(this.startDate, this.endDate).subscribe({
      next: (d) => this.revenue = d,
      error: () => {}
    });

    this.reportsService.getIncomeByCarModel().subscribe({
      next: (d) => this.incomeByModel = d,
      error: () => {}
    });

    // Fleet
    this.reportsService.getCarUtilizationReport().subscribe({
      next: (d) => this.carUtilization = d,
      error: () => {}
    });

    this.reportsService.getCarsWithMinimalBookings().subscribe({
      next: (d) => this.carsMinimalBookings = d,
      error: () => {}
    });

    this.reportsService.getFleetHealthReport().subscribe({
      next: (d) => this.fleetHealth = d,
      error: () => {}
    });

    this.reportsService.getProfitabilityReport().subscribe({
      next: (d) => this.profitability = d,
      error: () => {}
    });

    // Customers
    this.reportsService.getCustomersWithMaxBookings().subscribe({
      next: (d) => this.topCustomers = d,
      error: () => {}
    });

    this.reportsService.getCustomerLoyaltyAnalytics().subscribe({
      next: (d) => this.loyaltyAnalytics = d,
      error: () => {}
    });

    // Maintenance
    this.reportsService.getMaintenanceCostByCarModel().subscribe({
      next: (d) => this.maintenanceCostByModel = d,
      error: () => {}
    });

    this.reportsService.getMaintenanceCostByPeriod(this.startDate, this.endDate).subscribe({
      next: (d) => { this.maintenanceCostByPeriod = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get incomeByModelEntries(): [string, number][] { return Object.entries(this.incomeByModel).map(([k, v]) => [k, Number(v)]); }
  get carUtilizationEntries(): [string, number][] { return Object.entries(this.carUtilization).map(([k, v]) => [k, Number(v)]); }
  get carsMinimalEntries(): [string, number][] { return Object.entries(this.carsMinimalBookings).map(([k, v]) => [k, Number(v)]); }
  get profitabilityEntries(): [string, number][] { return Object.entries(this.profitability).map(([k, v]) => [k, Number(v)]); }
  get maintenanceCostByModelEntries(): [string, number][] { return Object.entries(this.maintenanceCostByModel).map(([k, v]) => [k, Number(v)]); }
  get maintenanceCostByPeriodEntries(): [string, number][] { return Object.entries(this.maintenanceCostByPeriod).map(([k, v]) => [k, Number(v)]); }

  getProfitClass(value: number): string {
    return value >= 0 ? 'profit-positive' : 'profit-negative';
  }

  print(): void { window.print(); }
}
