package com.ty.walletservice.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {
   @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<String> handleWalletNotFound(WalletNotFoundException ex){
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
   }
   @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalance(InsufficientBalanceException ib){
       return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ib.getMessage());
   }
   @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<String> handleInvalidAmount(InvalidAmountException ie){
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
   }
}
