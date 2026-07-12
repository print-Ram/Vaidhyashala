package com.version1.backend.service;

import com.version1.backend.dto.NextAvailabilityDto;
import com.version1.backend.dto.SlotDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SlotService {

    /**
     * Generate the full slot grid for a doctor on a given date.
     * Each SlotDto carries isBooked (CONFIRMED appointment exists) and
     * isPast (slot startTime < current time) flags — BookMyShow-style.
     *
     * @param doctorUserId the User.id of the DOCTOR (same as provider_id in appointments)
     * @param date         the calendar date to generate slots for
     */
    List<SlotDto> getSlotsForDate(UUID doctorUserId, LocalDate date);

    /**
     * Generate the 3-day next-availability summary (today, tomorrow, day-after).
     * Includes per-day slot counts, booked counts, and offer info.
     *
     * @param doctorUserId the User.id of the DOCTOR
     */
    NextAvailabilityDto getNextAvailability(UUID doctorUserId);
}
