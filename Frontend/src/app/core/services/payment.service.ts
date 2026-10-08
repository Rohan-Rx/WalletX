import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PaymentOrder {
  id: string;
  entity: string;
  amount: number;
  amount_paid: number;
  amount_due: number;
  currency: string;
  receipt: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private baseUrl = 'http://localhost:8082/payment';

  constructor(private http: HttpClient) {}

  createOrder(walletId: string, amount: number): Observable<PaymentOrder> {

    return this.http.post<PaymentOrder>(
      `${this.baseUrl}/create-order`,
      {
        walletId: walletId,
        amount: amount
      }
    );
  }

  verifyPayment(data: any): Observable<any> {

    return this.http.post(
      `${this.baseUrl}/verify`,
      data
    );
  }
}