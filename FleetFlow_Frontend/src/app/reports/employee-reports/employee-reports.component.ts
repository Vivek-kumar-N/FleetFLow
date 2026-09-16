import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ReportsService } from '../../services/reports.service';
import { BookingService } from '../../services/booking.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-employee-reports',
  templateUrl: './employee-reports.component.html',
  styleUrls: ['./employee-reports.component.css']
})
export class EmployeeReportsComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('utilizationChart') utilizationChartRef!: ElementRef;

  private chart: Chart | null = null;

  carUtilization: any = {};
  activeCount = 0;
  completedCount = 0;
  loading = true;
  dataReady = false;

  constructor(private reportsService: ReportsService, private bookingService: BookingService) {}

  ngOnInit(): void {
    let pending = 3;
    const done = () => { if (--pending === 0) { this.loading = false; this.dataReady = true; setTimeout(() => this.renderChart(), 100); } };

    this.reportsService.getCarUtilizationReport().subscribe({
      next: d => { this.carUtilization = d; done(); },
      error: () => done()
    });
    this.bookingService.getActiveBookings().subscribe({
      next: d => { this.activeCount = d.length; done(); },
      error: () => done()
    });
    this.bookingService.getCompletedBookings().subscribe({
      next: d => { this.completedCount = d.length; done(); },
      error: () => done()
    });
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    if (this.chart) { this.chart.destroy(); this.chart = null; }
  }

  private renderChart(): void {
    if (!this.utilizationChartRef?.nativeElement) return;
    if (this.chart) { this.chart.destroy(); this.chart = null; }

    const entries = this.utilizationEntries;
    if (entries.length === 0) return;

    const ctx = this.utilizationChartRef.nativeElement.getContext('2d');
    this.chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: entries.map(e => e[0]),
        datasets: [{
          label: 'Number of Rentals',
          data: entries.map(e => e[1]),
          backgroundColor: entries.map((_, i) => `hsl(${(i * 47) % 360}, 65%, 55%)`),
          borderColor: entries.map((_, i) => `hsl(${(i * 47) % 360}, 65%, 40%)`),
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false },
          title: { display: true, text: 'Car Utilization — Rentals per Car' }
        },
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: 1 }, title: { display: true, text: 'Rentals' } },
          x: { title: { display: true, text: 'Car Registration' } }
        }
      }
    });
  }

  get utilizationEntries(): [string, number][] {
    return Object.entries(this.carUtilization || {}).map(([k, v]) => [k, Number(v)] as [string, number]).sort((a, b) => b[1] - a[1]);
  }

  getPercent(val: any, entries: any[]): number {
    const max = Math.max(...entries.map(e => Number(e[1])));
    return max > 0 ? (Number(val) / max) * 100 : 0;
  }
}
