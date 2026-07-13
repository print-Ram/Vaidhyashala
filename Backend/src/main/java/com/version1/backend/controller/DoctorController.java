package com.version1.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.version1.backend.dto.*;
import com.version1.backend.pojo.DoctorProfile;
import com.version1.backend.repository.DoctorProfileRepository;
import com.version1.backend.security.UserPrincipal;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.service.DoctorService;
import com.version1.backend.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DoctorController handles:
 *  - Public endpoints: slot grid, next-availability, doctor registration
 *  - DOCTOR self-service: profile, availability, opt-out, resume upload, bio regeneration
 */
@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    @Autowired private DoctorService doctorService;
    @Autowired private SlotService slotService;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Public Endpoints (no auth required — see SecurityConfig)
    // -----------------------------------------------------------------------

    /**
     * GET /api/v1/doctors/{doctorUserId}/slots?date=YYYY-MM-DD
     * Returns BookMyShow-style slot grid for a doctor on a given date.
     * Slots before current IST time are flagged isPast=true.
     * Booked slots are flagged isBooked=true.
     */
    @GetMapping("/{doctorUserId}/slots")
    public ResponseEntity<List<SlotDto>> getSlotsForDate(
            @PathVariable UUID doctorUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getSlotsForDate(doctorUserId, date));
    }

    /**
     * GET /api/v1/doctors/{doctorUserId}/next-availability
     * Returns 3-day availability summary (today, tomorrow, day-after) with counts and offers.
     */
    @GetMapping("/{doctorUserId}/next-availability")
    public ResponseEntity<NextAvailabilityDto> getNextAvailability(@PathVariable UUID doctorUserId) {
        return ResponseEntity.ok(slotService.getNextAvailability(doctorUserId));
    }


    /**
     * GET /api/v1/doctors
     * Returns all ACTIVE (approved) doctors.
     */
    @GetMapping
    public ResponseEntity<List<DoctorProfileDto>> getActiveDoctors() {
        return ResponseEntity.ok(doctorService.getActiveDoctors());
    }

    /**
     * GET /api/v1/doctors/{id}
     * Returns details of a specific ACTIVE doctor profile.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileDto> getActiveDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getActiveDoctorById(id));
    }

    // -----------------------------------------------------------------------
    // DOCTOR Self-Service Endpoints
    // -----------------------------------------------------------------------

    /** GET /api/v1/doctors/me/profile — View own profile */
    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(doctorService.getMyProfile(principal.getId()));
    }

    /** PUT /api/v1/doctors/me/profile — Update own profile (editable fields) */
    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileDto> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DoctorProfileDto dto) {
        return ResponseEntity.ok(doctorService.updateMyProfile(principal.getId(), dto));
    }

    /** POST /api/v1/doctors/me/opt-out — Doctor voluntarily opts out */
    @PostMapping("/me/opt-out")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, String>> optOut(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OptOutRequestDto dto) {
        doctorService.optOut(principal.getId(), dto);
        return ResponseEntity.ok(Map.of("message", "Opt-out processed. The admin has been notified."));
    }

    // -----------------------------------------------------------------------
    // Availability Management
    // -----------------------------------------------------------------------

    /** POST /api/v1/doctors/me/availability — Create a new availability window */
    @PostMapping("/me/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorAvailabilityDto> createAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DoctorAvailabilityDto dto) {
        return new ResponseEntity<>(doctorService.saveAvailability(principal.getId(), dto), HttpStatus.CREATED);
    }

    /** PUT /api/v1/doctors/me/availability/{id} — Update an existing availability window */
    @PutMapping("/me/availability/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorAvailabilityDto> updateAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody DoctorAvailabilityDto dto) {
        return ResponseEntity.ok(doctorService.updateAvailability(principal.getId(), id, dto));
    }

    /** GET /api/v1/doctors/me/availability — View own availability windows */
    @GetMapping("/me/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorAvailabilityDto>> getMyAvailability(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(doctorService.getMyAvailability(principal.getId()));
    }

    // -----------------------------------------------------------------------
    // Spring AI — Resume Upload & Bio Generation
    // -----------------------------------------------------------------------

    /**
     * POST /api/v1/doctors/me/resume  (multipart/form-data, file=resume)
     * Saves the resume URL / path to the doctor's profile.
     * Returns the updated DoctorProfileDto.
     */
    @PostMapping(value = "/me/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileDto> uploadResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) throws Exception {

        DoctorProfile profile = doctorProfileRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        profile.setResumeUrl("uploads/resumes/" + file.getOriginalFilename());
        doctorProfileRepository.save(profile);

        return ResponseEntity.ok(doctorService.getMyProfile(principal.getId()));
    }

    /**
     * PUT /api/v1/doctors/me/configure
     * Unified dashboard settings and slots update API.
     * Consumes multipart/form-data.
     */
    @PutMapping(value = "/me/configure", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileDto> configureDashboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("settings") DoctorConfigurationRequestDto settings,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws Exception {
        return ResponseEntity.ok(doctorService.configureMyDashboard(principal.getId(), settings, profileImage));
    }
}
