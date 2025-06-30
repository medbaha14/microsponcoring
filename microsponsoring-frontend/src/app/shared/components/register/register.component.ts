import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      username: ['', Validators.required],
      password: ['', Validators.required],
      fullName: [''],
      userType: ['ORGANISATION_NONPROFIT', Validators.required]
    });
  }

  onSubmit() {
    if (this.registerForm.invalid) return;
    console.log(this.registerForm.value);
    
    this.authService.register(this.registerForm.value).subscribe({
      next: (res: any) => {
        console.log(res);
        
        this.router.navigate(['/login']);
      },
      error: err => {
        this.error = err.error || 'Registration failed';
      }
    });
  }
} 