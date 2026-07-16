package com.version1.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentCreateDto {

    @NotNull
    private UUID doctorId;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    private String description;

    /**
     * Consultation fee snapshot from the selected SlotDto.
     * Captured at booking time so historical pricing is preserved.
     */
    private BigDecimal consultationFee;

    /**
     * Day-specific offer percent from DoctorAvailability (0 if none).
     * First-appointment 50% discount overrides this when applicable.
     */
    private int offerPercent;
}

