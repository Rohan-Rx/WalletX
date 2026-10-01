import { Component,inject } from '@angular/core';
import {FormBuilder} from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule,RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  fb = inject(FormBuilder);
  authService = inject(AuthService);
    registerForm = this.fb.group({
        username: [''],
        email: [''],
        phone: [''],
        password: ['']
    });

    

    register() {
        const formData = this.registerForm.value;
        console.log('Registering user:', formData);
        this.authService.register(formData).subscribe(
            (response) => {
                console.log('User registered successfully:', response);
            },
            (error) => {
                console.error('Error registering user:', error);
            }
        );
    }
}
