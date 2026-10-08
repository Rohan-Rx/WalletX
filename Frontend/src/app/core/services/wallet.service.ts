import { Injectable,inject } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Wallet {
  id?: number;
  userid: number;
  walletId: string;
  balance: number;
  currency: string;
  status: string;
  created_at?: string;
  updated_at?: string;
}
@Injectable({
    providedIn:'root'
})

export class WalletService{
    private http = inject(HttpClient);
    private apiUrl = 'http://localhost:8082/wallet';

    getAllWallets(): Observable<Wallet[]>{
        return this.http.get<Wallet[]>(`${this.apiUrl}/getall`);
    }
    createWallet(userid:number): Observable<Wallet>{
        const Wallet = { userid: userid };
        return this.http.post<Wallet>(`${this.apiUrl}/createWallet`,Wallet);
    }
    getWalletById(walletId:string):Observable<Wallet>{
        return this.http.get<Wallet>(`${this.apiUrl}/getByWalletId/${walletId}`);
    } 
    getBalance(walletId:string): Observable<Wallet>{
        return this.http.get<Wallet>(`${this.apiUrl}/balance/${walletId}`);
    }
    credit(walletId:string,amount:number): Observable<Wallet>{
        const params = new HttpParams()
        .set('walletId',walletId)
        .set('amount',amount);

        return this.http.put<Wallet>(`${this.apiUrl}/credit`,null,{params});
    }
    debit(walletId:string,amount:number): Observable<Wallet>{
        const params = new HttpParams()
        .set('walletId',walletId)
        .set('amount',amount);
        return this.http.put<Wallet>(`${this.apiUrl}/debit`,null,{params});
    }
   transfer(
    senderId: string,
    receiverId: string,
    amount: number
  ): Observable<Wallet> {
    const params = new HttpParams()
      .set('SenderId', senderId)
      .set('ReceiverId', receiverId)
      .set('amount', amount);

    return this.http.put<Wallet>(
      `${this.apiUrl}/transfer`,
      null,
      { params }
    );
  }
  addMoney(walletId: string, amount: number) {
  return this.http.put<Wallet>(
    'http://localhost:8082/wallet/credit',
    null,
    {
      params: {
        walletId: walletId,
        amount: amount.toString()
      }
    }
  );
}

}