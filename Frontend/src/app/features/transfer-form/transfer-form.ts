import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { WalletService, Wallet } from '../../core/services/wallet.service';
import {
  TransactionService,
  Transaction
} from '../../core/services/transaction.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-transfer-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transfer-form.html',
  styleUrl: './transfer-form.css',
})
export class TransferForm {

  private walletService = inject(WalletService);
  private transactionService = inject(TransactionService);
  private authService = inject(AuthService);

  wallet: Wallet | null = null;

  receiverWalletId = '';
  transferAmount: number | null = null;

  transferLoading = false;
  transferError = '';
  transferSuccess = false;

  ngOnInit() {
    this.loadWallet();
  }

  loadWallet() {
    const user = this.authService.getCurrentUser();

    if (!user) {
      this.transferError = 'Please log in first.';
      return;
    }

    this.walletService.getAllWallets().subscribe({
      next: (wallets) => {

        this.wallet = wallets.find(
          wallet => wallet.userid === user.userid
        ) ?? null;

        if (!this.wallet) {
          this.transferError = 'Wallet not found.';
        }

        console.log('Transfer form wallet:', this.wallet);
      },

      error: (err) => {
        console.error('Wallet loading error:', err);
        this.transferError = 'Unable to load wallet.';
      }
    });
  }

  sendMoney() {

    this.transferError = '';
    this.transferSuccess = false;

    if (!this.wallet) {
      this.transferError = 'Your wallet is not available.';
      return;
    }

    if (!this.receiverWalletId.trim()) {
      this.transferError = 'Please enter the recipient wallet ID.';
      return;
    }

    if (!this.transferAmount || this.transferAmount <= 0) {
      this.transferError = 'Please enter a valid amount.';
      return;
    }

    if (this.receiverWalletId.trim() === this.wallet.walletId) {
      this.transferError =
        'You cannot transfer money to your own wallet.';
      return;
    }

    if (this.transferAmount > this.wallet.balance) {
      this.transferError =
        'Insufficient wallet balance.';
      return;
    }

    this.transferLoading = true;

    const request = {
      senderWalletId: this.wallet.walletId,
      receiverWalletId: this.receiverWalletId.trim(),
      amount: this.transferAmount
    };

    console.log('Transfer request:', request);

    this.transactionService.transfer(request).subscribe({

      next: (transaction: Transaction) => {

        console.log('Transfer successful:', transaction);

        this.transferLoading = false;
        this.transferSuccess = true;

        this.receiverWalletId = '';
        this.transferAmount = null;

        // Refresh wallet balance
        this.loadWallet();
      },

      error: (err) => {

        console.error('Transfer failed:', err);

        this.transferLoading = false;

        this.transferError =
          err.error?.message ||
          'Transfer failed. Please try again.';
      }
    });
  }
}