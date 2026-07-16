package com.version1.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentResponseDto {

    private UUID id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String description;
    private String meetLink;
    private String googleCalendarEventId;
    private LocalDateTime createdAt;

    // Doctor info (shown to both customer and doctor/provider)
    private DoctorInfo doctor;

    // Customer info (shown to provider only)
    private CustomerInfo customer;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DoctorInfo {
        private UUID id;
        private String name;
        private String email;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomerInfo {
        private UUID id;
        private String name;
        private String email;
    }
}
