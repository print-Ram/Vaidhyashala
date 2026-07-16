package com.version1.backend.service;

import com.version1.backend.dto.SpecialRequestStatusUpdateDto;
import com.version1.backend.dto.SpecialSlotRequestCreateDto;
import com.version1.backend.dto.SpecialSlotRequestResponseDto;
import com.version1.backend.enums.AppointmentStatus;
import com.version1.backend.enums.PaymentStatus;
import com.version1.backend.enums.Role;
import com.version1.backend.enums.SpecialRequestStatus;
import com.version1.backend.enums.UserStatus;
import com.version1.backend.pojo.*;
import com.version1.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class SpecialSlotRequestServiceTests {

    @Autowired
    private SpecialSlotRequestService specialSlotRequestService;

    @Autowired
    private SpecialSlotRequestRepository specialSlotRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private GoogleCalendarService googleCalendarService;

    @MockitoBean
    private JavaMailSender mailSender;

    private User doctorUser;
    private DoctorProfile doctorProfile;
    private User customerUser;
    private CustomerProfile customerProfile;

    @BeforeEach
    void setUp() {
        emailNotificationRepository.deleteAll();
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        specialSlotRequestRepository.deleteAll();
        availabilityRepository.deleteAll();
        addressRepository.deleteAll();
        customerProfileRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create doctor
        doctorUser = User.builder()
                .email("doctor@example.com")
                .passwordHash("hash")
                .role(Role.DOCTOR)
                .status(UserStatus.ACTIVE)
                .build();
        doctorUser = userRepository.save(doctorUser);

        doctorProfile = DoctorProfile.builder()
                .user(doctorUser)
                .firstName("Dr. John")
                .lastName("Watson")
                .status(com.version1.backend.enums.DoctorStatus.ACTIVE)
                .build();
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // 2. Create customer
        customerUser = User.builder()
                .email("customer@example.com")
                .passwordHash("hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        customerUser = userRepository.save(customerUser);

        customerProfile = CustomerProfile.builder()
                .user(customerUser)
                .firstName("Jane")
                .lastName("Doe")
                .build();
        customerProfile = customerProfileRepository.save(customerProfile);
    }

    @Test
    void testCreateSpecialRequestSuccess() {
        SpecialSlotRequestCreateDto dto = new SpecialSlotRequestCreateDto();
        dto.setDoctorId(doctorProfile.getId());
        dto.setRequestedDate(LocalDate.now().plusDays(3));
        dto.setStartTime(LocalTime.of(14, 0));
        dto.setEndTime(LocalTime.of(14, 45));
        dto.setNotes("Need custom consultation");

        SpecialSlotRequestResponseDto result = specialSlotRequestService.createSpecialRequest(customerUser.getId(), dto);

        assertNotNull(result);
        assertEquals(SpecialRequestStatus.PENDING, result.getStatus());
        assertEquals("Need custom consultation", result.getNotes());
        assertEquals(doctorProfile.getId(), result.getDoctorId());
    }

    @Test
    void testApproveSpecialRequestSuccess() {
        // Create request
        SpecialSlotRequestCreateDto dto = new SpecialSlotRequestCreateDto();
        dto.setDoctorId(doctorProfile.getId());
        dto.setRequestedDate(LocalDate.now().plusDays(3));
        dto.setStartTime(LocalTime.of(14, 0));
        dto.setEndTime(LocalTime.of(14, 45));
        dto.setNotes("Need custom consultation");

        SpecialSlotRequestResponseDto created = specialSlotRequestService.createSpecialRequest(customerUser.getId(), dto);

        // Approve request
        SpecialRequestStatusUpdateDto updateDto = new SpecialRequestStatusUpdateDto(SpecialRequestStatus.APPROVED);
        SpecialSlotRequestResponseDto approved = specialSlotRequestService.updateRequestStatus(
                doctorUser.getId(), created.getId(), updateDto);

        assertEquals(SpecialRequestStatus.APPROVED, approved.getStatus());

        // Verify custom availability was created
        var availabilities = availabilityRepository.findByDoctorIdAndIsActiveTrue(doctorProfile.getId());
        assertEquals(1, availabilities.size());
        assertEquals(LocalDate.now().plusDays(3), availabilities.get(0).getAvailabilityDate());
        assertEquals(LocalTime.of(14, 0), availabilities.get(0).getStartTime());

        // Verify PENDING appointment was created
        var appointments = appointmentRepository.findByCustomerId(customerProfile.getId());
        assertEquals(1, appointments.size());
        assertEquals(AppointmentStatus.PENDING, appointments.get(0).getStatus());

        // Verify PENDING payment was created
        var payments = paymentRepository.findAll();
        assertEquals(1, payments.size());
        assertEquals(PaymentStatus.PENDING, payments.get(0).getStatus());
    }

    @Test
    void testRejectSpecialRequestSuccess() {
        // Create request
        SpecialSlotRequestCreateDto dto = new SpecialSlotRequestCreateDto();
        dto.setDoctorId(doctorProfile.getId());
        dto.setRequestedDate(LocalDate.now().plusDays(3));
        dto.setStartTime(LocalTime.of(14, 0));
        dto.setEndTime(LocalTime.of(14, 45));
        dto.setNotes("Need custom consultation");

        SpecialSlotRequestResponseDto created = specialSlotRequestService.createSpecialRequest(customerUser.getId(), dto);

        // Reject request
        SpecialRequestStatusUpdateDto updateDto = new SpecialRequestStatusUpdateDto(SpecialRequestStatus.REJECTED);
        SpecialSlotRequestResponseDto rejected = specialSlotRequestService.updateRequestStatus(
                doctorUser.getId(), created.getId(), updateDto);

        assertEquals(SpecialRequestStatus.REJECTED, rejected.getStatus());

        // Verify no availability, appointment, or payment was created
        assertTrue(availabilityRepository.findByDoctorIdAndIsActiveTrue(doctorProfile.getId()).isEmpty());
        assertTrue(appointmentRepository.findByCustomerId(customerProfile.getId()).isEmpty());
        assertTrue(paymentRepository.findAll().isEmpty());
    }
}
