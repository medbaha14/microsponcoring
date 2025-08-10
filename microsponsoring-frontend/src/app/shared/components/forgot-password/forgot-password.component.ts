import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  error: string | null = null;
  success: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.forgotPasswordForm.invalid) return;
    
    this.loading = true;
    this.error = null;
    this.success = null;
    
    this.authService.forgotPassword(this.forgotPasswordForm.value).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.success = response || 'Password reset email sent successfully';
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err.error || 'Failed to send password reset email';
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
} 