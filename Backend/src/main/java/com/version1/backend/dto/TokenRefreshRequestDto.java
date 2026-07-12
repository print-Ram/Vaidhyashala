package com.version1.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequestDto {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
