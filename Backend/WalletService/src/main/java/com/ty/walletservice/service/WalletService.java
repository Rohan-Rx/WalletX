package com.ty.walletservice.service;

import com.ty.walletservice.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    Wallet create(Wallet wallet);
    List<Wallet> getAll();
    Wallet getWalletById(Integer id);
    Wallet getWalletByWalletId(String walletId);
    Wallet delete(Integer id);
    Wallet credit(String walletId, BigDecimal amount);
    Wallet debit(String walletId, BigDecimal amount);
    BigDecimal getBalance(String walletId);
    Wallet Transfer(String senderWalletId,String ReceiverWalletId,BigDecimal amount);

}
