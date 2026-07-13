package com.version1.backend.service;

import com.version1.backend.dto.PaymentCheckoutDto;
import com.version1.backend.dto.PaymentVerificationDto;
import com.version1.backend.pojo.Payment;

public interface PaymentService {
    Payment checkout(PaymentCheckoutDto dto);
    Payment verifyPayment(PaymentVerificationDto dto) throws Exception;
}
