import { Routes } from '@angular/router';
import { Register } from './features/auth/register/register';
import { Login } from './features/auth/login/login';
import { Dashboard } from './features/dashboard/dashboard';

export const routes: Routes = [
  { path: 'register', component: Register },
  { path: 'login', component: Login },
  {path:'dashboard',
    component:Dashboard
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];
