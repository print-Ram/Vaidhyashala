package com.version1.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.version1.backend.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SlotDto represents one individual time slot in a doctor's availability grid.
 * Returned by GET /api/v1/doctors/{doctorId}/slots?date=YYYY-MM-DD
 *
 * BookMyShow-style rules:
 *  - isBooked = true  → slot is CONFIRMED, shown as disabled/greyed out
 *  - isPast   = true  → slot has already passed (current time > startTime), shown as disabled
 *  - Both false       → slot is available for booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlotDto {

    /** Slot window start (date + time). */
    private LocalDateTime startTime;

    /** Slot window end (date + time). */
    private LocalDateTime endTime;

    /** True if a CONFIRMED appointment already exists for this slot. */
    private boolean isBooked;

    /**
     * True if the slot's startTime is before the current server time.
     * Morning slots become past when viewed in the evening — they are hidden/disabled.
     */
    private boolean isPast;

    /** Demand label inherited from the DoctorAvailability window. */
    private SlotStatus slotStatus;

    /** Base consultation fee for this slot. */
    private BigDecimal consultationFee;

    /**
     * Offer discount percent (from DoctorAvailability window).
     * 0 = no offer for this day. Does not include first-appointment 50% (applied at booking).
     */
    private int offerPercent;

    /** Human-readable offer label, null if no offer. */
    private String offerLabel;
}
