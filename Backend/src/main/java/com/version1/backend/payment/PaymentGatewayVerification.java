package com.version1.backend.payment;

public record PaymentGatewayVerification(boolean successful, String transactionId) {
}
