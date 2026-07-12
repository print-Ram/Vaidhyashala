package com.version1.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.version1.backend.dto.*;
import com.version1.backend.enums.AdminNotificationType;
import com.version1.backend.enums.DoctorStatus;
import com.version1.backend.enums.Role;
import com.version1.backend.enums.UserStatus;
import com.version1.backend.exception.CustomException;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.pojo.*;
import com.version1.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired private UserRepository userRepository;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private DoctorAvailabilityRepository availabilityRepository;
    @Autowired private AdminNotificationRepository adminNotificationRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    // -----------------------------------------------------------------------
    // Doctor Registration
    // -----------------------------------------------------------------------


    // -----------------------------------------------------------------------
    // Doctor Self-Service
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileDto getMyProfile(UUID userId) {
        DoctorProfile profile = findProfileByUserId(userId);
        return toDto(profile);
    }

    @Override
    @Transactional
    public DoctorProfileDto updateMyProfile(UUID userId, DoctorProfileDto dto) {
        DoctorProfile profile = findProfileByUserId(userId);

        // Doctors can update their own editable fields
        if (dto.getFirstName() != null)       profile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)         profile.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null)      profile.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getSpecialization() != null)   profile.setSpecialization(dto.getSpecialization());
        if (dto.getDepartment() != null)       profile.setDepartment(dto.getDepartment());
        if (dto.getExpertIn() != null)         profile.setExpertIn(dto.getExpertIn());
        if (dto.getEducationDetails() != null) profile.setEducationDetails(dto.getEducationDetails());
        if (dto.getCertifications() != null)   profile.setCertifications(dto.getCertifications());
        if (dto.getAbout() != null)            profile.setAbout(dto.getAbout());

        return toDto(doctorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void optOut(UUID userId, OptOutRequestDto dto) {
        DoctorProfile profile = findProfileByUserId(userId);

        if (profile.getStatus() == DoctorStatus.OPTED_OUT) {
            throw new CustomException("You have already opted out", HttpStatus.BAD_REQUEST);
        }

        profile.setStatus(DoctorStatus.OPTED_OUT);
        profile.setOptedOutReason(dto.getReason());
        doctorProfileRepository.save(profile);

        // Create admin notification
        String message = String.format(
                "Doctor opted out of the scheme. " +
                "Name: %s %s | Email: %s | Reason: %s",
                profile.getFirstName(), profile.getLastName(),
                profile.getUser().getEmail(), dto.getReason()
        );
        AdminNotification notification = AdminNotification.builder()
                .type(AdminNotificationType.DOCTOR_OPT_OUT)
                .referenceDoctorId(profile.getId())
                .message(message)
                .build();
        adminNotificationRepository.save(notification);

        // Notify admin via email (async)
        emailService.sendAdminDoctorOptOutNotification(
                profile.getFirstName() + " " + profile.getLastName(),
                profile.getUser().getEmail(),
                dto.getReason()
        );
    }

    // -----------------------------------------------------------------------
    // Availability Management
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public DoctorAvailabilityDto saveAvailability(UUID userId, DoctorAvailabilityDto dto) {
        DoctorProfile profile = findProfileByUserId(userId);

        if (profile.getStatus() != DoctorStatus.ACTIVE) {
            throw new CustomException("Only ACTIVE doctors can set availability", HttpStatus.FORBIDDEN);
        }

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(profile)
                .availabilityDate(dto.getAvailabilityDate())
                .dayOfWeek(dto.getDayOfWeek() != null ? dto.getDayOfWeek().toUpperCase() : null)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .slotDurationMinutes(dto.getSlotDurationMinutes() > 0 ? dto.getSlotDurationMinutes() : 30)
                .consultationFee(dto.getConsultationFee())
                .slotStatus(dto.getSlotStatus())
                .offerPercent(dto.getOfferPercent())
                .offerLabel(dto.getOfferLabel())
                .isActive(true)
                .build();

        return toAvailabilityDto(availabilityRepository.save(availability));
    }

    @Override
    @Transactional
    public DoctorAvailabilityDto updateAvailability(UUID userId, UUID availabilityId, DoctorAvailabilityDto dto) {
        DoctorProfile profile = findProfileByUserId(userId);

        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability window not found: " + availabilityId));

        // Ensure this window belongs to the requesting doctor
        if (!availability.getDoctor().getId().equals(profile.getId())) {
            throw new CustomException("You can only update your own availability windows", HttpStatus.FORBIDDEN);
        }

        if (dto.getStartTime() != null)     availability.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null)       availability.setEndTime(dto.getEndTime());
        if (dto.getConsultationFee() != null) availability.setConsultationFee(dto.getConsultationFee());
        if (dto.getSlotStatus() != null)    availability.setSlotStatus(dto.getSlotStatus());
        if (dto.getSlotDurationMinutes() > 0) availability.setSlotDurationMinutes(dto.getSlotDurationMinutes());
        availability.setOfferPercent(dto.getOfferPercent());
        availability.setOfferLabel(dto.getOfferLabel());
        availability.setActive(dto.isActive());

        return toAvailabilityDto(availabilityRepository.save(availability));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorAvailabilityDto> getMyAvailability(UUID userId) {
        DoctorProfile profile = findProfileByUserId(userId);
        return availabilityRepository.findByDoctorIdAndIsActiveTrue(profile.getId())
                .stream().map(this::toAvailabilityDto).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // PROVIDER (Admin) Operations
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileDto> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileDto getDoctorById(UUID doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        return toDto(profile);
    }

    @Override
    @Transactional
    public DoctorProfileDto approveDoctor(UUID doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));

        if (profile.getStatus() != DoctorStatus.PENDING_APPROVAL) {
            throw new CustomException("Doctor is not in PENDING_APPROVAL state", HttpStatus.BAD_REQUEST);
        }
        profile.setStatus(DoctorStatus.ACTIVE);
        return toDto(doctorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public DoctorProfileDto suspendDoctor(UUID doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        profile.setStatus(DoctorStatus.SUSPENDED);
        return toDto(doctorProfileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminNotification> getAllAdminNotifications() {
        return adminNotificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public void acknowledgeNotification(UUID notificationId) {
        AdminNotification notification = adminNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setAcknowledged(true);
        adminNotificationRepository.save(notification);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DoctorProfile findProfileByUserId(UUID userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + userId));
    }

    private DoctorProfileDto toDto(DoctorProfile profile) {
        return DoctorProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .email(profile.getUser().getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .specialization(profile.getSpecialization())
                .department(profile.getDepartment())
                .expertIn(profile.getExpertIn())
                .educationDetails(profile.getEducationDetails())
                .certifications(profile.getCertifications())
                .about(profile.getAbout())
                .resumeUrl(profile.getResumeUrl())
                .status(profile.getStatus())
                .build();
    }

    private DoctorAvailabilityDto toAvailabilityDto(DoctorAvailability a) {
        return DoctorAvailabilityDto.builder()
                .id(a.getId())
                .availabilityDate(a.getAvailabilityDate())
                .dayOfWeek(a.getDayOfWeek())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .slotDurationMinutes(a.getSlotDurationMinutes())
                .consultationFee(a.getConsultationFee())
                .slotStatus(a.getSlotStatus())
                .offerPercent(a.getOfferPercent())
                .offerLabel(a.getOfferLabel())
                .isActive(a.isActive())
                .build();
    }

    @Override
    @Transactional
    public DoctorProfileDto createDoctor(DoctorProfileDto dto, String password) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new CustomException("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (password == null || password.isBlank()) {
            throw new CustomException("Password is required", HttpStatus.BAD_REQUEST);
        }
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            throw new CustomException("First name (Name) is required", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("Email is already in use", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.DOCTOR)
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        DoctorProfile profile = DoctorProfile.builder()
                .user(savedUser)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .specialization(dto.getSpecialization())
                .department(dto.getDepartment())
                .expertIn(dto.getExpertIn())
                .educationDetails(dto.getEducationDetails())
                .certifications(dto.getCertifications())
                .about(dto.getAbout())
                .status(DoctorStatus.ACTIVE)
                .build();
        DoctorProfile savedProfile = doctorProfileRepository.save(profile);

        return toDto(savedProfile);
    }

    @Override
    @Transactional
    public DoctorProfileDto updateDoctor(UUID doctorId, DoctorProfileDto dto) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));

        if (dto.getFirstName() != null)       profile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)         profile.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null)      profile.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getSpecialization() != null)   profile.setSpecialization(dto.getSpecialization());
        if (dto.getDepartment() != null)       profile.setDepartment(dto.getDepartment());
        if (dto.getExpertIn() != null)         profile.setExpertIn(dto.getExpertIn());
        if (dto.getEducationDetails() != null) profile.setEducationDetails(dto.getEducationDetails());
        if (dto.getCertifications() != null)   profile.setCertifications(dto.getCertifications());
        if (dto.getAbout() != null)            profile.setAbout(dto.getAbout());
        if (dto.getStatus() != null)           profile.setStatus(dto.getStatus());

        return toDto(doctorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void deleteDoctor(UUID doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        User user = profile.getUser();

        // Check if doctor has appointments
        List<Appointment> appointments = appointmentRepository.findByProviderId(user.getId());
        if (!appointments.isEmpty()) {
            throw new CustomException("Cannot delete doctor: they have associated appointments. Suspend them instead.", HttpStatus.BAD_REQUEST);
        }

        // Delete all doctor availabilities
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorId(profile.getId());
        availabilityRepository.deleteAll(availabilities);

        // Delete admin notifications related to this doctor
        List<AdminNotification> notifications = adminNotificationRepository.findByReferenceDoctorIdOrderByCreatedAtDesc(profile.getId());
        adminNotificationRepository.deleteAll(notifications);

        // Delete refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        // Delete doctor profile and user
        doctorProfileRepository.delete(profile);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileDto> getActiveDoctors() {
        return doctorProfileRepository.findByStatus(DoctorStatus.ACTIVE)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileDto getActiveDoctorById(UUID doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        if (profile.getStatus() != DoctorStatus.ACTIVE) {
            throw new CustomException("Doctor profile is not active/approved", HttpStatus.FORBIDDEN);
        }
        return toDto(profile);
    }
}
