import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Router,
  NavigationEnd,
  RouterLink,
  RouterLinkActive
} from '@angular/router';
import { filter } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import {
  WalletService,
  Wallet
} from '../../../core/services/wallet.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent implements OnInit {

  private authService = inject(AuthService);
  private walletService = inject(WalletService);
  private router = inject(Router);

  currentUser: any = null;
  wallet: Wallet | null = null;

  currentPage = 'Dashboard';

  ngOnInit(): void {

    this.currentUser = this.authService.getCurrentUser();

    this.loadWallet();

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd)
      )
      .subscribe(() => {
        this.updateCurrentPage();
      });

    this.updateCurrentPage();
  }

  loadWallet(): void {

    if (!this.currentUser) {
      return;
    }

    this.walletService.getAllWallets().subscribe({

      next: (wallets) => {

        this.wallet =
          wallets.find(
            wallet => wallet.userid === this.currentUser.userid
          ) || null;

      },

      error: (error) => {
        console.error(
          'Error loading navbar wallet:',
          error
        );
      }

    });
  }

  updateCurrentPage(): void {

    const url = this.router.url;

    if (url === '/dashboard' || url === '/') {

      this.currentPage = 'Dashboard';

    } else if (url.startsWith('/wallet')) {

      this.currentPage = 'My Wallet';

    } else if (url.startsWith('/transfer')) {

      this.currentPage = 'Send Money';

    } else if (url.startsWith('/transaction')) {

      this.currentPage = 'Transactions';

    } else {

      this.currentPage = 'Dashboard';

    }
  }
}