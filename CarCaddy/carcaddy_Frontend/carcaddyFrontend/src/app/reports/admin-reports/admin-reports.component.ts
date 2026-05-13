import { Component, OnInit } from '@angular/core';
import { ReportsService } from '../../services/reports.service';

@Component({
  selector: 'app-admin-reports',
  templateUrl: './admin-reports.component.html',
  styleUrls: ['./admin-reports.component.css']
})
export class AdminReportsComponent implements OnInit {
  activeTab = 'revenue';
  loading = false;
  startDate = '';
  endDate = '';

  revenue = 0;
  incomeByModel: any = {};
  carUtilization: any = {};
  carsMinimalBookings: any = {};
  fleetHealth: any = {};
  profitability: any = {};
  topCustomers: any[] = [];
  loyaltyAnalytics: any = {};
  maintenanceCostByModel: any = {};
  maintenanceCostByPeriod: any = {};

  constructor(private reportsService: ReportsService) {}

  ngOnInit(): void {
    const today = new Date();
    const monthAgo = new Date(today);
    monthAgo.setMonth(monthAgo.getMonth() - 1);
    this.endDate = today.toISOString().split('T')[0];
    this.startDate = monthAgo.toISOString().split('T')[0];
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.reportsService.getRevenueForPeriod(this.startDate, this.endDate).subscribe({ next: d => this.revenue = d, error: () => {} });
    this.reportsService.getIncomeByCarModel().subscribe({ next: d => this.incomeByModel = d, error: () => {} });
    this.reportsService.getCarUtilizationReport().subscribe({ next: d => this.carUtilization = d, error: () => {} });
    this.reportsService.getCarsWithMinimalBookings().subscribe({ next: d => this.carsMinimalBookings = d, error: () => {} });
    this.reportsService.getFleetHealthReport().subscribe({ next: d => this.fleetHealth = d, error: () => {} });
    this.reportsService.getProfitabilityReport().subscribe({ next: d => this.profitability = d, error: () => {} });
    this.reportsService.getCustomersWithMaxBookings().subscribe({ next: d => this.topCustomers = d, error: () => {} });
    this.reportsService.getCustomerLoyaltyAnalytics().subscribe({ next: d => this.loyaltyAnalytics = d, error: () => {} });
    this.reportsService.getMaintenanceCostByCarModel().subscribe({ next: d => this.maintenanceCostByModel = d, error: () => {} });
    this.reportsService.getMaintenanceCostByPeriod(this.startDate, this.endDate).subscribe({ next: d => { this.maintenanceCostByPeriod = d; this.loading = false; }, error: () => { this.loading = false; } });
  }

  get incomeEntries(): [string, number][] { return Object.entries(this.incomeByModel || {}).map(([k, v]) => [k, Number(v)]); }
  get utilizationEntries(): [string, number][] { return Object.entries(this.carUtilization || {}).map(([k, v]) => [k, Number(v)]); }
  get minimalEntries(): [string, number][] { return Object.entries(this.carsMinimalBookings || {}).map(([k, v]) => [k, Number(v)]); }
  get profitabilityEntries(): [string, number][] { return Object.entries(this.profitability || {}).map(([k, v]) => [k, Number(v)]); }
  get maintenanceCostModelEntries(): [string, number][] { return Object.entries(this.maintenanceCostByModel || {}).map(([k, v]) => [k, Number(v)]); }
  get maintenanceCostPeriodEntries(): [string, number][] { return Object.entries(this.maintenanceCostByPeriod || {}).map(([k, v]) => [k, Number(v)]); }

  getPercent(val: any, entries: any[]): number {
    const max = Math.max(...entries.map(e => Math.abs(Number(e[1]))));
    return max > 0 ? (Math.abs(Number(val)) / max) * 100 : 0;
  }

  print(): void { window.print(); }
}
