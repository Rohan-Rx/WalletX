import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService, Wallet } from '../../core/services/wallet.service';
import { AuthService, User } from '../../core/services/auth.service';
import { TransactionService, Transaction } from '../../core/services/transaction.service';
import { RouterLink } from '@angular/router';
import { TransferForm } from '../transfer-form/transfer-form';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { PaymentService } from '../../core/services/payment.service';





@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, TransferForm, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private walletService = inject(WalletService);
  private authService = inject(AuthService);
  private transactionService = inject(TransactionService);
  private cdr = inject(ChangeDetectorRef);
  private paymentService = inject(PaymentService);
  showTransferForm = false;
  currentUser: User | null = null;
  
  greeting = '';
  getGreeting(): string {
    const hour = new Date().getHours();

    if (hour < 12) {
      return 'Good morning';
    } else if (hour < 17) {
      return 'Good afternoon';
    } else {
      return 'Good evening';
    }
  }

  wallet: Wallet | null = null;
  loading = true;
  errorMessage = '';
  userId: number | null = null;

  transactions: Transaction[] = [];
  totalSent = 0;
  totalReceived = 0;

  hasWallet = false;
  creatingWallet = false;

  showWalletCreationForm = false;
  selectedCurrency = 'INR';
  selectedWalletType = 'PERSONAL';

  showAddMoneyForm = false;
  addingMoney = false;
  addMoneyAmount: number | null = null;

  paymentStep: 'amount' | 'confirm' | 'processing' | 'success' = 'amount';
  paymentReference = '';


  openAddMoneyForm() {
    this.addMoneyAmount = null;
    this.paymentStep = 'amount';
    this.paymentReference = '';
    this.showAddMoneyForm = true;
  }
  continueToPayment() {
    if (!this.addMoneyAmount || this.addMoneyAmount <= 0) {
      alert('Please enter a valid amount.');
      return;
    }

    this.paymentStep = 'confirm';
  }
  addMoney() {

  if (!this.wallet || !this.addMoneyAmount || this.addMoneyAmount <= 0) {
    return;
  }

  this.addingMoney = true;
  this.paymentStep = 'processing';

  const walletId = this.wallet.walletId;
  const amount = this.addMoneyAmount;

  this.transactionService
    .topUp(walletId, amount)
    .subscribe({

      next: (transaction: Transaction) => {

        console.log('Top-up successful:', transaction);

        // Save payment reference
        this.paymentReference = transaction.transactionId;

        // Refresh wallet
        this.walletService
          .getWalletById(walletId)
          .subscribe({

            next: (updatedWallet: Wallet) => {

              console.log('Updated wallet:', updatedWallet);

              // Update dashboard wallet
              this.wallet = updatedWallet;

              // Refresh transaction history
              this.loadTransactions(walletId);

              // Stop processing
              this.addingMoney = false;

              // Show success screen
              this.paymentStep = 'success';
              this.cdr.detectChanges();
            },

            error: (err: HttpErrorResponse) => {

              console.error('Wallet refresh failed:', err);

              this.addingMoney = false;
              this.paymentStep = 'confirm';

              alert(
                'Payment was successful, but the wallet balance could not be refreshed.'
              );
            }
          });
      },

      error: (err: HttpErrorResponse) => {

        console.error('Top-up failed:', err);

        this.addingMoney = false;
        this.paymentStep = 'confirm';

        if (err.status === 400) {
          alert('Invalid payment amount.');
        } else if (err.status === 404) {
          alert('Wallet not found.');
        } else if (err.status === 0) {
          alert('Unable to connect to the payment service.');
        } else {
          alert('Payment failed. Please try again.');
        }
      }
    });
}
  finishPayment() {
  this.showAddMoneyForm = false;
  this.paymentStep = 'amount';
  this.addMoneyAmount = null;
  this.paymentReference = '';
}
payWithRazorpay() {

  if (!this.wallet || !this.addMoneyAmount) {
    alert('Wallet or amount is missing.');
    return;
  }

  this.addingMoney = true;
  this.paymentStep = 'processing';

  const walletId = this.wallet.walletId;
  const amount = this.addMoneyAmount;

  this.paymentService.createOrder(walletId, amount).subscribe({

    next: (order) => {

      console.log('Razorpay Order:', order);

      const options = {
        key: 'rzp_test_TkuWlwDklAOGiN',

        amount: order.amount,
        currency: order.currency,

        name: 'WalletX',
        description: 'Wallet Top-up',

        order_id: order.id,

        handler: (response: any) => {

          console.log('Razorpay Payment Response:', response);

          this.verifyRazorpayPayment(
            walletId,
            amount,
            response
          );
        },

        prefill: {
          name: this.currentUser?.username || '',
          email: this.currentUser?.email || ''
        },

        theme: {
          color: '#3399cc'
        },

        modal: {
          ondismiss: () => {
            this.addingMoney = false;
            this.paymentStep = 'confirm';
          }
        }
      };

      const razorpay = new Razorpay(options);

      razorpay.open();
    },

    error: (error) => {

      console.error('Order creation failed:', error);

      this.addingMoney = false;
      this.paymentStep = 'confirm';

      alert('Unable to create payment order.');
    }
  });
}
verifyRazorpayPayment(
  walletId: string,
  amount: number,
  response: any
) {

  const verificationData = {
    walletId: walletId,
    amount: amount,
    razorpayOrderId: response.razorpay_order_id,
    razorpayPaymentId: response.razorpay_payment_id,
    razorpaySignature: response.razorpay_signature
  };

  console.log('Verification Data:', verificationData);

  this.paymentService.verifyPayment(
    verificationData
  ).subscribe({

    next: (result) => {
      console.log('Payment verification successful:', result);

      this.paymentReference =
        response.razorpay_payment_id;

      this.addingMoney = false;
      this.paymentStep = 'success';

      this.walletService
        .getWalletById(walletId)
        .subscribe(updatedWallet => {
          this.wallet = updatedWallet;
          this.loadTransactions(walletId);
        });
    },

    error: (error) => {
      console.error('Payment verification failed:', error);
      console.error('Backend response:', error.error);

      this.addingMoney = false;
      this.paymentStep = 'confirm';

      alert('Payment verification failed.');
    }
  });
}

  closeAddMoneyForm() {
    if (!this.addingMoney) {
      this.showAddMoneyForm = false;
    }
  }



  ngOnInit() {
    const user = this.authService.getCurrentUser();
    this.greeting = this.getGreeting();

    console.log('Logged-in user:', user);

    if (!user) {
      this.errorMessage = 'Please log in to view your wallet.';
      this.loading = false;
      return;
    }

    this.currentUser = user;

    this.userId = user.userid;

    console.log('Dashboard user ID:', this.userId);

    this.loadWallet();
  }
  loadTransactions(walletId: string) {
    this.transactionService.getWalletHistory(walletId).subscribe({
      next: (data) => {
        console.log('Transaction history:', data);

        this.transactions = data.sort(
          (a, b) =>
            new Date(b.timestamp).getTime() -
            new Date(a.timestamp).getTime()
        );

        this.totalSent = data
          .filter(t => t.type === 'DEBIT' && t.status === 'SUCCESS')
          .reduce((sum, t) => sum + t.amount, 0);

        this.totalReceived = data
          .filter(t => t.type === 'CREDIT' && t.status === 'SUCCESS')
          .reduce((sum, t) => sum + t.amount, 0);
        this.cdr.detectChanges();

        console.log('Transactions assigned:', this.transactions);
      },
      error: (err) => {
        console.error('Transaction error:', err);
      }
    });
  }
  openWalletCreationForm() {
    this.showWalletCreationForm = true;
  }
  closeWalletCreationForm() {
    if (!this.creatingWallet) {
      this.showWalletCreationForm = false;
    }
  }
  createWallet() {
    if (this.userId === null) {
      alert('Unable to identify the logged-in user.');
      return;
    }

    this.creatingWallet = true;

    console.log('Creating wallet for user:', this.userId);

    this.walletService.createWallet(this.userId).subscribe({
      next: (response) => {
        console.log('Wallet created successfully:', response);

        this.creatingWallet = false;
        this.showWalletCreationForm = false;

        alert('Your WalletX wallet has been created successfully!');

        this.loadWallet();
      },

      error: (err) => {
        console.error('Wallet creation failed:', err);

        this.creatingWallet = false;

        if (err.status === 409) {
          alert('You already have a wallet.');
          this.showWalletCreationForm = false;
          this.loadWallet();
        } else if (err.status === 0) {
          alert('Unable to connect to the wallet server.');
        } else {
          alert('Unable to create your wallet. Please try again.');
        }
      }
    });
  }
  loadWallet() {
    if (this.userId === null) {
      this.loading = false;
      return;
    }

    this.loading = true;

    this.walletService.getAllWallets().subscribe({
      next: (wallets) => {
        console.log('Wallet response:', wallets);

        const selectedWallet = wallets.find(
          (item) => Number(item.userid) === Number(this.userId)
        ) ?? null;

        this.wallet = selectedWallet;

        if (selectedWallet) {
          this.wallet = selectedWallet;
          this.hasWallet = true;

          this.loadTransactions(selectedWallet.walletId);
        } else {
          this.wallet = null;
          this.hasWallet = false;
          this.transactions = [];
          this.totalSent = 0;
          this.totalReceived = 0;
        }

        this.loading = false;
        this.cdr.detectChanges();

        console.log('Final wallet:', this.wallet);
        console.log('Final loading state:', this.loading);
      },
      error: (err) => {
        console.error('Wallet API error:', err);
        this.errorMessage = 'Unable to load wallet details.';
        this.loading = false;
      }
    });
  }

}