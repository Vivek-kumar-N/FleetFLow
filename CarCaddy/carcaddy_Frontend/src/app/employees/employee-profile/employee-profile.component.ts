import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { EmployeeService } from '../../services/employee.service';
import { Employee } from '../../models/models';

@Component({
  selector: 'app-employee-profile',
  templateUrl: './employee-profile.component.html',
  styleUrls: ['./employee-profile.component.css']
})
export class EmployeeProfileComponent implements OnInit {
  employee: Employee | null = null;
  loading = true;
  error = '';
  success = '';

  passwordForm: FormGroup;
  contactForm: FormGroup;
  showPasswordForm = false;
  showContactForm = false;
  submitting = false;
  isFirstLogin = false;

  constructor(
    public authService: AuthService,
    private employeeService: EmployeeService,
    private fb: FormBuilder
  ) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatch });

    this.contactForm = this.fb.group({
      contactNumber: ['', [Validators.required, Validators.pattern('^[6-9][0-9]{9}$')]]
    });
  }

  passwordMatch(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value ? null : { mismatch: true };
  }

  ngOnInit(): void {
    this.employeeService.getAllEmployees().subscribe({
      next: (employees) => {
        const username = this.authService.getUsername();
        const found = employees.find(e => e.emailId === username);
        if (found) {
          this.employee = found;
          this.isFirstLogin = found.firstLogin === true;
          if (this.isFirstLogin) this.showPasswordForm = true;
        }
        this.loading = false;
      },
      error: () => { this.loading = false; this.error = 'Failed to load profile.'; }
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) { this.passwordForm.markAllAsTouched(); return; }
    this.submitting = true;
    const { currentPassword, newPassword } = this.passwordForm.value;
    this.authService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.success = 'Password changed successfully!';
        this.showPasswordForm = false;
        this.passwordForm.reset();
        this.submitting = false;
        this.isFirstLogin = false;
      },
      error: (err: any) => { this.submitting = false; this.error = err.error?.message || 'Failed to change password.'; }
    });
  }

  updateContact(): void {
    if (!this.employee || this.contactForm.invalid) return;
    this.submitting = true;
    this.employeeService.updateContactNumber(this.employee.employeeId!, this.contactForm.value.contactNumber).subscribe({
      next: () => {
        this.success = 'Contact updated!';
        this.showContactForm = false;
        if (this.employee) this.employee.contactNumber = this.contactForm.value.contactNumber;
        this.submitting = false;
      },
      error: (err: any) => { this.submitting = false; this.error = err.error || 'Failed.'; }
    });
  }

  get pf() { return this.passwordForm.controls; }
}
