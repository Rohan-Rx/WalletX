import { Routes } from '@angular/router';
import { Register } from './features/auth/register/register';
import { Login } from './features/auth/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { TransferForm } from './features/transfer-form/transfer-form';
import { About } from './features/about/about';

export const routes: Routes = [
  { path: 'register', component: Register },
  { path: 'login', component: Login },
  { path: 'dashboard', component: Dashboard },

 { 
  path: 'transfer', 
  component: TransferForm 
},
 {
    path: 'about',
    component: About
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];