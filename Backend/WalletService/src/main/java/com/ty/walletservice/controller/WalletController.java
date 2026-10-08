package com.ty.walletservice.controller;


import com.ty.walletservice.entity.Wallet;
import com.ty.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Receiver;
import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService service;

    @GetMapping("/getall")
    public ResponseEntity<List<Wallet>> getall(){
        List<Wallet> wallet=service.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(wallet);
    }
    @PostMapping("/createWallet")
    public ResponseEntity<Wallet> create(@RequestBody Wallet wallet){
        Wallet w= service.create(wallet);
        return ResponseEntity.status(HttpStatus.CREATED).body(w);
    }
    @GetMapping("/getByWalletId/{walletId}")
    public ResponseEntity<Wallet> getByWalletId(
            @PathVariable String walletId) {

        return ResponseEntity.ok(
                service.getWalletByWalletId(walletId)
        );
    }
    @PutMapping("/credit")
    public ResponseEntity<Wallet> credit(@RequestParam String walletId,@RequestParam BigDecimal amount){
        Wallet wallet = service.credit(walletId,amount);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wallet);

    }
    @PutMapping("/debit")
    public  ResponseEntity<Wallet> debit(@RequestParam String walletId,@RequestParam BigDecimal amount){
        Wallet wallet = service.debit(walletId, amount);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(wallet);
    }
    @GetMapping("/balance/{walletId}")
    public ResponseEntity<BigDecimal> checkBalance(@PathVariable String walletId){
        BigDecimal balance = service.getBalance(walletId);
        if(balance !=null){
            return ResponseEntity.ok(balance);
        }
        return ResponseEntity.notFound().build();
    }
    @PutMapping("/transfer")
    public ResponseEntity<Wallet> transfer(@RequestParam String SenderId,@RequestParam String ReceiverId,@RequestParam BigDecimal amount){
        Wallet w = service.Transfer(SenderId,ReceiverId,amount);
        return ResponseEntity.status(HttpStatus.OK).body(w);
    }


}
