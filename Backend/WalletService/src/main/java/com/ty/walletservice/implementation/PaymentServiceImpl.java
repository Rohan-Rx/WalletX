package com.ty.walletservice.implementation;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.ty.walletservice.entity.TransferRequest;
import com.ty.walletservice.exception.InvalidAmountException;
import com.ty.walletservice.service.PaymentService;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.ty.walletservice.entity.Payment;
import com.ty.walletservice.repository.PaymentRepo;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final RazorpayClient razorpayClient;
    private final PaymentRepo paymentRepo;

    public PaymentServiceImpl(RazorpayClient razorpayClient, PaymentRepo paymentRepo) {
        this.razorpayClient = razorpayClient;
        this.paymentRepo=paymentRepo;
    }

    @Override
    public Order createOrder(
            String walletId,
            BigDecimal amount
    ) throws RazorpayException {

        // Validate wallet ID
        if (walletId == null || walletId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Wallet ID cannot be null or empty"
            );
        }

        // Validate amount
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException(
                    "Amount must be greater than zero"
            );
        }

        // Convert INR to paise
        long amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        // Generate unique receipt
        String receipt = "wallet_" + UUID.randomUUID();

        // Create Razorpay order request
        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        // Send request to Razorpay
        Order order = razorpayClient.orders.create(orderRequest);

        Payment payment = new Payment();

        payment.setRazorpayOrderId(order.get("id"));
        payment.setWalletId(walletId);
        payment.setAmount(amount);
        payment.setStatus("CREATED");

        paymentRepo.save(payment);

        return order;
    }

    @Override
    public boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    ) {

        try {

            System.out.println("===== Razorpay Verification =====");
            System.out.println("Order ID     : " + razorpayOrderId);
            System.out.println("Payment ID   : " + razorpayPaymentId);
            System.out.println("Signature    : " + razorpaySignature);
            System.out.println("================================");

            if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
                System.out.println("Order ID is missing");
                return false;
            }

            if (razorpayPaymentId == null || razorpayPaymentId.isBlank()) {
                System.out.println("Payment ID is missing");
                return false;
            }

            if (razorpaySignature == null || razorpaySignature.isBlank()) {
                System.out.println("Signature is missing");
                return false;
            }

            String payload =
                    razorpayOrderId + "|" + razorpayPaymentId;

            System.out.println("Payload: " + payload);

            boolean verified = Utils.verifySignature(
                    payload,
                    razorpaySignature,
                    keySecret
            );

            System.out.println("Signature verified: " + verified);

            return verified;

        } catch (Exception e) {

            System.out.println("===== Razorpay Verification Error =====");
            e.printStackTrace();

            return false;
        }
    }

    @Override
    public Order createTransferOrder(
            TransferRequest request
    ) throws RazorpayException {

        if (request == null) {
            throw new IllegalArgumentException("Transfer request cannot be null");
        }

        if (request.getSenderWalletId() == null ||
                request.getSenderWalletId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Sender Wallet ID cannot be null or empty"
            );
        }

        if (request.getReceiverWalletId() == null ||
                request.getReceiverWalletId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Receiver Wallet ID cannot be null or empty"
            );
        }

        if (request.getSenderWalletId()
                .equals(request.getReceiverWalletId())) {
            throw new IllegalArgumentException(
                    "Sender and Receiver must be different"
            );
        }

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than 0"
            );
        }

        long amountInPaise = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        String receipt = "transfer_" + UUID.randomUUID();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        Order order = razorpayClient.orders.create(orderRequest);

        Payment payment = new Payment();

        payment.setRazorpayOrderId(order.get("id"));
        payment.setSenderWalletId(request.getSenderWalletId());
        payment.setReceiverWalletId(request.getReceiverWalletId());
        payment.setAmount(request.getAmount());
        payment.setStatus("CREATED");

        paymentRepo.save(payment);

        return order;
    }


    @Override
    public boolean isPaymentCapturedForOrder(
            String razorpayOrderId,
            String razorpayPaymentId,
            BigDecimal expectedAmount
    ) {
        try {
            if (razorpayOrderId == null || razorpayOrderId.isBlank()
                    || razorpayPaymentId == null || razorpayPaymentId.isBlank()
                    || expectedAmount == null
                    || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // Fetch the actual payment details from Razorpay.
            com.razorpay.Payment razorpayPayment =
                    razorpayClient.payments.fetch(razorpayPaymentId);

            String actualOrderId = razorpayPayment.get("order_id");
            String status = razorpayPayment.get("status");
            String currency = razorpayPayment.get("currency");
            long actualAmountInPaise =
                    ((Number) razorpayPayment.get("amount")).longValue();

            long expectedAmountInPaise = expectedAmount
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            return razorpayOrderId.equals(actualOrderId)
                    && "captured".equalsIgnoreCase(status)
                    && "INR".equalsIgnoreCase(currency)
                    && actualAmountInPaise == expectedAmountInPaise;

        } catch (Exception e) {
            System.err.println(
                    "Unable to verify captured Razorpay payment: "
                            + e.getMessage()
            );
            return false;
        }
    }

}