import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  passwordForm: FormGroup;
  usernameForm: FormGroup;
  securityForm: FormGroup;

  loading = false;
  success = '';
  error = '';
  activeTab = 'password';

  constructor(public authService: AuthService, private fb: FormBuilder) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.usernameForm = this.fb.group({
      newUsername: ['', [Validators.required, Validators.minLength(3)]]
    });

    this.securityForm = this.fb.group({
      securityQuestion: ['', Validators.required],
      securityAnswer: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.authService.getSecurityQuestion().subscribe({
      next: (res: any) => {
        if (res.securityQuestion) {
          this.securityForm.patchValue({ securityQuestion: res.securityQuestion });
        }
      },
      error: () => {}
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';

    this.authService.changePassword(
      this.passwordForm.value.currentPassword,
      this.passwordForm.value.newPassword
    ).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Password changed successfully!';
        this.passwordForm.reset();
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to change password.';
      }
    });
  }

  updateUsername(): void {
    if (this.usernameForm.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';

    this.authService.updateUsername(this.usernameForm.value.newUsername).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Username updated! Please re-login.';
        setTimeout(() => this.authService.logout(), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to update username.';
      }
    });
  }

  updateSecurityQuestion(): void {
    if (this.securityForm.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';

    this.authService.setSecurityQuestion(
      this.securityForm.value.securityQuestion,
      this.securityForm.value.securityAnswer
    ).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Security question updated!';
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to update security question.';
      }
    });
  }
}
