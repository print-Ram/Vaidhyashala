package com.version1.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DoctorResumeParseResult is returned by AiService after parsing a doctor's uploaded resume PDF.
 * Fields are used to autofill DoctorProfile, and the raw profile is then fed to bio generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResumeParseResult {

    private String firstName;
    private String lastName;
    private String phoneNumber;

    /** Primary specializations detected from resume. */
    private List<String> specialization;

    /** Department detected from resume. */
    private String department;

    /** Expertise areas detected from resume. */
    private List<String> expertIn;

    /**
     * List of education entries parsed from resume.
     */
    private List<EducationEntry> education;

    /**
     * List of certification entries parsed from resume.
     */
    private List<CertificationEntry> certifications;

    /** AI-generated "About" bio text from resume analysis. */
    private String generatedBio;
}
