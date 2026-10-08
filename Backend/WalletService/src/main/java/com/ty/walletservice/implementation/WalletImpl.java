package com.ty.walletservice.implementation;

import com.ty.walletservice.entity.Wallet;
import com.ty.walletservice.exception.InsufficientBalanceException;
import com.ty.walletservice.exception.InvalidAmountException;
import com.ty.walletservice.exception.WalletNotFoundException;
import com.ty.walletservice.repository.WalletRepo;
import com.ty.walletservice.service.WalletNumberGenerator;
import com.ty.walletservice.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletImpl implements WalletService {
    @Autowired
    private WalletRepo repo;
    @Autowired
    private WalletNumberGenerator walletNumberGenerator;

        @Override
        @Transactional
        public Wallet create(Wallet wallet) {

            if (wallet == null) {
                throw new IllegalArgumentException("Wallet cannot be null");
            }

            if (wallet.getUserid() == null) {
                throw new IllegalArgumentException("UserID cannot be null");
            }

            if (repo.existsByUserid(wallet.getUserid())) {
                throw new IllegalArgumentException(
                        "User already has a wallet"
                );
            }

            wallet.setBalance(BigDecimal.ZERO);

            if (wallet.getCurrency() == null) {
                wallet.setCurrency("INR");
            }

            if (wallet.getStatus() == null) {
                wallet.setStatus("ACTIVE");
            }

            String walletNumber =
                    walletNumberGenerator.generateWalletNumber();

            wallet.setWalletId(walletNumber);

            return repo.save(wallet);
        }

    @Override
    public List<Wallet> getAll() {
        return repo.findAll();
    }

    @Override
    public Wallet getWalletById(Integer id) {

        return repo.findById(id).orElseThrow(()->
                new WalletNotFoundException("Wallet Not Found"+id));
    }

    @Override
    public Wallet getWalletByWalletId(String walletId) {

        return repo.findByWalletId(walletId).get();
    }

    @Override
    public Wallet delete(Integer id) {

        Wallet wallet = repo.findById(id).orElseThrow(()->
                new WalletNotFoundException("Wallet Not Found"+id));

        repo.delete(wallet);

        return wallet;
    }

    @Override
    public Wallet credit(String walletId, BigDecimal amount) {
        Wallet wallet = repo.findByWalletId(walletId).orElseThrow(()->
                new WalletNotFoundException("Wallet Not Found"+walletId));
        if(amount==null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        wallet.setBalance(wallet.getBalance().add(amount));
        return repo.save(wallet);
    }

    @Override
    public Wallet debit(String walletId, BigDecimal amount) {
        Wallet wallet=repo.findByWalletId(walletId).orElseThrow(()->
                new WalletNotFoundException("Wallet Not Found"+walletId));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (amount.compareTo(wallet.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        return repo.save(wallet);
    }

    @Override
    public BigDecimal getBalance(String walletId) {
        Wallet wallet = repo.findByWalletId(walletId).orElseThrow(()->
                new WalletNotFoundException("Wallet Not Found"+walletId));

        return wallet.getBalance();
    }

    @Override
    @Transactional
    public Wallet Transfer(String senderWalletId, String ReceiverWalletId, BigDecimal amount) {
        if(amount==null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("Amount Must be Greater Than Zero");
        }
        if(senderWalletId.equals(ReceiverWalletId)){
            throw new IllegalArgumentException("Sender and Receiver Wallet must be different");
        }
        Wallet sender = repo.findByWalletId(senderWalletId).orElseThrow(()->
                new WalletNotFoundException("Sender Wallet Not found "+senderWalletId));
        Wallet receiver = repo.findByWalletId(ReceiverWalletId).orElseThrow(()->
                new WalletNotFoundException("Receiver Wallet not found "+ReceiverWalletId));
        if(amount.compareTo(sender.getBalance()) > 0){
            throw new InsufficientBalanceException("Insufficient Wallet Balance" );
        }
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        repo.save(sender);
        repo.save(receiver);
        return sender;
    }
}
