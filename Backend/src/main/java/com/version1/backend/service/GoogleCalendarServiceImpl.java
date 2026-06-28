package com.version1.backend.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.version1.backend.dto.CalendarEventResult;
import com.version1.backend.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    @Autowired
    private Calendar googleCalendar;

    @Value("${app.google.calendar.id:primary}")
    private String calendarId;

    @Value("${app.google.calendar.invite-attendees:true}")
    private boolean inviteAttendees;

    @Override
    public CalendarEventResult createEvent(String customerEmail, LocalDateTime startTime, LocalDateTime endTime, String description) {
        try {
            Event event = new Event()
                    .setSummary("Vaidhyashala Consultation")
                    .setDescription(description != null ? description : "Scheduled Wellness consultation.")
                    .setLocation("Google Meet Video Call");

            // Convert LocalDateTime to ISO-8601 DateTime for Google API
            ZoneId zoneId = ZoneId.systemDefault();
            long startEpoch = startTime.atZone(zoneId).toInstant().toEpochMilli();
            long endEpoch = endTime.atZone(zoneId).toInstant().toEpochMilli();

            EventDateTime startEventTime = new EventDateTime()
                    .setDateTime(new DateTime(startEpoch))
                    .setTimeZone(zoneId.getId());
            event.setStart(startEventTime);

            EventDateTime endEventTime = new EventDateTime()
                    .setDateTime(new DateTime(endEpoch))
                    .setTimeZone(zoneId.getId());
            event.setEnd(endEventTime);

            if (inviteAttendees) {
                // Add the customer as an attendee (requires Domain-Wide Delegation in Workspace)
                EventAttendee attendee = new EventAttendee().setEmail(customerEmail);
                event.setAttendees(Collections.singletonList(attendee));
            }

            // Enable Google Meet video conference link generation
            CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest()
                    .setRequestId(UUID.randomUUID().toString())
                    .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"));
            ConferenceData conferenceData = new ConferenceData()
                    .setCreateRequest(createConferenceRequest);
            event.setConferenceData(conferenceData);

            Event createdEvent;
            try {
                // Create event in Google Calendar API with Google Meet integration
                Calendar.Events.Insert insert = googleCalendar.events().insert(calendarId, event)
                        .setConferenceDataVersion(1); // Required to trigger Meet link generation
                
                if (inviteAttendees) {
                    insert.setSendUpdates("all");
                }
                createdEvent = insert.execute();
            } catch (GoogleJsonResponseException e) {
                if (event.getConferenceData() != null) {
                    log.warn("Failed to generate Google Meet link (likely due to consumer/personal account restrictions). Retrying event creation without Google Meet: {}", e.getMessage());
                    event.setConferenceData(null);
                    Calendar.Events.Insert retryInsert = googleCalendar.events().insert(calendarId, event);
                    if (inviteAttendees) {
                        retryInsert.setSendUpdates("all");
                    }
                    createdEvent = retryInsert.execute();
                } else {
                    throw e;
                }
            }

            // Extract Google Meet join link (present if conference data was created)
            String meetLink = createdEvent.getHangoutLink();
            if (meetLink == null && createdEvent.getConferenceData() != null
                    && createdEvent.getConferenceData().getEntryPoints() != null) {
                meetLink = createdEvent.getConferenceData().getEntryPoints().stream()
                        .filter(ep -> "video".equals(ep.getEntryPointType()))
                        .map(ep -> ep.getUri())
                        .findFirst()
                        .orElse(null);
            }
            log.info("Google Calendar Event created successfully: {} | Meet link: {}", createdEvent.getId(), meetLink);
            return new CalendarEventResult(createdEvent.getId(), meetLink);

        } catch (Exception e) {
            log.error("Failed to create Google Calendar event", e);
            throw new CustomException("Failed to schedule meeting on Google Calendar: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
