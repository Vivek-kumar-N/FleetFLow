import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';
import { Employee } from '../../models/models';

@Component({
  selector: 'app-admin-employees',
  templateUrl: './admin-employees.component.html',
  styleUrls: ['./admin-employees.component.css']
})
export class AdminEmployeesComponent implements OnInit {
  employees: Employee[] = [];
  filteredEmployees: Employee[] = [];
  loading = false;
  error = '';
  success = '';
  searchTerm = '';
  designationFilter = '';
  statusFilter = '';

  showAddModal = false;
  showDeleteModal = false;
  showExpiryModal = false;
  selectedEmployee: Employee | null = null;
  submitting = false;
  newPassword = '';

  employeeForm: FormGroup;
  expiryForm: FormGroup;
  contactForm: FormGroup;
  showContactModal = false;

  designations = ['Manager', 'Receptionist', 'Mechanic', 'Driver', 'Accountant', 'Supervisor'];
  accountTypes = ['PERMANENT', 'TEMPORARY'];

  constructor(private employeeService: EmployeeService, private fb: FormBuilder) {
    this.employeeForm = this.fb.group({
      employeeName: ['', [Validators.required, Validators.minLength(3)]],
      emailId: ['', [Validators.required, Validators.email]],
      contactNumber: ['', [Validators.required, Validators.pattern('^[6-9][0-9]{9}$')]],
      designation: ['Manager', Validators.required],
      dateOfBirth: ['', Validators.required],
      accountType: ['PERMANENT', Validators.required],
      expiryDate: ['']
    });
    this.expiryForm = this.fb.group({ expiryDate: ['', Validators.required] });
    this.contactForm = this.fb.group({ contactNumber: ['', [Validators.required, Validators.pattern('^[6-9][0-9]{9}$')]] });
  }

  ngOnInit(): void { this.loadEmployees(); }

  loadEmployees(): void {
    this.loading = true;
    this.employeeService.getAllEmployees().subscribe({
      next: d => { this.employees = d; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load employees.'; }
    });
  }

  applyFilters(): void {
    this.filteredEmployees = this.employees.filter(e => {
      const s = this.searchTerm.toLowerCase();
      const matchSearch = !s || e.employeeName.toLowerCase().includes(s) || e.emailId.toLowerCase().includes(s);
      const matchDes = !this.designationFilter || e.designation === this.designationFilter;
      const matchStatus = !this.statusFilter || (this.statusFilter === 'ACTIVE' ? e.accountActive : !e.accountActive);
      return matchSearch && matchDes && matchStatus;
    });
  }

  openAddModal(): void { this.employeeForm.reset({ designation: 'Manager', accountType: 'PERMANENT' }); this.showAddModal = true; this.error = ''; this.success = ''; this.newPassword = ''; }
  openDeleteModal(e: Employee): void { this.selectedEmployee = e; this.showDeleteModal = true; }
  openExpiryModal(e: Employee): void { this.selectedEmployee = e; this.expiryForm.reset({ expiryDate: e.expiryDate || '' }); this.showExpiryModal = true; }
  openContactModal(e: Employee): void { this.selectedEmployee = e; this.contactForm.reset({ contactNumber: e.contactNumber }); this.showContactModal = true; }
  closeModals(): void { this.showAddModal = false; this.showDeleteModal = false; this.showExpiryModal = false; this.showContactModal = false; this.selectedEmployee = null; this.submitting = false; }

  submitEmployee(): void {
    if (this.employeeForm.invalid) { this.employeeForm.markAllAsTouched(); return; }
    this.submitting = true;
    this.employeeService.addEmployee({ ...this.employeeForm.value, firstLogin: true }).subscribe({
      next: (saved: any) => { this.newPassword = saved.password || ''; this.success = `Employee added! Default password: ${this.newPassword}`; this.closeModals(); this.loadEmployees(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  deleteEmployee(): void {
    if (!this.selectedEmployee) return;
    this.submitting = true;
    this.employeeService.deleteEmployee(this.selectedEmployee.employeeId!).subscribe({
      next: () => { this.success = 'Employee deleted.'; this.closeModals(); this.loadEmployees(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  setExpiry(): void {
    if (!this.selectedEmployee || this.expiryForm.invalid) return;
    this.submitting = true;
    this.employeeService.setExpiryDate(this.selectedEmployee.employeeId!, this.expiryForm.value.expiryDate).subscribe({
      next: () => { this.success = 'Expiry date set.'; this.closeModals(); this.loadEmployees(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  updateContact(): void {
    if (!this.selectedEmployee || this.contactForm.invalid) return;
    this.submitting = true;
    this.employeeService.updateContactNumber(this.selectedEmployee.employeeId!, this.contactForm.value.contactNumber).subscribe({
      next: () => { this.success = 'Contact updated.'; this.closeModals(); this.loadEmployees(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  autoDeactivate(): void {
    this.employeeService.autoDeactivate().subscribe({
      next: () => { this.success = 'Expired accounts deactivated and notified.'; this.loadEmployees(); },
      error: (err: any) => { this.error = AuthService.parseError(err); }
    });
  }

  get f() { return this.employeeForm.controls; }
}
