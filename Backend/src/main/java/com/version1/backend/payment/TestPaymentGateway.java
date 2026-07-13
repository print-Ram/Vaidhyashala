package com.version1.backend.payment;

import com.version1.backend.pojo.Payment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local test-mode gateway. It creates deterministic test order IDs and accepts
 * non-blank test transaction IDs. It must not be used for production payments.
 */
@Component
@ConditionalOnProperty(name = "app.payment.gateway", havingValue = "test", matchIfMissing = true)
public class TestPaymentGateway implements PaymentGateway {

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public PaymentGatewayOrder createOrder(Payment payment) {
        return new PaymentGatewayOrder("test_order_" + payment.getId());
    }

    @Override
    public PaymentGatewayVerification verify(Payment payment, String transactionId) {
        return new PaymentGatewayVerification(
                transactionId != null && !transactionId.isBlank(),
                transactionId
        );
    }
}
