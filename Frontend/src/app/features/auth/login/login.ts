import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';


@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  fb = inject(FormBuilder);
  authService = inject(AuthService);
  router = inject(Router);

  loginError = '';
  isLoading = false;

  

  loginForm = this.fb.group({
    email: [''],
    password: ['']
  });

 login() {
  this.isLoading = true;

  const email = this.loginForm.value.email ?? '';
  const password = this.loginForm.value.password ?? '';

  if (!email || !password) {
    this.isLoading = false;
    alert('Please enter your email and password.');
    return;
  }

  this.authService.login(email, password).subscribe({

    next: (response) => {
      this.isLoading = false;

      alert('Login successful!');

      this.router.navigate(['/dashboard']);
    },

    error: (error) => {
      console.error('Login failed:', error);

      this.isLoading = false;

      if (error.status === 401 || error.status === 403) {
        alert('Invalid email or password.');
      } 
      else if (error.status === 404) {
        alert('Login service not found.');
      } 
      else if (error.status === 0) {
        alert('Unable to connect to the server.');
      } 
      else {
        alert('Login failed. Please try again.');
      }
    }

  });
}
}