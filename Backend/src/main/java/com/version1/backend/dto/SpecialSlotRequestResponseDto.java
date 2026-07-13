package com.version1.backend.dto;

import com.version1.backend.enums.SpecialRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSlotRequestResponseDto {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID doctorId;
    private String doctorName;
    private LocalDate requestedDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;
    private SpecialRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
