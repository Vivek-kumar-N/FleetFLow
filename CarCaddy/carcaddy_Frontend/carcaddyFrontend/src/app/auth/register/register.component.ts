import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  form: FormGroup;
  loading = false;
  error = '';
  success = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['ROLE_CUSTOMER', Validators.required],
      securityQuestion: [''],
      securityAnswer: [''],
      // Customer-only fields
      customerName: [''],
      emailId: [''],
      contactNumber: [''],
      drivingLicense: [''],
      address: [''],
      occupation: ['']
    });

    // Dynamically add/remove validators when role changes
    this.form.get('role')!.valueChanges.subscribe(role => {
      this.updateCustomerValidators(role);
    });

    // Set initial validators since default role is ROLE_CUSTOMER
    this.updateCustomerValidators('ROLE_CUSTOMER');
  }

  updateCustomerValidators(role: string): void {
    const customerFields = ['customerName', 'emailId', 'contactNumber', 'drivingLicense', 'address'];
    if (role === 'ROLE_CUSTOMER') {
      this.form.get('customerName')!.setValidators([Validators.required, Validators.minLength(2)]);
      this.form.get('emailId')!.setValidators([Validators.required, Validators.email]);
      this.form.get('contactNumber')!.setValidators([Validators.required, Validators.pattern('^[0-9]{10}$')]);
      this.form.get('drivingLicense')!.setValidators([Validators.required, Validators.minLength(5)]);
      this.form.get('address')!.setValidators([Validators.required, Validators.minLength(5)]);
    } else {
      customerFields.forEach(field => this.form.get(field)!.clearValidators());
    }
    customerFields.forEach(field => this.form.get(field)!.updateValueAndValidity());
  }

  get isCustomer(): boolean {
    return this.form.get('role')?.value === 'ROLE_CUSTOMER';
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';

    this.authService.register(this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Registration successful! Redirecting to login...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Registration failed.';
      }
    });
  }

  get f() { return this.form.controls; }
}
