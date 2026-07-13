package com.version1.backend.dto;

import com.version1.backend.enums.SpecialRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialRequestStatusUpdateDto {
    @NotNull(message = "Status is required")
    private SpecialRequestStatus status;
}
