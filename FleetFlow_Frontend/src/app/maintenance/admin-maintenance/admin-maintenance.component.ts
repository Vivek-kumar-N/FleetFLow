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

  // Filters
  typeFilter = '';
  statusFilter = '';
  regSearch = '';
  dateRangeStart = '';
  dateRangeEnd = '';

  // Modals
  showAddModal = false;
  showStatusModal = false;
  showDeleteModal = false;
  showDetailModal = false;
  showRoutineModal = false;
  showCostModal = false;

  selectedRecord: Maintenance | null = null;
  submitting = false;

  // Cost per car
  carRegForCost = '';
  totalCostForCar: number | null = null;
  carMaintenanceHistory: Maintenance[] = [];
  loadingCost = false;

  maintenanceForm: FormGroup;
  statusForm: FormGroup;
  routineForm: FormGroup;

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
    this.routineForm = this.fb.group({
      registrationNumber: ['', Validators.required],
      scheduledDate: ['', Validators.required],
      description: [''],
      cost: [0, Validators.min(0)],
      performedBy: ['']
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
      const matchType   = !this.typeFilter   || r.maintenanceType === this.typeFilter;
      const matchStatus = !this.statusFilter || r.status === this.statusFilter;
      const matchReg    = !this.regSearch    || (r.registrationNumber || '').toLowerCase().includes(this.regSearch.toLowerCase());
      let matchDate = true;
      if (this.dateRangeStart && r.scheduledDate) {
        matchDate = matchDate && r.scheduledDate >= this.dateRangeStart;
      }
      if (this.dateRangeEnd && r.scheduledDate) {
        matchDate = matchDate && r.scheduledDate <= this.dateRangeEnd;
      }
      return matchType && matchStatus && matchReg && matchDate;
    });
  }

  resetFilters(): void {
    this.typeFilter = '';
    this.statusFilter = '';
    this.regSearch = '';
    this.dateRangeStart = '';
    this.dateRangeEnd = '';
    this.applyFilters();
  }

  // ── Modals ────────────────────────────────────────────────────────────────
  openAddModal(): void { this.maintenanceForm.reset({ maintenanceType: 'ROUTINE', status: 'SCHEDULED', cost: 0 }); this.showAddModal = true; this.error = ''; this.success = ''; }
  openStatusModal(r: Maintenance): void { this.selectedRecord = r; this.statusForm.patchValue({ status: r.status, cost: r.cost, performedBy: r.performedBy }); this.showStatusModal = true; }
  openDeleteModal(r: Maintenance): void { this.selectedRecord = r; this.showDeleteModal = true; }
  openDetailModal(r: Maintenance): void { this.selectedRecord = r; this.showDetailModal = true; }
  openRoutineModal(): void { this.routineForm.reset({ cost: 0, scheduledDate: new Date().toISOString().split('T')[0] }); this.showRoutineModal = true; this.error = ''; this.success = ''; }

  openCostModal(): void {
    this.carRegForCost = '';
    this.totalCostForCar = null;
    this.carMaintenanceHistory = [];
    this.showCostModal = true;
  }

  lookupCarCost(): void {
    if (!this.carRegForCost.trim()) return;
    this.loadingCost = true;
    this.maintenanceService.getTotalCostByCar(this.carRegForCost).subscribe({
      next: cost => { this.totalCostForCar = cost; this.loadingCost = false; },
      error: () => { this.loadingCost = false; }
    });
    this.maintenanceService.getByCarRegistration(this.carRegForCost).subscribe({
      next: records => { this.carMaintenanceHistory = records; },
      error: () => {}
    });
  }

  closeModals(): void {
    this.showAddModal = false;
    this.showStatusModal = false;
    this.showDeleteModal = false;
    this.showDetailModal = false;
    this.showRoutineModal = false;
    this.showCostModal = false;
    this.selectedRecord = null;
    this.submitting = false;
  }

  // ── CRUD ──────────────────────────────────────────────────────────────────
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
    if (this.routineForm.invalid) { this.routineForm.markAllAsTouched(); return; }
    this.submitting = true;
    const v = this.routineForm.value;
    const dto: any = {
      registrationNumber: v.registrationNumber,
      maintenanceType: 'ROUTINE',
      status: 'SCHEDULED',
      cost: v.cost || 0,
      scheduledDate: v.scheduledDate,
      description: v.description || 'Routine maintenance',
      performedBy: v.performedBy || ''
    };
    this.maintenanceService.scheduleRoutine(dto).subscribe({
      next: () => { this.success = 'Routine maintenance scheduled!'; this.closeModals(); this.loadRecords(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  get rf() { return this.routineForm.controls; }
  get f()  { return this.maintenanceForm.controls; }
}
