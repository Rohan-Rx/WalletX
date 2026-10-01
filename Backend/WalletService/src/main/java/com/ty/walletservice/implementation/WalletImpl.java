package com.ty.walletservice.implementation;

import com.ty.walletservice.entity.Wallet;
import com.ty.walletservice.repository.WalletRepo;
import com.ty.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WalletImpl implements WalletService {
    @Autowired
    private WalletRepo repo;
    
    @Override
    public Wallet create(Wallet wallet) {
        return repo.save(wallet);
    }

    @Override
    public List<Wallet> getAll() {
        return repo.findAll();
    }

    @Override
    public Wallet getWalletById(Integer id) {
        return repo.findById(id).get();
    }

    @Override
    public Wallet getWalletByWalletId(String walletId) {
        return repo.findByWalletNumber(walletId).get();
    }

    @Override
    public Wallet delete(Integer id) {

        Optional<Wallet> wallet = repo.findById(id);

        if (wallet.isPresent()) {
            Wallet w = wallet.get();
            repo.delete(w);
            return w;
        }

        return null;
    }

    @Override
    public Wallet credit(String walletId, BigDecimal ammount) {
        Optional<Wallet> wallet = repo.findByWalletId(walletId);
        if(wallet.isPresent()){
            Wallet w=wallet.get();
            w.setBalance(w.getBalance().add(ammount));
            repo.save(w);
        }
        return null;
    }

    @Override
    public Wallet debit(String walletId, BigDecimal ammount) {
        Optional<Wallet> wallet=repo.findByWalletId(walletId);
        if(wallet.isPresent()){
            Wallet w=wallet.get();
            if(ammount.compareTo(w.getBalance()) <= 0) {
                w.setBalance(w.getBalance().subtract(ammount));
                repo.save(w);
            }
        }
        return null;
    }

    @Override
    public BigDecimal getBalance(String walletId) {
        Optional<Wallet> wallet = repo.findByWalletId(walletId);

        if (wallet.isPresent()) {
            Wallet w = wallet.get();
            return w.getBalance();
        }

        return null;
    }

    @Override
    public Wallet Transfer(String senderWalletId, String ReceiverWalletId, BigDecimal ammount) {
        return null;
    }
}
