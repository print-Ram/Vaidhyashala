package com.version1.backend.service;

import com.version1.backend.dto.CalendarEventResult;

import java.time.LocalDateTime;

public interface GoogleCalendarService {
    /**
     * Creates a calendar event on the shared organization calendar and invites the customer.
     *
     * @param customerEmail The email address of the customer to invite.
     * @param startTime The appointment start time.
     * @param endTime The appointment end time.
     * @param description Brief description of the meeting.
     * @return CalendarEventResult containing the event ID and Google Meet link (if available).
     */
    CalendarEventResult createEvent(String customerEmail, LocalDateTime startTime, LocalDateTime endTime, String description);
}
