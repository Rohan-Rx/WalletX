package com.ty.walletservice.service;

import com.ty.walletservice.entity.Transaction;
import com.ty.walletservice.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    List<Transaction> getall();
    Transaction savetransaction(Transaction t);
    Transaction getTransactionById(String transactionId);
    List<Transaction> getWalletHistory(String walletId);
    List<Transaction> getTransactionByType(String type);
    List<Transaction> getTransactionByStatus(String status);
    Transaction transfer(
            String senderWalletId, String receiverWalletId, BigDecimal amount
    );
    Transaction topUp(String walletId, BigDecimal amount);

}
