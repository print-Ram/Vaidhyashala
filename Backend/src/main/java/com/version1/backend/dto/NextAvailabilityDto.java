package com.version1.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * NextAvailabilityDto is the response for GET /api/v1/doctors/{doctorId}/next-availability.
 *
 * Shows a 3-day live summary (today, tomorrow, day after) with slot counts and offer info.
 * Used in the "Next Availability" sidebar section of the landing page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NextAvailabilityDto {

    /** Summary for today. */
    private DaySummary today;

    /** Summary for tomorrow. */
    private DaySummary tomorrow;

    /** Summary for the day after tomorrow. */
    private DaySummary dayAfterTomorrow;

    /** Total confirmed bookings across all 3 days (for the "X booked" badge). */
    private long totalBookedIn3Days;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DaySummary {

        private LocalDate date;

        /** Total slot count derived from the availability window for this day. */
        private int totalSlots;

        /** Number of CONFIRMED bookings in this day. */
        private long bookedCount;

        /** totalSlots - bookedCount (excludes past slots — past slots are not bookable). */
        private int availableCount;

        /**
         * Offer discount percent active for this day (from DoctorAvailability).
         * 0 = no offer.
         */
        private int offerPercent;

        /** Human-readable offer label, null if no offer. */
        private String offerLabel;

        /** Whether any availability window exists for this day at all. */
        private boolean hasAvailability;
    }
}
