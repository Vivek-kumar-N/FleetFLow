import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  step = 1; // 1: enter username, 2: answer question, 3: new password
  usernameForm: FormGroup;
  answerForm: FormGroup;
  passwordForm: FormGroup;
  securityQuestion = '';
  loading = false;
  error = '';
  success = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.usernameForm = this.fb.group({ username: ['', Validators.required] });
    this.answerForm = this.fb.group({ securityAnswer: ['', Validators.required] });
    this.passwordForm = this.fb.group({ newPassword: ['', [Validators.required, Validators.minLength(6)]] });
  }

  getQuestion(): void {
    if (this.usernameForm.invalid) return;
    this.loading = true;
    this.error = '';
    const username = this.usernameForm.value.username;

    this.authService.getSecurityQuestionByUsername(username).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.securityQuestion = res.securityQuestion;
        this.step = 2;
      },
      error: () => {
        this.loading = false;
        this.error = 'Username not found or no security question set.';
      }
    });
  }

  verifyAnswer(): void {
    if (this.answerForm.invalid) return;
    this.loading = true;
    this.error = '';

    this.authService.verifySecurityAnswer(
      this.usernameForm.value.username,
      this.answerForm.value.securityAnswer
    ).subscribe({
      next: (res: any) => {
        this.loading = false;
        if (res.correct) {
          this.step = 3;
        } else {
          this.error = 'Incorrect answer. Please try again.';
        }
      },
      error: () => {
        this.loading = false;
        this.error = 'Verification failed.';
      }
    });
  }

  resetPassword(): void {
    if (this.passwordForm.invalid) return;
    this.loading = true;
    this.error = '';

    this.authService.resetPassword(
      this.usernameForm.value.username,
      this.answerForm.value.securityAnswer,
      this.passwordForm.value.newPassword
    ).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Password reset successfully! Redirecting to login...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: () => {
        this.loading = false;
        this.error = 'Password reset failed.';
      }
    });
  }
}
