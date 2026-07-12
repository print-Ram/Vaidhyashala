package com.version1.backend.service;

import com.version1.backend.dto.DoctorResumeParseResult;
import com.version1.backend.pojo.DoctorProfile;
import org.springframework.web.multipart.MultipartFile;

public interface AiService {

    /**
     * Parses a doctor's uploaded resume (PDF or text) using Spring AI / LLM.
     * Returns structured fields (education, certifications, specialization, etc.)
     * and an AI-generated "About" bio.
     *
     * @param resumeFile the uploaded resume file
     * @return parsed structured result ready to populate DoctorProfile
     */
    DoctorResumeParseResult parseResume(MultipartFile resumeFile);

    /**
     * Generates or regenerates an "About" bio for a doctor based on their current profile fields.
     * The doctor can call this to get a new AI-generated bio without uploading a resume.
     *
     * @param profile the current DoctorProfile entity
     * @return AI-generated bio text (not saved — caller decides whether to persist)
     */
    String generateBio(DoctorProfile profile);
}
