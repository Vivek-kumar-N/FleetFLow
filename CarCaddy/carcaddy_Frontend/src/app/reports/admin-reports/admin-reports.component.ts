import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ReportsService } from '../../services/reports.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-admin-reports',
  templateUrl: './admin-reports.component.html',
  styleUrls: ['./admin-reports.component.css']
})
export class AdminReportsComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('bookingTrendsChart') bookingTrendsChartRef!: ElementRef;
  @ViewChild('revenueTrendsChart') revenueTrendsChartRef!: ElementRef;
  @ViewChild('fleetPieChart')      fleetPieChartRef!: ElementRef;
  @ViewChild('maintenanceVsIncomeChart') maintenanceVsIncomeChartRef!: ElementRef;
  @ViewChild('profitabilityChart') profitabilityChartRef!: ElementRef;

  private charts: Chart[] = [];

  activeTab = 'revenue';
  loading = false;
  startDate = '';
  endDate = '';
  trendsGranularity: 'monthly' | 'weekly' | 'daily' = 'monthly';

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

  private dataLoaded = false;

  constructor(private reportsService: ReportsService) {}

  ngOnInit(): void {
    const today = new Date();
    const yearAgo = new Date(today);
    yearAgo.setFullYear(yearAgo.getFullYear() - 1);
    this.endDate = today.toISOString().split('T')[0];
    this.startDate = yearAgo.toISOString().split('T')[0];
    this.loadAll();
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    this.destroyAllCharts();
  }

  private destroyAllCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  loadAll(): void {
    this.loading = true;
    this.dataLoaded = false;
    this.destroyAllCharts();

    let pending = 12;
    const done = () => { if (--pending === 0) { this.loading = false; this.dataLoaded = true; setTimeout(() => this.renderActiveChart(), 100); } };

    this.reportsService.getRevenueForPeriod(this.startDate, this.endDate).subscribe({ next: d => { this.revenue = d; done(); }, error: () => done() });
    this.reportsService.getIncomeByCarModel().subscribe({ next: d => { this.incomeByModel = d; done(); }, error: () => done() });
    this.reportsService.getCarUtilizationReport().subscribe({ next: d => { this.carUtilization = d; done(); }, error: () => done() });
    this.reportsService.getCarsWithMinimalBookings().subscribe({ next: d => { this.carsMinimalBookings = d; done(); }, error: () => done() });
    this.reportsService.getFleetHealthReport().subscribe({ next: d => { this.fleetHealth = d; done(); }, error: () => done() });
    this.reportsService.getProfitabilityReport().subscribe({ next: d => { this.profitability = d; done(); }, error: () => done() });
    this.reportsService.getCustomersWithMaxBookings().subscribe({ next: d => { this.topCustomers = d; done(); }, error: () => done() });
    this.reportsService.getCustomerLoyaltyAnalytics().subscribe({ next: d => { this.loyaltyAnalytics = d; done(); }, error: () => done() });
    this.reportsService.getMaintenanceCostByCarModel().subscribe({ next: d => { this.maintenanceCostByModel = d; done(); }, error: () => done() });
    this.reportsService.getMaintenanceCostByPeriod(this.startDate, this.endDate).subscribe({ next: d => { this.maintenanceCostByPeriod = d; done(); }, error: () => done() });
    this.reportsService.getServiceCenterPerformance().subscribe({ next: d => { this.serviceCenterPerformance = d; done(); }, error: () => done() });
    this.reportsService.getBookingTrends(this.startDate, this.endDate).subscribe({ next: d => { this.bookingTrends = d; done(); }, error: () => done() });
  }

  resetDates(): void {
    const today = new Date();
    const yearAgo = new Date(today);
    yearAgo.setFullYear(yearAgo.getFullYear() - 1);
    this.endDate = today.toISOString().split('T')[0];
    this.startDate = yearAgo.toISOString().split('T')[0];
    this.loadAll();
  }

  switchTab(tab: string): void {
    this.activeTab = tab;
    setTimeout(() => this.renderActiveChart(), 100);
  }

  private renderActiveChart(): void {
    this.destroyAllCharts();
    switch (this.activeTab) {
      case 'trends':      this.renderTrendsCharts(); break;
      case 'fleet':       this.renderFleetPieChart(); break;
      case 'maintenance': this.renderMaintenanceVsIncomeChart(); break;
      case 'profitability': this.renderProfitabilityChart(); break;
    }
  }

  // ── Booking Trends — Line/Bar Chart ───────────────────────────────────────
  private renderTrendsCharts(): void {
    const months = Object.keys(this.bookingTrends?.bookingsByMonth || {});
    const bookings = months.map(m => Number(this.bookingTrends.bookingsByMonth[m] || 0));
    const revenues = months.map(m => Number(this.bookingTrends.revenueByMonth?.[m] || 0));

    if (this.bookingTrendsChartRef?.nativeElement) {
      const ctx = this.bookingTrendsChartRef.nativeElement.getContext('2d');
      this.charts.push(new Chart(ctx, {
        type: 'bar',
        data: {
          labels: months,
          datasets: [{
            label: 'Bookings',
            data: bookings,
            backgroundColor: 'rgba(13,110,253,0.7)',
            borderColor: 'rgba(13,110,253,1)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          plugins: { legend: { display: true }, title: { display: true, text: 'Booking Volume by Month' } },
          scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
        }
      }));
    }

    if (this.revenueTrendsChartRef?.nativeElement) {
      const ctx2 = this.revenueTrendsChartRef.nativeElement.getContext('2d');
      this.charts.push(new Chart(ctx2, {
        type: 'line',
        data: {
          labels: months,
          datasets: [{
            label: 'Revenue (₹)',
            data: revenues,
            backgroundColor: 'rgba(25,135,84,0.2)',
            borderColor: 'rgba(25,135,84,1)',
            borderWidth: 2,
            fill: true,
            tension: 0.3,
            pointBackgroundColor: 'rgba(25,135,84,1)'
          }]
        },
        options: {
          responsive: true,
          plugins: { legend: { display: true }, title: { display: true, text: 'Revenue Trend by Month' } },
          scales: { y: { beginAtZero: true } }
        }
      }));
    }
  }

  // ── Fleet Status — Pie/Donut Chart ────────────────────────────────────────
  private renderFleetPieChart(): void {
    if (!this.fleetPieChartRef?.nativeElement) return;
    const ctx = this.fleetPieChartRef.nativeElement.getContext('2d');
    this.charts.push(new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Available', 'Rented', 'In Maintenance'],
        datasets: [{
          data: [
            this.fleetHealth?.availableCars || 0,
            this.fleetHealth?.rentedCars || 0,
            this.fleetHealth?.maintenanceCars || 0
          ],
          backgroundColor: ['#198754', '#ffc107', '#dc3545'],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'bottom' },
          title: { display: true, text: 'Fleet Status Distribution' }
        }
      }
    }));
  }

  // ── Maintenance vs Income — Grouped Bar Chart ─────────────────────────────
  private renderMaintenanceVsIncomeChart(): void {
    if (!this.maintenanceVsIncomeChartRef?.nativeElement) return;
    const entries = this.maintenanceVsIncomeEntries;
    const ctx = this.maintenanceVsIncomeChartRef.nativeElement.getContext('2d');
    this.charts.push(new Chart(ctx, {
      type: 'bar',
      data: {
        labels: entries.map(e => e.model),
        datasets: [
          {
            label: 'Rental Income (₹)',
            data: entries.map(e => e.income),
            backgroundColor: 'rgba(25,135,84,0.7)',
            borderColor: 'rgba(25,135,84,1)',
            borderWidth: 1
          },
          {
            label: 'Maintenance Cost (₹)',
            data: entries.map(e => e.maintenance),
            backgroundColor: 'rgba(220,53,69,0.7)',
            borderColor: 'rgba(220,53,69,1)',
            borderWidth: 1
          },
          {
            label: 'Net Profit (₹)',
            data: entries.map(e => e.profit),
            backgroundColor: entries.map(e => e.profit >= 0 ? 'rgba(13,110,253,0.7)' : 'rgba(255,193,7,0.7)'),
            borderColor: entries.map(e => e.profit >= 0 ? 'rgba(13,110,253,1)' : 'rgba(255,193,7,1)'),
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'top' },
          title: { display: true, text: 'Maintenance Cost vs Rental Income per Car Model' }
        },
        scales: { y: { beginAtZero: true } }
      }
    }));
  }

  // ── Profitability — Horizontal Bar Chart ──────────────────────────────────
  private renderProfitabilityChart(): void {
    if (!this.profitabilityChartRef?.nativeElement) return;
    const entries = this.profitabilityEntries;
    const ctx = this.profitabilityChartRef.nativeElement.getContext('2d');
    this.charts.push(new Chart(ctx, {
      type: 'bar',
      data: {
        labels: entries.map(e => e[0]),
        datasets: [{
          label: 'Net Profit/Loss (₹)',
          data: entries.map(e => e[1]),
          backgroundColor: entries.map(e => e[1] >= 0 ? 'rgba(25,135,84,0.7)' : 'rgba(220,53,69,0.7)'),
          borderColor: entries.map(e => e[1] >= 0 ? 'rgba(25,135,84,1)' : 'rgba(220,53,69,1)'),
          borderWidth: 1
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        plugins: {
          legend: { display: false },
          title: { display: true, text: 'Most & Least Profitable Cars (Net Profit/Loss)' }
        },
        scales: { x: { beginAtZero: true } }
      }
    }));
  }

  // ── Computed entries ──────────────────────────────────────────────────────
  get incomeEntries(): [string, number][] { return Object.entries(this.incomeByModel || {}).map(([k, v]) => [k, Number(v)] as [string, number]).sort((a, b) => b[1] - a[1]); }
  get utilizationEntries(): [string, number][] { return Object.entries(this.carUtilization || {}).map(([k, v]) => [k, Number(v)] as [string, number]).sort((a, b) => b[1] - a[1]); }
  get minimalEntries(): [string, number][] { return Object.entries(this.carsMinimalBookings || {}).map(([k, v]) => [k, Number(v)] as [string, number]); }
  get profitabilityEntries(): [string, number][] { return Object.entries(this.profitability || {}).map(([k, v]) => [k, Number(v)] as [string, number]).sort((a, b) => b[1] - a[1]); }
  get maintenanceCostModelEntries(): [string, number][] { return Object.entries(this.maintenanceCostByModel || {}).map(([k, v]) => [k, Number(v)] as [string, number]); }
  get maintenanceCostPeriodEntries(): [string, number][] { return Object.entries(this.maintenanceCostByPeriod || {}).map(([k, v]) => [k, Number(v)] as [string, number]); }
  get bookingsByMonthEntries(): [string, number][] { return Object.entries(this.bookingTrends?.bookingsByMonth || {}).map(([k, v]) => [k, Number(v)] as [string, number]); }
  get revenueByMonthEntries(): [string, number][] { return Object.entries(this.bookingTrends?.revenueByMonth || {}).map(([k, v]) => [k, Number(v)] as [string, number]); }

  get mostProfitable(): [string, number][] { return this.profitabilityEntries.filter(e => e[1] >= 0).slice(0, 5); }
  get leastProfitable(): [string, number][] { return [...this.profitabilityEntries].sort((a, b) => a[1] - b[1]).slice(0, 5); }

  get maintenanceVsIncomeEntries(): { model: string; income: number; maintenance: number; profit: number }[] {
    const models = new Set([...Object.keys(this.incomeByModel || {}), ...Object.keys(this.maintenanceCostByModel || {})]);
    return Array.from(models).map(model => ({
      model,
      income: Number(this.incomeByModel?.[model] || 0),
      maintenance: Number(this.maintenanceCostByModel?.[model] || 0),
      profit: Number(this.incomeByModel?.[model] || 0) - Number(this.maintenanceCostByModel?.[model] || 0)
    })).sort((a, b) => b.profit - a.profit);
  }

  get fleetTotal(): number { return this.fleetHealth?.totalCars || 0; }

  getPercent(val: any, entries: any[]): number {
    const max = Math.max(...entries.map(e => Math.abs(Number(e[1]))));
    return max > 0 ? (Math.abs(Number(val)) / max) * 100 : 0;
  }

  getMaxValue(entries: { income: number; maintenance: number }[]): number {
    return Math.max(...entries.map(e => Math.max(e.income, e.maintenance)), 1);
  }

  print(): void { window.print(); }

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
      csv = 'Customer,Email,Loyalty Points\n' + this.topCustomers.map(c => `${c.customerName},${c.emailId},${c.loyaltyPoints}`).join('\n');
      filename = 'top_customers.csv';
    } else if (tab === 'maintenance') {
      csv = 'Model,Rental Income (INR),Maintenance Cost (INR),Net Profit (INR)\n' + this.maintenanceVsIncomeEntries.map(e => `${e.model},${e.income},${e.maintenance},${e.profit}`).join('\n');
      filename = 'maintenance_vs_income.csv';
    } else if (tab === 'profitability') {
      csv = 'Model,Net Profit (INR),Status\n' + this.profitabilityEntries.map(e => `${e[0]},${e[1]},${e[1] >= 0 ? 'Profitable' : 'Loss'}`).join('\n');
      filename = 'profitability.csv';
    } else if (tab === 'trends') {
      csv = 'Month,Bookings,Revenue (INR)\n' + this.bookingsByMonthEntries.map(e => { const rev = Number(this.bookingTrends?.revenueByMonth?.[e[0]] || 0); return `${e[0]},${e[1]},${rev}`; }).join('\n');
      filename = 'booking_trends.csv';
    } else if (tab === 'service') {
      csv = 'Performer,Jobs Completed,Total Cost (INR),Avg Completion Days\n' + this.serviceCenterPerformance.map(p => `${p.performer},${p.jobsCompleted},${p.totalCost?.toFixed(0) || 0},${p.avgCompletionDays?.toFixed(1) || 'N/A'}`).join('\n');
      filename = 'service_center_performance.csv';
    }
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
  }
}
