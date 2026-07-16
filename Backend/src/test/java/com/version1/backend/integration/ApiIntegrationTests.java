package com.version1.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.version1.backend.dto.*;
import com.version1.backend.enums.*;
import com.version1.backend.pojo.*;
import com.version1.backend.repository.*;
import com.version1.backend.security.JwtTokenProvider;
import com.version1.backend.service.GoogleCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

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
    private SpecialSlotRequestRepository specialSlotRequestRepository;

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
    private String doctorToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

        // 1. Seed doctor
        doctorUser = User.builder()
                .email("doctor@example.com")
                .passwordHash("hash")
                .role(Role.DOCTOR)
                .status(UserStatus.ACTIVE)
                .build();
        doctorUser = userRepository.save(doctorUser);

        doctorProfile = DoctorProfile.builder()
                .user(doctorUser)
                .firstName("Dr. Watson")
                .lastName("Watson")
                .status(DoctorStatus.ACTIVE)
                .build();
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // 2. Seed customer
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

        // 3. Generate tokens
        doctorToken = tokenProvider.generateTokenFromEmail(doctorUser.getEmail());
        customerToken = tokenProvider.generateTokenFromEmail(customerUser.getEmail());
    }

    @Test
    void testDoctorConfigureDashboardApi() throws Exception {
        DoctorProfileDto profileDto = new DoctorProfileDto();
        profileDto.setFirstName("Dr. Watson Updated");
        profileDto.setDesignation("Cardiology Specialist");
        profileDto.setRegNo("DOC-998877");

        DoctorAvailabilityDto availabilityDto = new DoctorAvailabilityDto();
        availabilityDto.setAvailabilityDate(LocalDate.now().plusDays(2));
        availabilityDto.setStartTime(LocalTime.of(10, 0));
        availabilityDto.setEndTime(LocalTime.of(12, 0));
        availabilityDto.setSlotDurationMinutes(30);
        availabilityDto.setConsultationFee(BigDecimal.valueOf(600.00));

        DoctorConfigurationRequestDto requestDto = new DoctorConfigurationRequestDto();
        requestDto.setProfile(profileDto);
        requestDto.setAvailabilities(Collections.singletonList(availabilityDto));

        MockMultipartFile settingsPart = new MockMultipartFile(
                "settings",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(requestDto)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "profileImage",
                "watson.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy-image".getBytes()
        );

        var requestBuilder = MockMvcRequestBuilders.multipart("/api/v1/doctors/me/configure")
                .file(settingsPart)
                .file(imagePart)
                .header("Authorization", "Bearer " + doctorToken);

        // Modify builder method to PUT since multipart defaults to POST
        requestBuilder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // Verify changes persistent
        DoctorProfile updatedProfile = doctorProfileRepository.findById(doctorProfile.getId()).orElseThrow();
        assertEquals("Dr. Watson Updated", updatedProfile.getFirstName());
        assertEquals("Cardiology Specialist", updatedProfile.getDesignation());
        assertEquals("DOC-998877", updatedProfile.getRegNo());
        assertNotNull(updatedProfile.getProfileImageUrl());

        var slots = availabilityRepository.findByDoctorIdAndIsActiveTrue(doctorProfile.getId());
        assertEquals(1, slots.size());
        assertEquals(LocalTime.of(10, 0), slots.get(0).getStartTime());
    }

    @Test
    void testPaymentCheckoutAndVerifyApi() throws Exception {
        // Pre-create pending appointment
        Appointment appointment = Appointment.builder()
                .customer(customerProfile)
                .doctor(doctorUser)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .status(AppointmentStatus.PENDING)
                .description("Cardiology consultation")
                .consultationFee(BigDecimal.valueOf(500.00))
                .discountPercent(50)
                .isFirstAppointment(true)
                .build();
        appointment = appointmentRepository.save(appointment);

        // 1. Checkout
        PaymentCheckoutDto checkoutDto = new PaymentCheckoutDto(appointment.getId());
        String checkoutResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutDto))
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Payment checkoutPayment = objectMapper.readValue(checkoutResponse, Payment.class);
        assertNotNull(checkoutPayment.getId());
        assertEquals(PaymentStatus.PENDING, checkoutPayment.getStatus());
        assertEquals(0, BigDecimal.valueOf(250.00).compareTo(checkoutPayment.getAmount()));
        assertEquals("test", checkoutPayment.getGateway());
        assertNotNull(checkoutPayment.getGatewayOrderId());

        // 2. Verify
        PaymentVerificationDto verificationDto = new PaymentVerificationDto(checkoutPayment.getId(), "tx-razorpay-9988");
        String verifyResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verificationDto))
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Payment verifiedPayment = objectMapper.readValue(verifyResponse, Payment.class);
        assertEquals(PaymentStatus.COMPLETED, verifiedPayment.getStatus());
        assertEquals("tx-razorpay-9988", verifiedPayment.getTransactionId());

        // Verify appointment status updated to CONFIRMED
        Appointment updatedAppointment = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.CONFIRMED, updatedAppointment.getStatus());
    }

    @Test
    void testSpecialSlotRequestFlow() throws Exception {
        // 1. Customer submits a request
        SpecialSlotRequestCreateDto createDto = new SpecialSlotRequestCreateDto();
        createDto.setDoctorId(doctorProfile.getId());
        createDto.setRequestedDate(LocalDate.now().plusDays(5));
        createDto.setStartTime(LocalTime.of(15, 0));
        createDto.setEndTime(LocalTime.of(15, 45));
        createDto.setNotes("Need quick checkup");

        String createResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/special-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        SpecialSlotRequestResponseDto reqResponse = objectMapper.readValue(createResponse, SpecialSlotRequestResponseDto.class);
        assertNotNull(reqResponse.getId());
        assertEquals(SpecialRequestStatus.PENDING, reqResponse.getStatus());

        // 2. Doctor approves the request
        SpecialRequestStatusUpdateDto updateDto = new SpecialRequestStatusUpdateDto(SpecialRequestStatus.APPROVED);
        String updateResponse = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/special-requests/" + reqResponse.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SpecialSlotRequestResponseDto approvedResponse = objectMapper.readValue(updateResponse, SpecialSlotRequestResponseDto.class);
        assertEquals(SpecialRequestStatus.APPROVED, approvedResponse.getStatus());

        // Assert that DoctorAvailability is created
        var availabilities = availabilityRepository.findByDoctorIdAndIsActiveTrue(doctorProfile.getId());
        assertEquals(1, availabilities.size());
        assertEquals(LocalTime.of(15, 0), availabilities.get(0).getStartTime());

        // Assert that Appointment is created in PENDING status
        var appointments = appointmentRepository.findByCustomerId(customerProfile.getId());
        assertEquals(1, appointments.size());
        assertEquals(AppointmentStatus.PENDING, appointments.get(0).getStatus());

        // Assert that Payment is created in PENDING status
        var payments = paymentRepository.findAll();
        assertEquals(1, payments.size());
        assertEquals(PaymentStatus.PENDING, payments.get(0).getStatus());
    }
}
