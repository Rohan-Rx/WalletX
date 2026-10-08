package com.ty.walletservice.controller;

import com.ty.walletservice.entity.TopUpRequest;
import com.ty.walletservice.entity.Transaction;
import com.ty.walletservice.entity.TransferRequest;
import com.ty.walletservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@CrossOrigin("http://localhost:4200")
public class TransactionController {
   @Autowired
    private TransactionService service;

   @GetMapping
    public ResponseEntity<List<Transaction>> getall(){
       List<Transaction> tr = service.getall();
       return ResponseEntity.status(HttpStatus.OK).body(tr);
   }
   @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction){
       Transaction t = service.savetransaction(transaction);
       return ResponseEntity.status(HttpStatus.CREATED).body(t);
   }
   @GetMapping("/getById/{transactionId}")
    public ResponseEntity<Transaction> getById(@PathVariable String transactionId){
       Transaction t = service.getTransactionById(transactionId);
       return ResponseEntity.status(HttpStatus.OK).body(t);
   }
   @GetMapping("/getHistory/{walletId}")
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable String walletId){
       List<Transaction> history = service.getWalletHistory(walletId);
       return ResponseEntity.status(HttpStatus.OK).body(history);

   }
   @GetMapping("/byType")
    public ResponseEntity<List<Transaction>> getByType(@RequestParam String type){
       List<Transaction> gettype = service.getTransactionByType(type);
       return ResponseEntity.status(HttpStatus.OK).body(gettype);
   }
   @GetMapping("/byStatus")
    public ResponseEntity<List<Transaction>> getByStatus(@RequestParam String status){
       List<Transaction> getByStatus = service.getTransactionByStatus(status);
       return ResponseEntity.status(HttpStatus.OK).body(getByStatus);
   }
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestBody TransferRequest request) {
        Transaction transaction = service.transfer(request.getSenderWalletId(),request.getReceiverWalletId(),request.getAmount());

        return ResponseEntity.ok(transaction);
    }
    @PostMapping("/topup")
    public ResponseEntity<Transaction> topUp(
            @RequestBody TopUpRequest request) {

        Transaction transaction = service.topUp(
                request.getWalletId(),
                request.getAmount()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaction);
    }


}
