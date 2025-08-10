import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm: FormGroup;
  error: string | null = null;
  success: string | null = null;
  loading = false;
  token: string | null = null;
  tokenValid = false;
  validatingToken = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.resetPasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (this.token) {
        this.validateToken();
      } else {
        this.error = 'Invalid reset link';
        this.validatingToken = false;
      }
    });
  }

  validateToken() {
    this.authService.validateResetToken(this.token!).subscribe({
      next: (res) => {
        this.tokenValid = true;
        this.validatingToken = false;
        console.log('Token valid! Should show form.', res);
      },
      error: (err) => {
        this.error = 'Invalid or expired reset link';
        this.validatingToken = false;
        console.log('Token invalid!', err);
      }
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    return null;
  }

  onSubmit() {
    if (this.resetPasswordForm.invalid || !this.token) return;
    
    this.loading = true;
    this.error = null;
    this.success = null;
    
    const resetData = {
      token: this.token,
      newPassword: this.resetPasswordForm.get('newPassword')?.value
    };
    
    this.authService.resetPassword(resetData).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.success = response || 'Password reset successfully';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err.error || 'Failed to reset password';
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
} 