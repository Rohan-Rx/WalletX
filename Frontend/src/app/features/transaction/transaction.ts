import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService } from '../../core/services/wallet.service';
import { FormsModule } from '@angular/forms';

import {
  TransactionService,
  Transaction
} from '../../core/services/transaction.service';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-transaction',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './transaction.html',
  styleUrl: './transaction.css'
})
export class TransactionComponent implements OnInit {

 private transactionService = inject(TransactionService);
private authService = inject(AuthService);
private walletService = inject(WalletService);

  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];

  loading = true;

  searchText = '';
  selectedFilter = 'ALL';

  selectedTransaction: Transaction | null = null;

  currentUser: any;
  

 ngOnInit(): void {
  this.currentUser = this.authService.getCurrentUser();

  if (!this.currentUser) {
    this.loading = false;
    return;
  }

  this.loadTransactions();
}
openTransaction(transaction: Transaction): void {
  this.selectedTransaction = transaction;
}

isCredit(transaction: Transaction): boolean {
  return transaction.type.toUpperCase() === 'CREDIT';
}
loadTransactions(): void {
  const userId = this.currentUser.userid;

  this.walletService.getAllWallets().subscribe({
    next: (wallets) => {

      const userWallet = wallets.find(
        wallet => wallet.userid === userId
      );

      if (!userWallet) {
        console.error('No wallet found for user:', userId);
        this.loading = false;
        return;
      }

      console.log('User Wallet:', userWallet);

      this.transactionService
        .getWalletHistory(userWallet.walletId)
        .subscribe({
          next: (transactions) => {
            console.log('Transactions:', transactions);

            this.transactions = transactions;
            this.filteredTransactions = transactions;

            this.loading = false;
          },

          error: (error) => {
            console.error('Error loading transactions:', error);
            this.loading = false;
          }
        });
    },

    error: (error) => {
      console.error('Error loading wallets:', error);
      this.loading = false;
    }
  });
}
onSearch(): void {
  this.applyFilters();
}
closeTransaction(): void {
  this.selectedTransaction = null;
}

filterTransactions(filter: string): void {
  this.selectedFilter = filter;
  this.applyFilters();
}

applyFilters(): void {
  let result = [...this.transactions];

  // Filter by transaction type
  if (this.selectedFilter !== 'ALL') {
    result = result.filter(
      transaction =>
        transaction.type.toUpperCase() === this.selectedFilter
    );
  }

  // Search
  if (this.searchText.trim()) {
    const search = this.searchText.toLowerCase().trim();

    result = result.filter(transaction =>
      transaction.description?.toLowerCase().includes(search) ||
      transaction.transactionId?.toLowerCase().includes(search) ||
      transaction.type?.toLowerCase().includes(search) ||
      transaction.status?.toLowerCase().includes(search)
    );
  }

  this.filteredTransactions = result;
}


}