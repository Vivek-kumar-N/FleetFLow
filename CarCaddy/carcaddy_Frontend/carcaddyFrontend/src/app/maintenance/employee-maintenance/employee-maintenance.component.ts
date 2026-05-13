import { Component, OnInit } from '@angular/core';
import { MaintenanceService } from '../../services/maintenance.service';
import { Maintenance } from '../../models/models';

@Component({
  selector: 'app-employee-maintenance',
  templateUrl: './employee-maintenance.component.html',
  styleUrls: ['./employee-maintenance.component.css']
})
export class EmployeeMaintenanceComponent implements OnInit {
  records: Maintenance[] = [];
  upcoming: Maintenance[] = [];
  overdue: Maintenance[] = [];
  filteredRecords: Maintenance[] = [];
  loading = false;
  error = '';
  typeFilter = '';
  statusFilter = '';
  activeTab = 'all';

  types = ['ROUTINE', 'REPAIR', 'EMERGENCY'];
  statuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

  constructor(private maintenanceService: MaintenanceService) {}

  ngOnInit(): void {
    this.loading = true;
    this.maintenanceService.getAll().subscribe({ next: d => { this.records = d; this.applyFilters(); this.loading = false; }, error: () => { this.loading = false; this.error = 'Failed to load.'; } });
    this.maintenanceService.getUpcoming().subscribe({ next: d => this.upcoming = d, error: () => {} });
    this.maintenanceService.getOverdue().subscribe({ next: d => this.overdue = d, error: () => {} });
  }

  applyFilters(): void {
    this.filteredRecords = this.records.filter(r => {
      const matchType = !this.typeFilter || r.maintenanceType === this.typeFilter;
      const matchStatus = !this.statusFilter || r.status === this.statusFilter;
      return matchType && matchStatus;
    });
  }
}
