package com.version1.backend.service;

import com.version1.backend.dto.*;
import com.version1.backend.pojo.AdminNotification;

import java.util.List;
import java.util.UUID;

public interface DoctorService {


    /** Get own doctor profile (DOCTOR role). */
    DoctorProfileDto getMyProfile(UUID userId);

    /** Update own doctor profile — editable fields only (about, specialization, etc.). */
    DoctorProfileDto updateMyProfile(UUID userId, DoctorProfileDto dto);

    /** Doctor opts out — sets OPTED_OUT status + sends admin notification. */
    void optOut(UUID userId, OptOutRequestDto dto);

    /** Set or update an availability window (DOCTOR role). */
    DoctorAvailabilityDto saveAvailability(UUID userId, DoctorAvailabilityDto dto);

    /** Update existing availability window by ID. */
    DoctorAvailabilityDto updateAvailability(UUID userId, UUID availabilityId, DoctorAvailabilityDto dto);

    /** Get own availability windows (DOCTOR role). */
    List<DoctorAvailabilityDto> getMyAvailability(UUID userId);

    // ---------- PROVIDER (admin) operations ----------

    /** List all doctors (any status) — PROVIDER only. */
    List<DoctorProfileDto> getAllDoctors();

    /** Get a specific doctor's full profile — PROVIDER only. */
    DoctorProfileDto getDoctorById(UUID doctorId);

    /** Approve a doctor (PENDING_APPROVAL → ACTIVE) — PROVIDER only. */
    DoctorProfileDto approveDoctor(UUID doctorId);

    /** Suspend a doctor — PROVIDER only. */
    DoctorProfileDto suspendDoctor(UUID doctorId);

    /** Get all admin notifications, newest first — PROVIDER only. */
    List<AdminNotification> getAllAdminNotifications();

    /** Acknowledge an admin notification — PROVIDER only. */
    void acknowledgeNotification(UUID notificationId);

    // CRUD operations by Admin
    DoctorProfileDto createDoctor(DoctorProfileDto dto, String password);
    DoctorProfileDto updateDoctor(UUID doctorId, DoctorProfileDto dto);
    void deleteDoctor(UUID doctorId);

    // Public/Guest operations
    List<DoctorProfileDto> getActiveDoctors();
    DoctorProfileDto getActiveDoctorById(UUID doctorId);

    /** Unified doctor settings and availability configuration dashboard API. */
    DoctorProfileDto configureMyDashboard(UUID userId, DoctorConfigurationRequestDto config, org.springframework.web.multipart.MultipartFile profileImageFile) throws Exception;
}
