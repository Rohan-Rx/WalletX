import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {

  fb = inject(FormBuilder);
  authService = inject(AuthService);

  registerForm = this.fb.group({
    username: ['', [
      Validators.required,
      Validators.minLength(3)
    ]],

    email: ['', [
      Validators.required,
      Validators.email
    ]],

    phone: ['', [
      Validators.required,
      Validators.pattern(/^[6-9][0-9]{9}$/)
    ]],

    password: ['', [
      Validators.required,
      Validators.minLength(8)
    ]]
  });

  register() {

    // Stop if form is invalid
    if (this.registerForm.invalid) {

      this.registerForm.markAllAsTouched();

      alert('Please enter valid registration details.');

      return;
    }

    const formData = this.registerForm.value;

    console.log('Registering user:', formData);

    this.authService.register(formData).subscribe({

      next: (response) => {

        console.log('User registered successfully:', response);

        alert('Account created successfully!');

        this.registerForm.reset();
      },

      error: (error) => {

        console.error('Error registering user:', error);

        if (error.status === 400) {
          alert('Invalid registration details.');
        }
        else if (error.status === 409) {
            alert(error.error?.message || 'Email or phone number already exists.');
        }
        else if (error.status === 0) {
          alert('Unable to connect to the server.');
        }
        else {
          alert('Registration failed. Please try again.');
        }
      }

    });
  }
}