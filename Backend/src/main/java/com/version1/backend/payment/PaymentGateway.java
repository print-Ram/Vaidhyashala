package com.version1.backend.payment;

import com.version1.backend.pojo.Payment;

/**
 * Gateway boundary for payment providers such as Razorpay and GoKwik.
 */
public interface PaymentGateway {

    String getName();

    PaymentGatewayOrder createOrder(Payment payment);

    PaymentGatewayVerification verify(Payment payment, String transactionId);
}
