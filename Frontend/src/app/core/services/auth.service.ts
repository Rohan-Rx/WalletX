import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private http = inject(HttpClient);

  register(userData: any) {
    return this.http.post(
      'http://localhost:8081/user/create',
      userData
    );
  }
  login(email: string, password: string) {

    const params = new HttpParams()
      .set('email', email)
      .set('password', password);

    return this.http.post(
      'http://localhost:8081/user/login',
      null,
      { params }
    );
  }
}