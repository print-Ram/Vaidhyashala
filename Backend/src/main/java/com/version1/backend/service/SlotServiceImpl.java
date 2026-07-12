package com.version1.backend.service;

import com.version1.backend.dto.NextAvailabilityDto;
import com.version1.backend.dto.SlotDto;
import com.version1.backend.enums.AppointmentStatus;
import com.version1.backend.pojo.Appointment;
import com.version1.backend.pojo.DoctorAvailability;
import com.version1.backend.repository.AppointmentRepository;
import com.version1.backend.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SlotServiceImpl implements the BookMyShow-style slot grid logic:
 *
 * Resolution order for a given date:
 *  1. Specific-date override (e.g. doctor set a custom window for 2026-07-05)
 *  2. Recurring weekday rule (e.g. every MONDAY 9am–1pm)
 *  3. If neither → no availability for that day
 *
 * Slot states:
 *  - isPast   = slot startTime is before current IST time
 *  - isBooked = a CONFIRMED appointment already occupies this slot
 *  - Both false → slot is open and bookable
 */
@Service
public class SlotServiceImpl implements SlotService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Autowired private DoctorAvailabilityRepository availabilityRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    // -----------------------------------------------------------------------
    // Slot Grid for a Specific Date
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SlotDto> getSlotsForDate(UUID doctorUserId, LocalDate date) {
        // Resolve the availability window for this date
        Optional<DoctorAvailability> windowOpt = resolveAvailability(doctorUserId, date);

        if (windowOpt.isEmpty()) {
            // No availability configured for this day
            return List.of();
        }

        DoctorAvailability window = windowOpt.get();
        LocalDateTime now = LocalDateTime.now(IST);

        // Fetch all CONFIRMED appointments for this doctor on this date
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd   = date.atTime(LocalTime.MAX);
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorUserIdAndStatusAndStartTimeBetween(
                        doctorUserId, AppointmentStatus.CONFIRMED, dayStart, dayEnd);

        // Build a set of booked start times for O(1) lookup
        Set<LocalDateTime> bookedStartTimes = bookedAppointments.stream()
                .map(a -> a.getStartTime())
                .collect(Collectors.toSet());

        // Generate slot grid by splitting the window
        List<SlotDto> slots = new ArrayList<>();
        LocalTime cursor = window.getStartTime();
        LocalTime windowEnd = window.getEndTime();
        int durationMins = window.getSlotDurationMinutes();

        while (cursor.plusMinutes(durationMins).compareTo(windowEnd) <= 0) {
            LocalDateTime slotStart = date.atTime(cursor);
            LocalDateTime slotEnd   = slotStart.plusMinutes(durationMins);

            SlotDto slot = SlotDto.builder()
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .isBooked(bookedStartTimes.contains(slotStart))
                    .isPast(slotStart.isBefore(now))
                    .slotStatus(window.getSlotStatus())
                    .consultationFee(window.getConsultationFee())
                    .offerPercent(window.getOfferPercent())
                    .offerLabel(window.getOfferPercent() > 0 ? window.getOfferLabel() : null)
                    .build();

            slots.add(slot);
            cursor = cursor.plusMinutes(durationMins);
        }

        return slots;
    }

    // -----------------------------------------------------------------------
    // Next Availability — 3-Day Summary
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public NextAvailabilityDto getNextAvailability(UUID doctorUserId) {
        LocalDate today = LocalDate.now(IST);
        LocalDateTime now = LocalDateTime.now(IST);

        NextAvailabilityDto.DaySummary todaySummary    = buildDaySummary(doctorUserId, today, now);
        NextAvailabilityDto.DaySummary tomorrowSummary = buildDaySummary(doctorUserId, today.plusDays(1), now);
        NextAvailabilityDto.DaySummary dayAfterSummary = buildDaySummary(doctorUserId, today.plusDays(2), now);

        long totalBooked = todaySummary.getBookedCount()
                         + tomorrowSummary.getBookedCount()
                         + dayAfterSummary.getBookedCount();

        return NextAvailabilityDto.builder()
                .today(todaySummary)
                .tomorrow(tomorrowSummary)
                .dayAfterTomorrow(dayAfterSummary)
                .totalBookedIn3Days(totalBooked)
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Resolve availability for a date using the priority order:
     * specific-date override → weekday rule → empty.
     */
    private Optional<DoctorAvailability> resolveAvailability(UUID doctorUserId, LocalDate date) {
        // We need DoctorProfile.id (not user.id) for availability lookup
        Optional<DoctorAvailability> byDate = availabilityRepository
                .findByDoctorUserIdAndDate(doctorUserId, date);
        if (byDate.isPresent()) return byDate;

        String weekday = date.getDayOfWeek().name(); // e.g. "MONDAY"
        return availabilityRepository.findByDoctorUserIdAndWeekday(doctorUserId, weekday);
    }

    private NextAvailabilityDto.DaySummary buildDaySummary(UUID doctorUserId, LocalDate date, LocalDateTime now) {
        Optional<DoctorAvailability> windowOpt = resolveAvailability(doctorUserId, date);

        if (windowOpt.isEmpty()) {
            return NextAvailabilityDto.DaySummary.builder()
                    .date(date)
                    .hasAvailability(false)
                    .totalSlots(0)
                    .bookedCount(0)
                    .availableCount(0)
                    .build();
        }

        DoctorAvailability window = windowOpt.get();

        // Compute total slots in the window
        int totalSlots = computeTotalSlots(window);

        // Count past slots (slots whose start is before now — not bookable)
        int pastSlots = 0;
        if (date.equals(LocalDate.now(IST))) {
            // Only today can have past slots
            LocalTime cursor = window.getStartTime();
            LocalTime windowEnd = window.getEndTime();
            int dur = window.getSlotDurationMinutes();
            while (cursor.plusMinutes(dur).compareTo(windowEnd) <= 0) {
                LocalDateTime slotStart = date.atTime(cursor);
                if (slotStart.isBefore(now)) pastSlots++;
                cursor = cursor.plusMinutes(dur);
            }
        }

        // Count confirmed bookings for this day
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd   = date.plusDays(1).atStartOfDay();
        long bookedCount = appointmentRepository.countByDoctorUserIdAndStatusAndStartTimeBetween(
                doctorUserId, AppointmentStatus.CONFIRMED, dayStart, dayEnd);

        int availableCount = Math.max(0, totalSlots - pastSlots - (int) bookedCount);

        return NextAvailabilityDto.DaySummary.builder()
                .date(date)
                .hasAvailability(true)
                .totalSlots(totalSlots)
                .bookedCount(bookedCount)
                .availableCount(availableCount)
                .offerPercent(window.getOfferPercent())
                .offerLabel(window.getOfferPercent() > 0 ? window.getOfferLabel() : null)
                .build();
    }

    private int computeTotalSlots(DoctorAvailability window) {
        int totalMinutes = (int) java.time.Duration.between(
                window.getStartTime(), window.getEndTime()).toMinutes();
        return totalMinutes / window.getSlotDurationMinutes();
    }
}
