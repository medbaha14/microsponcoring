import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { TokenHandler } from '../../../services/token-handler';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) return;
    this.authService.login(this.loginForm.value).subscribe({
      next: (res: any) => {
        console.log(res);
        TokenHandler.saveToken(res.token);
        TokenHandler.saveUser(res.user);
        const user = res.user;
        if (user.userType === 'ADMIN') {
          this.router.navigate(['/dashboard/admin']);
        } else if (user.userType === 'SPONSOR') {
          this.router.navigate(['/dashboard/sponsor']);
        } else if (user.userType === 'ORGANISATION_NONPROFIT') {
          this.router.navigate(['/dashboard/organisation']);
        } else {
          this.router.navigate(['/login']);
        }
      },
      error: err => {
        this.error = err.error || 'Login failed';
      }
    });
  }
} 