package com.ty.walletservice.implementation;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.ty.walletservice.exception.InvalidAmountException;
import com.ty.walletservice.service.PaymentService;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final RazorpayClient razorpayClient;

    public PaymentServiceImpl(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
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
        return razorpayClient.orders.create(orderRequest);
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
}