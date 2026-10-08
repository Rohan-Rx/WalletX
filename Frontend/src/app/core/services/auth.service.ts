import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { tap } from 'rxjs';

export interface User {
  userid: number;
  username: string;
  email: string;
  phone: string;
  role: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:8081/user';

  register(userData: any) {
    return this.http.post(`${this.apiUrl}/create`, userData);
  }

  login(email: string, password: string) {
    const params = new HttpParams()
      .set('email', email)
      .set('password', password);

    return this.http.post<User>(
      `${this.apiUrl}/login`,
      null,
      { params }
    ).pipe(
      tap(user => {
        const safeUser: User = {
          userid: user.userid,
          username: user.username,
          email: user.email,
          phone: user.phone,
          role: user.role,
          status: user.status
        };

        localStorage.setItem('loggedInUser', JSON.stringify(safeUser));
      })
    );
  }

  getCurrentUser(): User | null {
    const user = localStorage.getItem('loggedInUser');
    return user ? JSON.parse(user) as User : null;
  }

  logout() {
    localStorage.removeItem('loggedInUser');
  }
}