package com.ty.walletservice.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.ty.walletservice.entity.PaymentVerifyRequest;
import com.ty.walletservice.entity.TopUpRequest;
import com.ty.walletservice.entity.Transaction;
import com.ty.walletservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ty.walletservice.service.TransactionService;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {
    private final PaymentService paymentService;
    private final TransactionService transactionService;

    public PaymentController(PaymentService paymentService, TransactionService transactionService) {
        this.paymentService = paymentService;
        this.transactionService = transactionService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestBody TopUpRequest request) {

        try {

            Order order = paymentService.createOrder(
                    request.getWalletId(),
                    request.getAmount()
            );

            // Convert Razorpay Order into a normal JSON response
            java.util.Map<String, Object> response = new java.util.HashMap<>();

            response.put("id", order.get("id"));
            response.put("entity", order.get("entity"));
            response.put("amount", order.get("amount"));
            response.put("amount_paid", order.get("amount_paid"));
            response.put("amount_due", order.get("amount_due"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("status", order.get("status"));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RazorpayException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to create Razorpay order");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody PaymentVerifyRequest request) {

        boolean verified = paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!verified) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Payment verification failed");
        }

        // Payment is genuine → credit wallet
        Transaction transaction = transactionService.topUp(
                request.getWalletId(),
                request.getAmount()
        );

        return ResponseEntity.ok(transaction);
    }
}
