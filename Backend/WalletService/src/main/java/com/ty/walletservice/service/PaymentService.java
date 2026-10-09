package com.ty.walletservice.service;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.ty.walletservice.entity.TransferRequest;

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
    Order createTransferOrder(
            TransferRequest request
    ) throws RazorpayException;

    boolean isPaymentCapturedForOrder(
            String razorpayOrderId,
            String razorpayPaymentId,
            BigDecimal expectedAmount
    );
}
