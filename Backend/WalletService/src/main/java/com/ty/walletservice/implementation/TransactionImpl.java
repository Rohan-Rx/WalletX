package com.ty.walletservice.implementation;

import com.ty.walletservice.entity.Transaction;
import com.ty.walletservice.entity.Wallet;
import com.ty.walletservice.exception.InvalidAmountException;
import com.ty.walletservice.repository.TransactionRepo;
import com.ty.walletservice.service.TransactionService;
import com.ty.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionImpl implements TransactionService {
    @Autowired
    private TransactionRepo repo;
    @Autowired
    private WalletService walletService;

    @Override
    public List<Transaction> getall() {
        return repo.findAll();
    }

    @Override
    public Transaction savetransaction(Transaction t) {
        return repo.save(t);
    }

    @Override
    public Transaction getTransactionById(String transactionId) {
        return repo.findByTransactionId(transactionId).orElseThrow(()->
                new RuntimeException("Transaction Not Found"+transactionId));
    }

    @Override
    public List<Transaction> getWalletHistory(String walletId) {
        return repo.findByWalletIdOrderByTimestampDesc(walletId);
    }

    @Override
    public List<Transaction> getTransactionByType(String type) {
        return repo.findByType(type);
    }

    @Override
    public List<Transaction> getTransactionByStatus(String status) {
        return repo.findByStatus(status);
    }

    @Override
    @Transactional
    public Transaction transfer(String senderWalletId, String receiverWalletId, BigDecimal amount) {
        if(amount==null || amount.compareTo(BigDecimal.ZERO)<=0){
            throw new InvalidAmountException("Amount Must be greater than 0");
        }
        if(senderWalletId ==null || receiverWalletId==null){
            throw new IllegalArgumentException("Wallet Id cannot be Null");
        }
        if(senderWalletId.equals(receiverWalletId)){
            throw new IllegalArgumentException("Sender And Receiver Must be Different");

        }
        walletService.Transfer(senderWalletId,receiverWalletId,amount);
        String reference = UUID.randomUUID().toString();
        //DEBIT RECORD
        Transaction debit = new Transaction();
        debit.setWalletId(senderWalletId);
        debit.setType("DEBIT");
        debit.setStatus("SUCCESS");
        debit.setAmount(amount);
        debit.setDescription("Transfer Reference "+reference);

        //CREDIT RECORD
        Transaction credit = new Transaction();
        credit.setWalletId(receiverWalletId);
        credit.setType("CREDIT");
        credit.setStatus("SUCCESS");
        credit.setAmount(amount);
        credit.setDescription("Transfer Reference "+reference);

        repo.save(debit);
        repo.save(credit);
        return debit;
    }

    @Override
    @Transactional
    public Transaction topUp(String walletId, BigDecimal amount) {

        // 1. Validate wallet ID
        if (walletId == null || walletId.trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet ID cannot be null or empty");
        }

        // 2. Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0");
        }

        // 3. Simulate successful payment
        String paymentReference = UUID.randomUUID().toString();

        // 4. Credit the wallet only after payment success
        walletService.credit(walletId, amount);

        // 5. Create CREDIT transaction
        Transaction transaction = new Transaction();

        transaction.setWalletId(walletId);
        transaction.setType("CREDIT");
        transaction.setAmount(amount);
        transaction.setStatus("SUCCESS");
        transaction.setDescription(
                "Wallet Top-up | Payment Reference " + paymentReference
        );

        // 6. Save transaction
        return repo.save(transaction);
    }
}
