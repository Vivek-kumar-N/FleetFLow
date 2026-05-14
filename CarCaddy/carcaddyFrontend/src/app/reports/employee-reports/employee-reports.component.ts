import { Component, OnInit } from '@angular/core';
import { ReportsService } from '../../services/reports.service';
import { BookingService } from '../../services/booking.service';

@Component({
  selector: 'app-employee-reports',
  templateUrl: './employee-reports.component.html',
  styleUrls: ['./employee-reports.component.css']
})
export class EmployeeReportsComponent implements OnInit {
  carUtilization: any = {};
  activeCount = 0;
  completedCount = 0;
  loading = true;

  constructor(private reportsService: ReportsService, private bookingService: BookingService) {}

  ngOnInit(): void {
    this.reportsService.getCarUtilizationReport().subscribe({ next: d => { this.carUtilization = d; this.loading = false; }, error: () => { this.loading = false; } });
    this.bookingService.getActiveBookings().subscribe({ next: d => this.activeCount = d.length, error: () => {} });
    this.bookingService.getCompletedBookings().subscribe({ next: d => this.completedCount = d.length, error: () => {} });
  }

  get utilizationEntries(): [string, number][] { return Object.entries(this.carUtilization || {}).map(([k, v]) => [k, Number(v)]); }

  getPercent(val: any, entries: any[]): number {
    const max = Math.max(...entries.map(e => Number(e[1])));
    return max > 0 ? (Number(val) / max) * 100 : 0;
  }
}
