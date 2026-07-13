package com.version1.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationDto {
    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
}
