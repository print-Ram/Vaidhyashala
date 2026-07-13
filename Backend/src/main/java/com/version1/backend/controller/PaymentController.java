package com.version1.backend.controller;

import com.version1.backend.dto.PaymentCheckoutDto;
import com.version1.backend.dto.PaymentVerificationDto;
import com.version1.backend.pojo.Payment;
import com.version1.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Payment> checkout(@Valid @RequestBody PaymentCheckoutDto dto) {
        Payment payment = paymentService.checkout(dto);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Payment> verifyPayment(@Valid @RequestBody PaymentVerificationDto dto) throws Exception {
        Payment payment = paymentService.verifyPayment(dto);
        return ResponseEntity.ok(payment);
    }
}
