package com.ty.walletservice.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class PaymentVerifyRequest {
    private String walletId;
    private BigDecimal amount;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
