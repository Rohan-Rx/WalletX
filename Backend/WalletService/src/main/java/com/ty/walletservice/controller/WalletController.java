package com.ty.walletservice.controller;


import com.ty.walletservice.entity.Wallet;
import com.ty.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService service;

    @GetMapping("/getall")
    private ResponseEntity<List<Wallet>> getall(){
        List<Wallet> wallet=service.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(wallet);
    }
    @PostMapping("/createWallet")
    private ResponseEntity<Wallet> create(Wallet wallet){
        Wallet w= service.create(wallet);
        return ResponseEntity.status(HttpStatus.CREATED).body(w);
    }
    @GetMapping("/getbyWalletId/{id}")
    private ResponseEntity<Wallet> getbywalletId(String Id){
        Wallet wallet = service.getWalletByWalletId(Id);
        return ResponseEntity.status(HttpStatus.OK).body(wallet);
    }
    @PutMapping("/credit")
    private ResponseEntity<Wallet> credit(@RequestParam String walletId,@RequestParam BigDecimal amount){
        Wallet wallet = service.credit(walletId,amount);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wallet);

    }
    @PutMapping("/debit")
    private  ResponseEntity<Wallet> debit(@RequestParam String walletId,@RequestParam BigDecimal amount){
        Wallet wallet = service.debit(walletId, amount);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wallet);
    }
    @GetMapping("/balance/{walletId}")
    private ResponseEntity<BigDecimal> checkBalance(@PathVariable String walletId){
        BigDecimal balance = service.getBalance(walletId);
        if(balance !=null){
            return ResponseEntity.ok(balance);
        }
        return ResponseEntity.notFound().build();
    }


}
