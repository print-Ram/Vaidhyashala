package com.version1.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequestDto {
    @NotBlank
    @Email
    private String email;

    private String name;
}
