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
  serviceCenterPerformance: any[] = [];
  bookingTrends: any = {};

  constructor(private reportsService: ReportsService) {}

  ngOnInit(): void {
    const today = new Date();
    const yearAgo = new Date(today);
    yearAgo.setFullYear(yearAgo.getFullYear() - 1);
    this.endDate = today.toISOString().split('T')[0];
    this.startDate = yearAgo.toISOString().split('T')[0];
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
    this.reportsService.getMaintenanceCostByPeriod(this.startDate, this.endDate).subscribe({ next: d => this.maintenanceCostByPeriod = d, error: () => {} });
    this.reportsService.getServiceCenterPerformance().subscribe({ next: d => this.serviceCenterPerformance = d, error: () => {} });
    this.reportsService.getBookingTrends(this.startDate, this.endDate).subscribe({
      next: d => { this.bookingTrends = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  // ── Computed entries ──────────────────────────────────────────────────────
  get incomeEntries(): [string, number][] { return Object.entries(this.incomeByModel || {}).map(([k, v]) => [k, Number(v)]); }
  get utilizationEntries(): [string, number][] { return Object.entries(this.carUtilization || {}).map(([k, v]) => [k, Number(v)]); }
  get minimalEntries(): [string, number][] { return Object.entries(this.carsMinimalBookings || {}).map(([k, v]) => [k, Number(v)]); }
  get profitabilityEntries(): [string, number][] { return Object.entries(this.profitability || {}).map(([k, v]) => [k, Number(v)]); }
  get maintenanceCostModelEntries(): [string, number][] { return Object.entries(this.maintenanceCostByModel || {}).map(([k, v]) => [k, Number(v)]); }
  get maintenanceCostPeriodEntries(): [string, number][] { return Object.entries(this.maintenanceCostByPeriod || {}).map(([k, v]) => [k, Number(v)]); }
  get bookingsByMonthEntries(): [string, number][] { return Object.entries(this.bookingTrends?.bookingsByMonth || {}).map(([k, v]) => [k, Number(v)]); }
  get revenueByMonthEntries(): [string, number][] { return Object.entries(this.bookingTrends?.revenueByMonth || {}).map(([k, v]) => [k, Number(v)]); }

  getPercent(val: any, entries: any[]): number {
    const max = Math.max(...entries.map(e => Math.abs(Number(e[1]))));
    return max > 0 ? (Math.abs(Number(val)) / max) * 100 : 0;
  }

  // ── Print ─────────────────────────────────────────────────────────────────
  print(): void { window.print(); }

  // ── CSV Export ────────────────────────────────────────────────────────────
  exportCSV(tab: string): void {
    let csv = '';
    let filename = 'report.csv';

    if (tab === 'revenue') {
      csv = 'Model,Income (INR)\n' + this.incomeEntries.map(e => `${e[0]},${e[1]}`).join('\n');
      filename = 'revenue_by_model.csv';
    } else if (tab === 'fleet') {
      csv = 'Car,Rentals\n' + this.utilizationEntries.map(e => `${e[0]},${e[1]}`).join('\n');
      filename = 'car_utilization.csv';
    } else if (tab === 'customers') {
      csv = 'Customer,Email,Loyalty Points\n' +
        this.topCustomers.map(c => `${c.customerName},${c.emailId},${c.loyaltyPoints}`).join('\n');
      filename = 'top_customers.csv';
    } else if (tab === 'maintenance') {
      csv = 'Model,Cost (INR)\n' + this.maintenanceCostModelEntries.map(e => `${e[0]},${e[1]}`).join('\n');
      filename = 'maintenance_cost.csv';
    } else if (tab === 'profitability') {
      csv = 'Model,Net Profit (INR)\n' + this.profitabilityEntries.map(e => `${e[0]},${e[1]}`).join('\n');
      filename = 'profitability.csv';
    } else if (tab === 'trends') {
      csv = 'Month,Bookings,Revenue (INR)\n' +
        this.bookingsByMonthEntries.map(e => {
          const rev = Number(this.bookingTrends?.revenueByMonth?.[e[0]] || 0);
          return `${e[0]},${e[1]},${rev}`;
        }).join('\n');
      filename = 'booking_trends.csv';
    } else if (tab === 'service') {
      csv = 'Performer,Jobs Completed,Total Cost (INR),Avg Completion Days\n' +
        this.serviceCenterPerformance.map(p =>
          `${p.performer},${p.jobsCompleted},${p.totalCost?.toFixed(0) || 0},${p.avgCompletionDays?.toFixed(1) || 'N/A'}`
        ).join('\n');
      filename = 'service_center_performance.csv';
    }

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }
}
