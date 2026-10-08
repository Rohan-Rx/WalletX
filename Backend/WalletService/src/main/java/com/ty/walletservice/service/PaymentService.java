package com.ty.walletservice.service;

import com.razorpay.Order;
import com.razorpay.RazorpayException;

import java.math.BigDecimal;

public interface PaymentService {

    Order createOrder(
            String walletId,
            BigDecimal amount
    ) throws RazorpayException;
    boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    );
}
