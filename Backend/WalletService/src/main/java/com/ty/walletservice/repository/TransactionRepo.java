package com.ty.walletservice.repository;

import com.ty.walletservice.entity.Transaction;
import com.ty.walletservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Integer> {

    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByWalletIdOrderByTimestampDesc(String walletId);
    List<Transaction> findByType(String type);
    List<Transaction> findByStatus(String status);
}