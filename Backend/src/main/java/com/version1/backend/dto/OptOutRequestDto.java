package com.version1.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Request body for POST /api/v1/doctors/me/opt-out */
@Data
public class OptOutRequestDto {

    @NotBlank(message = "A reason must be provided when opting out")
    private String reason;
}
