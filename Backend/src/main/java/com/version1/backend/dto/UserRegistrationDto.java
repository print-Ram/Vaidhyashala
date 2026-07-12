package com.version1.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegistrationDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank
    private String firstName;

    private String lastName;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    // -------------------------------------------------------------------
    // Address fields are OPTIONAL at sign-up.
    // Customers can fill them in later via PUT /api/v1/customers/me/address
    // -------------------------------------------------------------------
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
