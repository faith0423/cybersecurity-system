import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  loginForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  get emailControl() { return this.loginForm.get('email')!; }
  get passwordControl() { return this.loginForm.get('password')!; }

  submit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
       this.errorMessage = '';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.http.post<any>('https://cybersecurity-system-production.up.railway.app/api/auth/login', this.loginForm.value)
      .subscribe({
        next: (response: any) => {
          // Save token, role and email to localStorage
          localStorage.setItem('token', response.token);
          localStorage.setItem('role', response.role);
          localStorage.setItem('email', response.email);

          console.log('Login success. Role:', response.role, 'Token saved:', !!response.token);

          this.loading = false;

          // Route by role
          if (response.role === 'ADMIN') {
            this.router.navigate(['/dashboard']);
          } else {
            this.router.navigate(['/incidents']);
          }
        },
        error: (error: any) => {
          console.error('Login failed:', error);
          this.loading = false;
          this.errorMessage = 'Invalid email or password.';
        }
      });
  }
}