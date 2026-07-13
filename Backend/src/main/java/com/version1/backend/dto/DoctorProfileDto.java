package com.version1.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.version1.backend.enums.DoctorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DoctorProfileDto is used for both GET (view profile) and PUT (update profile) responses.
 * Sensitive fields like status and opted_out_reason are read-only (set by service layer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorProfileDto {

    private UUID id;
    private UUID userId;
    private String email;           // from associated User

    private String firstName;
    private String lastName;
    private String phoneNumber;

    private java.util.List<String> specialization;
    private String department;
    private java.util.List<String> expertIn;

    /** Education details. */
    private java.util.List<EducationEntry> educationDetails;

    /** Certifications. */
    private java.util.List<CertificationEntry> certifications;

    /** AI-generated or doctor-edited "About" bio. */
    private String about;

    /** URL/path to the uploaded resume (read-only after upload). */
    private String resumeUrl;

    private String designation;
    private String regNo;
    private String profileImageUrl;

    /** Current approval status — set by admin, read-only for doctor. */
    private DoctorStatus status;
}
