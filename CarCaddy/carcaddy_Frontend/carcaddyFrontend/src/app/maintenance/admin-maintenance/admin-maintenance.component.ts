import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MaintenanceService } from '../../services/maintenance.service';
import { AuthService } from '../../services/auth.service';
import { Maintenance } from '../../models/models';

@Component({
  selector: 'app-admin-maintenance',
  templateUrl: './admin-maintenance.component.html',
  styleUrls: ['./admin-maintenance.component.css']
})
export class AdminMaintenanceComponent implements OnInit {
  records: Maintenance[] = [];
  filteredRecords: Maintenance[] = [];
  loading = false;
  error = '';
  success = '';
  typeFilter = '';
  statusFilter = '';

  showAddModal = false;
  showStatusModal = false;
  showDeleteModal = false;
  selectedRecord: Maintenance | null = null;
  submitting = false;

  maintenanceForm: FormGroup;
  statusForm: FormGroup;

  types = ['ROUTINE', 'REPAIR', 'EMERGENCY'];
  statuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

  constructor(private maintenanceService: MaintenanceService, private fb: FormBuilder) {
    this.maintenanceForm = this.fb.group({
      registrationNumber: ['', Validators.required],
      maintenanceType: ['ROUTINE', Validators.required],
      scheduledDate: ['', Validators.required],
      description: [''],
      cost: [0, Validators.min(0)],
      performedBy: [''],
      status: ['SCHEDULED', Validators.required]
    });
    this.statusForm = this.fb.group({
      status: ['', Validators.required],
      completedDate: [''],
      cost: [0],
      performedBy: [''],
      description: ['']
    });
  }

  ngOnInit(): void { this.loadRecords(); }

  loadRecords(): void {
    this.loading = true;
    this.maintenanceService.getAll().subscribe({
      next: d => { this.records = d; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load records.'; }
    });
  }

  applyFilters(): void {
    this.filteredRecords = this.records.filter(r => {
      const matchType = !this.typeFilter || r.maintenanceType === this.typeFilter;
      const matchStatus = !this.statusFilter || r.status === this.statusFilter;
      return matchType && matchStatus;
    });
  }

  openAddModal(): void { this.maintenanceForm.reset({ maintenanceType: 'ROUTINE', status: 'SCHEDULED', cost: 0 }); this.showAddModal = true; this.error = ''; this.success = ''; }
  openStatusModal(r: Maintenance): void { this.selectedRecord = r; this.statusForm.patchValue({ status: r.status, cost: r.cost, performedBy: r.performedBy }); this.showStatusModal = true; }
  openDeleteModal(r: Maintenance): void { this.selectedRecord = r; this.showDeleteModal = true; }
  closeModals(): void { this.showAddModal = false; this.showStatusModal = false; this.showDeleteModal = false; this.selectedRecord = null; this.submitting = false; }

  submitMaintenance(): void {
    if (this.maintenanceForm.invalid) { this.maintenanceForm.markAllAsTouched(); return; }
    this.submitting = true;
    this.maintenanceService.addMaintenance(this.maintenanceForm.value).subscribe({
      next: () => { this.success = 'Maintenance record added!'; this.closeModals(); this.loadRecords(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  updateStatus(): void {
    if (!this.selectedRecord) return;
    this.submitting = true;
    const v = this.statusForm.value;
    this.maintenanceService.updateStatus(this.selectedRecord.maintenanceId!, v.status, v.completedDate || undefined, v.cost || undefined, v.performedBy || undefined).subscribe({
      next: () => { this.success = 'Status updated!'; this.closeModals(); this.loadRecords(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  deleteRecord(): void {
    if (!this.selectedRecord) return;
    this.submitting = true;
    this.maintenanceService.delete(this.selectedRecord.maintenanceId!).subscribe({
      next: () => { this.success = 'Record deleted.'; this.closeModals(); this.loadRecords(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  scheduleRoutine(): void {
    const dto: any = { maintenanceType: 'ROUTINE', status: 'SCHEDULED', cost: 0, scheduledDate: new Date().toISOString().split('T')[0], description: 'Auto-scheduled routine maintenance' };
    this.maintenanceService.scheduleRoutine(dto).subscribe({
      next: () => { this.success = 'Routine maintenance scheduled!'; this.loadRecords(); },
      error: (err: any) => { this.error = AuthService.parseError(err); }
    });
  }

  get f() { return this.maintenanceForm.controls; }
}
