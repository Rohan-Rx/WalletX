package com.ty.walletservice.repository;

import com.ty.walletservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepo extends JpaRepository<Wallet,Integer> {
    Optional<Wallet> findByUserId(Integer userId);

    Optional<Wallet> findByWalletId(String walletId);

    Optional<Wallet> findByWalletNumber(String walletNumber);

    Wallet delete(Optional<Wallet> wallet);
}
