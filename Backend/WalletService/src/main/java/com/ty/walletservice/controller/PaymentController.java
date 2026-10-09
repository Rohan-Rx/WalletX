package com.ty.walletservice.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.ty.walletservice.entity.*;
import com.ty.walletservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ty.walletservice.service.TransactionService;
import com.ty.walletservice.repository.PaymentRepo;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final PaymentRepo paymentRepo;

    public PaymentController(PaymentService paymentService, TransactionService transactionService, PaymentRepo paymentRepo) {
        this.paymentService = paymentService;
        this.transactionService = transactionService;
        this.paymentRepo = paymentRepo;
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

        // 1. Find our payment record using Razorpay Order ID
        Payment payment = paymentRepo
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Payment order not found")
                );
        System.out.println("===== Payment From Database =====");
        System.out.println("Payment ID     : " + payment.getId());
        System.out.println("Wallet ID      : [" + payment.getWalletId() + "]");
        System.out.println("Amount         : " + payment.getAmount());
        System.out.println("Status         : " + payment.getStatus());
        System.out.println("Order ID       : " + payment.getRazorpayOrderId());
        System.out.println("================================");

        // 2. Prevent duplicate processing
        if ("PROCESSED".equals(payment.getStatus())) {
            return ResponseEntity
                    .badRequest()
                    .body("Payment has already been processed");
        }

        // 3. Verify Razorpay signature
        boolean verified = paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!verified) {

            payment.setStatus("FAILED");
            paymentRepo.save(payment);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Payment verification failed");
        }

        // 4. Save Razorpay payment ID
        payment.setRazorpayPaymentId(
                request.getRazorpayPaymentId()
        );
        String walletId = payment.getWalletId();
        BigDecimal amount = payment.getAmount();

        System.out.println("===== Before Top Up =====");
        System.out.println("Wallet ID : [" + walletId + "]");
        System.out.println("Amount    : " + amount);
        System.out.println("=========================");



        // 5. Credit the wallet using DATABASE values
        Transaction transaction = transactionService.topUp(
                payment.getWalletId(),
                payment.getAmount()
        );

        // 6. Mark payment as processed
        payment.setStatus("PROCESSED");
        paymentRepo.save(payment);

        return ResponseEntity.ok(transaction);
    }
    @PostMapping("/create-transfer-order")
    public ResponseEntity<?> createTransferOrder(
            @RequestBody TransferRequest request) {

        try {

            Order order = paymentService.createTransferOrder(request);

            java.util.Map<String, Object> response =
                    new java.util.HashMap<>();

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
                    .body("Unable to create Razorpay transfer order");
        }
    }

    @PostMapping("/verify-transfer")
    public ResponseEntity<?> verifyTransfer(
            @RequestBody PaymentVerifyRequest request) {

        if (request.getRazorpayOrderId() == null
                || request.getRazorpayOrderId().isBlank()
                || request.getRazorpayPaymentId() == null
                || request.getRazorpayPaymentId().isBlank()
                || request.getRazorpaySignature() == null
                || request.getRazorpaySignature().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Order ID, payment ID, and signature are required");
        }

        Payment payment = paymentRepo
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElse(null);

        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment order not found");
        }

        if ("PROCESSED".equals(payment.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("Payment has already been processed");
        }

        // Ensure this is a transfer payment, not an Add Money order.
        if (payment.getSenderWalletId() == null
                || payment.getReceiverWalletId() == null) {
            return ResponseEntity.badRequest()
                    .body("This order is not a wallet transfer");
        }

        // Step 1: Verify the Razorpay signature.
        boolean signatureValid = paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!signatureValid) {
            return ResponseEntity.badRequest()
                    .body("Razorpay signature verification failed");
        }

        // Step 2: Verify the actual captured payment with Razorpay.
        boolean paymentCaptured = paymentService.isPaymentCapturedForOrder(
                payment.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                payment.getAmount()
        );

        if (!paymentCaptured) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Payment is not captured or payment details do not match");
        }

        // Step 3: Record the Razorpay payment ID.
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());

        // Step 4: Execute transfer using server-side database values.
        Transaction transaction = transactionService.transfer(
                payment.getSenderWalletId(),
                payment.getReceiverWalletId(),
                payment.getAmount()
        );

        // Step 5: Mark the payment as processed.
        payment.setStatus("PROCESSED");
        paymentRepo.save(payment);

        return ResponseEntity.ok(transaction);
    }
}
