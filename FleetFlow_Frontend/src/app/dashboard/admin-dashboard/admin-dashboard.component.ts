import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { ReportsService } from '../../services/reports.service';
import { CarService } from '../../services/car.service';
import { DashboardStats } from '../../models/models';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('utilizationBarChart') utilizationBarChartRef!: ElementRef;
  @ViewChild('incomeBarChart') incomeBarChartRef!: ElementRef;

  private charts: Chart[] = [];

  stats: DashboardStats = { totalEmployees: 0, totalCars: 0, totalCustomers: 0, totalRentals: 0, totalMaintenanceRecords: 0 };
  fleetHealth: any = {};
  topCustomers: any[] = [];
  minimalBookingsCars: any = {};
  incomeByModel: any = {};
  maintenanceCostByModel: any = {};
  carUtilization: any = {};
  loyaltyAnalytics: any = {};
  carsNeedingMaintenance: any[] = [];
  loading = true;
  error = '';
  dataReady = false;

  // Cached computed arrays - recalculated only when data loads
  cachedIncomeEntries: [string, number][] = [];
  cachedMaintenanceEntries: [string, number][] = [];
  cachedUtilizationEntries: [string, number][] = [];
  cachedMinimalEntries: [string, number][] = [];
  maintenanceMax = 0;

  constructor(private reportsService: ReportsService, private carService: CarService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadAll(); }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  loadAll(): void {
    this.loading = true;
    let pending = 9;
    const done = () => {
      if (--pending === 0) {
        this.loading = false;
        this.dataReady = true;
        this.computeCachedEntries();
        this.cdr.markForCheck();
        setTimeout(() => { this.renderCharts(); this.cdr.markForCheck(); }, 150);
      }
    };

    this.reportsService.getDashboardStats().subscribe({ next: d => { this.stats = d; done(); }, error: () => { this.loading = false; this.error = 'Failed to load stats.'; done(); } });
    this.reportsService.getFleetHealthReport().subscribe({ next: d => { this.fleetHealth = d; done(); }, error: () => done() });
    this.reportsService.getCustomersWithMaxBookings().subscribe({ next: d => { this.topCustomers = d.slice(0, 5); done(); }, error: () => done() });
    this.reportsService.getCarsWithMinimalBookings().subscribe({ next: d => { this.minimalBookingsCars = d; done(); }, error: () => done() });
    this.reportsService.getIncomeByCarModel().subscribe({ next: d => { this.incomeByModel = d; done(); }, error: () => done() });
    this.reportsService.getMaintenanceCostByCarModel().subscribe({ next: d => { this.maintenanceCostByModel = d; done(); }, error: () => done() });
    this.reportsService.getCarUtilizationReport().subscribe({ next: d => { this.carUtilization = d; done(); }, error: () => done() });
    this.reportsService.getCustomerLoyaltyAnalytics().subscribe({ next: d => { this.loyaltyAnalytics = d; done(); }, error: () => done() });
    this.carService.getCarsRequiringMaintenance().subscribe({ next: d => { this.carsNeedingMaintenance = d; done(); }, error: () => done() });
  }

  private computeCachedEntries(): void {
    this.cachedIncomeEntries = Object.entries(this.incomeByModel || {}).map(([k, v]) => [k, Number(v)] as [string, number]).sort((a, b) => b[1] - a[1]);
    this.cachedMaintenanceEntries = Object.entries(this.maintenanceCostByModel || {}).map(([k, v]) => [k, Number(v)] as [string, number]);
    this.cachedUtilizationEntries = Object.entries(this.carUtilization || {}).map(([k, v]) => [k, Number(v)] as [string, number]).sort((a, b) => b[1] - a[1]);
    this.cachedMinimalEntries = Object.entries(this.minimalBookingsCars || {}).map(([k, v]) => [k, Number(v)] as [string, number]);
    this.maintenanceMax = this.cachedMaintenanceEntries.length > 0 ? Math.max(...this.cachedMaintenanceEntries.map(e => e[1])) : 0;
  }

  private renderCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
    this.renderUtilizationChart();
    this.renderIncomeChart();
  }

  private renderUtilizationChart(): void {
    if (!this.utilizationBarChartRef?.nativeElement) return;
    const entries = this.cachedUtilizationEntries;
    if (entries.length === 0) return;
    const ctx = this.utilizationBarChartRef.nativeElement.getContext('2d');
    this.charts.push(new Chart(ctx, {
      type: 'bar',
      data: {
        labels: entries.map(e => e[0]),
        datasets: [{
          label: 'Rentals',
          data: entries.map(e => e[1]),
          backgroundColor: entries.map((_e, i) => `hsl(${(i * 47) % 360}, 65%, 55%)`),
          borderColor: entries.map((_e, i) => `hsl(${(i * 47) % 360}, 65%, 40%)`),
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false }, title: { display: true, text: 'Car Utilization — Rentals per Car' } },
        scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
      }
    }));
  }

  private renderIncomeChart(): void {
    if (!this.incomeBarChartRef?.nativeElement) return;
    const entries = this.cachedIncomeEntries;
    if (entries.length === 0) return;
    const ctx = this.incomeBarChartRef.nativeElement.getContext('2d');
    this.charts.push(new Chart(ctx, {
      type: 'bar',
      data: {
        labels: entries.map(e => e[0]),
        datasets: [{
          label: 'Income (₹)',
          data: entries.map(e => e[1]),
          backgroundColor: 'rgba(25,135,84,0.7)',
          borderColor: 'rgba(25,135,84,1)',
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false }, title: { display: true, text: 'Revenue by Car Model' } },
        scales: { y: { beginAtZero: true, title: { display: true, text: 'Income (₹)' } } }
      }
    }));
  }

  getPercent(val: number): number {
    return this.maintenanceMax > 0 ? (val / this.maintenanceMax) * 100 : 0;
  }
}
