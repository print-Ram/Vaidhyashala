package com.version1.backend.dto;

import com.version1.backend.enums.SlotStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DoctorAvailabilityDto is used for:
 *  - POST /api/v1/doctors/me/availability   (doctor sets a new window)
 *  - PUT  /api/v1/doctors/me/availability/{id}   (update existing window)
 *  - GET  /api/v1/doctors/me/availability   (view all windows)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityDto {

    private UUID id;

    /** Specific date override (null = recurring weekday rule). */
    private LocalDate availabilityDate;

    /** Recurring weekday name e.g. "MONDAY". Null if specific date is provided. */
    private String dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    /** Duration of each slot in minutes (default 30). */
    @Builder.Default
    private int slotDurationMinutes = 30;

    /** Consultation fee for slots in this window. */
    private BigDecimal consultationFee;

    /** Demand label for this window. */
    @Builder.Default
    private SlotStatus slotStatus = SlotStatus.NORMAL;

    /** Offer discount percent (0 = no offer). */
    @Builder.Default
    private int offerPercent = 0;

    /** Offer label text, e.g. "Festival Discount". */
    private String offerLabel;

    private boolean isActive;
}
