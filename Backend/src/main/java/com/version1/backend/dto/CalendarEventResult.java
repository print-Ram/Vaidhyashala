package com.version1.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarEventResult {
    private String eventId;
    private String meetLink; // Google Meet URL, may be null if account doesn't support it
}
