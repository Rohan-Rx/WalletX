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

  loginForm = this.fb.group({
    email: [''],
    password: ['']
  });

  login() {

    const email = this.loginForm.value.email ?? '';
    const password = this.loginForm.value.password ?? '';

    this.authService.login(email, password).subscribe({

      next: (response) => {
        console.log('Login successful:', response);

        this.router.navigate(['/dashboard']);
      },

      error: (error) => {
        console.error('Login failed:', error);
      }

    });
  }
}