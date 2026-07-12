package com.version1.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.version1.backend.dto.DoctorResumeParseResult;
import com.version1.backend.exception.CustomException;
import com.version1.backend.pojo.DoctorProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * AiServiceImpl uses Spring AI (ChatClient) to:
 * 1. Parse a doctor's resume PDF/text → extract structured credentials + generate bio.
 * 2. Regenerate the "About" bio from existing profile data.
 *
 * Spring AI dependency: spring-ai-openai-spring-boot-starter (or equivalent Gemini starter).
 * Configure: spring.ai.openai.api-key in application.yml.
 *
 * The LLM is prompted to return JSON matching DoctorResumeParseResult structure.
 */
@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiServiceImpl(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    // -----------------------------------------------------------------------
    // Resume Parsing
    // -----------------------------------------------------------------------

    @Override
    public DoctorResumeParseResult parseResume(MultipartFile resumeFile) {
        try {
            // Extract text content from the uploaded file
            String resumeText = extractText(resumeFile);

            String prompt = buildResumeParsePrompt(resumeText);

            // Call the LLM
            String rawJson = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Strip markdown code fences if LLM wrapped response
            rawJson = stripMarkdownCodeFences(rawJson);

            return objectMapper.readValue(rawJson, DoctorResumeParseResult.class);

        } catch (Exception e) {
            log.error("Failed to parse resume via AI: {}", e.getMessage(), e);
            throw new CustomException("Resume parsing failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -----------------------------------------------------------------------
    // Bio Generation
    // -----------------------------------------------------------------------

    @Override
    public String generateBio(DoctorProfile profile) {
        try {
            String prompt = buildBioPrompt(profile);

            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("Failed to generate bio via AI: {}", e.getMessage(), e);
            throw new CustomException("Bio generation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -----------------------------------------------------------------------
    // Prompt Builders
    // -----------------------------------------------------------------------

    private String buildResumeParsePrompt(String resumeText) {
        return """
                You are a medical professional profile extractor. Given the doctor's resume text below,
                extract the following information and return ONLY a valid JSON object (no markdown, no explanation):
                
                {
                  "firstName": "string or null",
                  "lastName": "string or null",
                  "phoneNumber": "string or null",
                  "specialization": "primary medical specialization e.g. Cardiologist",
                  "department": "hospital department e.g. Cardiology",
                  "expertIn": "specific expertise e.g. Interventional Cardiology",
                  "education": [
                    { "degree": "MBBS", "institution": "AIIMS Delhi", "year": "2012" }
                  ],
                  "certifications": [
                    { "name": "DNB Cardiology", "issuingBody": "NBE", "year": "2016" }
                  ],
                  "generatedBio": "Write a 3-4 sentence professional third-person bio about this doctor \
                highlighting their expertise, experience, and patient care philosophy. Make it warm, \
                professional and inspiring for patients."
                }
                
                RESUME TEXT:
                """ + resumeText;
    }

    private String buildBioPrompt(DoctorProfile profile) {
        return String.format("""
                You are a professional medical copywriter. Based on the doctor's profile below,
                write a 3-4 sentence professional third-person "About" bio for their clinic profile page.
                The bio should highlight their expertise, inspire patient trust, and be warm yet authoritative.
                Return ONLY the bio text, no markdown, no labels.
                
                Doctor Profile:
                - Name: %s %s
                - Specialization: %s
                - Department: %s
                - Expert In: %s
                - Education: %s
                - Certifications: %s
                """,
                profile.getFirstName(), profile.getLastName(),
                profile.getSpecialization() != null ? profile.getSpecialization() : "General Medicine",
                profile.getDepartment() != null ? profile.getDepartment() : "General",
                profile.getExpertIn() != null ? profile.getExpertIn() : "General Practice",
                profile.getEducationDetails() != null ? profile.getEducationDetails() : "Not provided",
                profile.getCertifications() != null ? profile.getCertifications() : "Not provided"
        );
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        // For PDF files: use Apache PDFBox (add dependency if needed)
        // For now, attempt to read as UTF-8 text (works for .txt resumes)
        // TODO: Add PDF text extraction with org.apache.pdfbox:pdfbox when dependency is available
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private String stripMarkdownCodeFences(String text) {
        if (text == null) return "{}";
        String stripped = text.strip();
        if (stripped.startsWith("```json")) stripped = stripped.substring(7);
        else if (stripped.startsWith("```")) stripped = stripped.substring(3);
        if (stripped.endsWith("```")) stripped = stripped.substring(0, stripped.length() - 3);
        return stripped.strip();
    }
}
