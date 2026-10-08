import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";


export interface Transaction {
    id?: number;
    transactionId: string;
    walletId: string;
    type: string;
    amount: number;
    status: string;
    description: string;
    timestamp: string;
}
export interface TransferRequest {
    senderWalletId: string;
    receiverWalletId: string;
    amount: number;
}

@Injectable({
    providedIn: 'root'
})
export class TransactionService {
    private http = inject(HttpClient);
    private apiUrl = 'http://localhost:8082/transaction';

    getAllTransaction(): Observable<Transaction[]> {
        return this.http.get<Transaction[]>(this.apiUrl);
    }
    getWalletHistory(walletId: string): Observable<Transaction[]> {
        return this.http.get<Transaction[]>(`${this.apiUrl}/getHistory/${walletId}`);
    }
    transfer(request: TransferRequest) {
        return this.http.post<Transaction>(
            'http://localhost:8082/transaction/transfer',
            request
        );
    }
    topUp(walletId: string, amount: number): Observable<Transaction> {
        return this.http.post<Transaction>(
            'http://localhost:8082/transaction/topup',
            {
                walletId: walletId,
                amount: amount
            }
        );
    }
}