package com.version1.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.version1.backend.dto.DoctorProfileDto;
import com.version1.backend.pojo.AdminNotification;
import com.version1.backend.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AdminController handles all PROVIDER (clinic admin) management endpoints.
 * All endpoints require PROVIDER role.
 *
 * Endpoint prefix: /api/v1/admin
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('PROVIDER')")
public class AdminController {

    @Autowired
    private DoctorService doctorService;

    // -----------------------------------------------------------------------
    // Doctor Management
    // -----------------------------------------------------------------------

    /**
     * GET /api/v1/admin/doctors
     * Returns all doctors across all statuses (PENDING_APPROVAL, ACTIVE, OPTED_OUT, SUSPENDED).
     */
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorProfileDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    /**
     * GET /api/v1/admin/doctors/{id}
     * Returns full profile details for a specific doctor.
     */
    @GetMapping("/doctors/{id}")
    public ResponseEntity<DoctorProfileDto> getDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    /**
     * PUT /api/v1/admin/doctors/{id}/approve
     * Approves a PENDING_APPROVAL doctor → sets status to ACTIVE.
     */
    @PutMapping("/doctors/{id}/approve")
    public ResponseEntity<DoctorProfileDto> approveDoctor(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.approveDoctor(id));
    }

    @PutMapping("/doctors/{id}/suspend")
    public ResponseEntity<DoctorProfileDto> suspendDoctor(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.suspendDoctor(id));
    }

    /**
     * POST /api/v1/admin/doctors
     * Creates a new doctor profile directly.
     * Body format: { ...DoctorProfileDto fields..., "password": "..." }
     */
    @PostMapping("/doctors")
    public ResponseEntity<DoctorProfileDto> createDoctor(@Valid @RequestBody Map<String, Object> body) {
        ObjectMapper mapper = new ObjectMapper();
        DoctorProfileDto dto = mapper.convertValue(body, DoctorProfileDto.class);
        String password = (String) body.get("password");
        if (password == null || password.isBlank()) {
            password = UUID.randomUUID().toString().substring(0, 8); // Auto-generate if blank
        }
        return new ResponseEntity<>(doctorService.createDoctor(dto, password), HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/admin/doctors/{id}
     * Updates an existing doctor profile fields.
     */
    @PutMapping("/doctors/{id}")
    public ResponseEntity<DoctorProfileDto> updateDoctor(
            @PathVariable UUID id,
            @RequestBody DoctorProfileDto dto) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, dto));
    }

    /**
     * DELETE /api/v1/admin/doctors/{id}
     * Deletes a doctor's profile and user account (only if they have no appointments).
     */
    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(Map.of("message", "Doctor profile and user account deleted successfully."));
    }

    // -----------------------------------------------------------------------
    // Admin Notifications Inbox
    // -----------------------------------------------------------------------

    /**
     * GET /api/v1/admin/notifications
     * Returns all admin notifications, newest first.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<AdminNotification>> getAllNotifications() {
        return ResponseEntity.ok(doctorService.getAllAdminNotifications());
    }

    /**
     * PUT /api/v1/admin/notifications/{id}/acknowledge
     * Marks a notification as acknowledged (read/actioned by admin).
     */
    @PutMapping("/notifications/{id}/acknowledge")
    public ResponseEntity<Map<String, String>> acknowledgeNotification(@PathVariable UUID id) {
        doctorService.acknowledgeNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification acknowledged"));
    }
}
