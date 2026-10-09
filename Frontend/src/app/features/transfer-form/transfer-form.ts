import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { WalletService, Wallet } from '../../core/services/wallet.service';
import {
  TransactionService,
  Transaction
} from '../../core/services/transaction.service';
import { AuthService } from '../../core/services/auth.service';
import { PaymentService } from '../../core/services/payment.service';

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
  private paymentService = inject(PaymentService);

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

  const receiverWalletId = this.receiverWalletId.trim();
  const amount = this.transferAmount;

  if (!receiverWalletId) {
    this.transferError = 'Please enter the recipient wallet ID.';
    return;
  }

  if (amount === null || !Number.isFinite(amount) || amount <= 0) {
    this.transferError = 'Please enter a valid amount.';
    return;
  }

  if (receiverWalletId === this.wallet.walletId) {
    this.transferError = 'You cannot transfer money to your own wallet.';
    return;
  }

  if (amount > this.wallet.balance) {
    this.transferError = 'Insufficient wallet balance.';
    return;
  }

  this.transferLoading = true;

  const request = {
    senderWalletId: this.wallet.walletId,
    receiverWalletId,
    amount
  };

  // Step 1: Create a Razorpay order on the backend.
  this.paymentService.createTransferOrder(request).subscribe({
    next: (order) => {
      console.log('Transfer Razorpay order:', order);

      const options = {
        key: 'rzp_test_TkuWlwDklAOGiN',
        amount: order.amount,
        currency: order.currency,
        name: 'WalletX',
        description: 'Wallet Transfer',
        order_id: order.id,

        handler: (response: any) => {
          console.log('Transfer payment response:', response);

          // Step 2: Verify the actual Razorpay response.
          this.verifyTransferPayment(response);
        },

        prefill: {
          name: this.authService.getCurrentUser()?.username || '',
          email: this.authService.getCurrentUser()?.email || ''
        },

        theme: {
          color: '#3399cc'
        },

        modal: {
          ondismiss: () => {
            this.transferLoading = false;
            this.transferError = 'Payment cancelled.';
          }
        }
      };

      try {
        const razorpay = new Razorpay(options);
        razorpay.open();
      } catch (error) {
        console.error('Unable to open Razorpay Checkout:', error);
        this.transferLoading = false;
        this.transferError = 'Unable to open payment window.';
      }
    },

    error: (err) => {
      console.error('Transfer order creation failed:', err);
      this.transferLoading = false;
      this.transferError =
        err.error?.message || 'Unable to create transfer order.';
    }
  });
}

verifyTransferPayment(response: any) {
  const verificationData = {
    razorpayOrderId: response.razorpay_order_id,
    razorpayPaymentId: response.razorpay_payment_id,
    razorpaySignature: response.razorpay_signature
  };

  if (
    !verificationData.razorpayOrderId ||
    !verificationData.razorpayPaymentId ||
    !verificationData.razorpaySignature
  ) {
    this.transferLoading = false;
    this.transferError = 'Payment response is incomplete. Please contact support if money was deducted.';
    return;
  }

  console.log('Transfer verification data:', verificationData);

  // Step 3: Ask the backend to verify the payment and execute the transfer.
  this.paymentService.verifyTransfer(verificationData).subscribe({
    
next: (result) => {
  console.log('Transfer verified successfully:', result);

  this.transferLoading = false;
  this.transferSuccess = true;

  this.receiverWalletId = '';
  this.transferAmount = null;

  // Refresh the sender's wallet balance.
  this.loadWallet();

  // Optional: notify other components to refresh their data.
  window.dispatchEvent(new CustomEvent('wallet-updated'));
},

    error: (err) => {
      console.error('Transfer verification failed:', err);
      console.error('Backend response:', err.error);

      this.transferLoading = false;
      this.transferError =
        err.error?.message ||
        'Payment verification or transfer failed. Check your payment status before retrying.';
    }
  });
}
}