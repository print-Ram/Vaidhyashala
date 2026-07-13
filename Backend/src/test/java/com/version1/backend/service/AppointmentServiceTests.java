package com.version1.backend.service;

import com.version1.backend.dto.AppointmentCreateDto;
import com.version1.backend.dto.CalendarEventResult;
import com.version1.backend.exception.CustomException;
import com.version1.backend.pojo.*;
import com.version1.backend.enums.AppointmentStatus;
import com.version1.backend.enums.Role;
import com.version1.backend.enums.UserStatus;
import com.version1.backend.repository.AddressRepository;
import com.version1.backend.repository.AppointmentRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.repository.DoctorProfileRepository;
import com.version1.backend.repository.EmailNotificationRepository;
import com.version1.backend.repository.PaymentRepository;
import com.version1.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class AppointmentServiceTests {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @MockitoBean
    private GoogleCalendarService googleCalendarService;

    @MockitoBean
    private JavaMailSender mailSender;

    private User provider;
    private User customerUser;
    private CustomerProfile customerProfile;

    @BeforeEach
    void setUp() {
        emailNotificationRepository.deleteAll();
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        addressRepository.deleteAll();
        customerProfileRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create provider user
        provider = User.builder()
                .email("provider@example.com")
                .passwordHash("hash")
                .role(Role.PROVIDER)
                .status(UserStatus.ACTIVE)
                .build();
        provider = userRepository.save(provider);

        // 2. Create customer user
        customerUser = User.builder()
                .email("customer@example.com")
                .passwordHash("hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        customerUser = userRepository.save(customerUser);

        // 3. Create customer profile
        customerProfile = CustomerProfile.builder()
                .user(customerUser)
                .firstName("Jane")
                .lastName("Doe")
                .build();
        customerProfile = customerProfileRepository.save(customerProfile);
    }

    @Test
    void testCreateAppointmentSuccess() throws Exception {
        Mockito.when(googleCalendarService.createEvent(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyString()))
                .thenReturn(new CalendarEventResult("gcal-event-123", null));

        AppointmentCreateDto dto = new AppointmentCreateDto();
        dto.setProviderId(provider.getId());
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        dto.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(45));
        dto.setDescription("General checkup");

        Appointment appointment = appointmentService.createAppointment(customerUser.getId(), dto);

        assertNotNull(appointment);
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());

        // 1. Checkout
        Payment payment = paymentService.checkout(new com.version1.backend.dto.PaymentCheckoutDto(appointment.getId()));
        assertNotNull(payment);
        assertEquals(com.version1.backend.enums.PaymentStatus.PENDING, payment.getStatus());

        // 2. Verify payment
        Payment verifiedPayment = paymentService.verifyPayment(new com.version1.backend.dto.PaymentVerificationDto(payment.getId(), "tx-123"));
        assertEquals(com.version1.backend.enums.PaymentStatus.COMPLETED, verifiedPayment.getStatus());
        assertEquals("tx-123", verifiedPayment.getTransactionId());

        // 3. Reload appointment and verify it is CONFIRMED and has Google Calendar details
        Appointment updatedAppointment = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.CONFIRMED, updatedAppointment.getStatus());
        assertEquals("gcal-event-123", updatedAppointment.getGoogleCalendarEventId());
    }

    @Test
    void testCreateAppointmentOverlapConflict() throws Exception {
        Mockito.when(googleCalendarService.createEvent(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyString()))
                .thenReturn(new CalendarEventResult("gcal-event-123", null));

        LocalDateTime time = LocalDateTime.now().plusDays(2);

        AppointmentCreateDto firstAppointmentDto = new AppointmentCreateDto();
        firstAppointmentDto.setProviderId(provider.getId());
        firstAppointmentDto.setStartTime(time);
        firstAppointmentDto.setEndTime(time.plusMinutes(45));
        firstAppointmentDto.setDescription("First consultation");

        Appointment firstAppointment = appointmentService.createAppointment(customerUser.getId(), firstAppointmentDto);

        // Confirm first appointment via payment to block the slot
        Payment payment = paymentService.checkout(new com.version1.backend.dto.PaymentCheckoutDto(firstAppointment.getId()));
        paymentService.verifyPayment(new com.version1.backend.dto.PaymentVerificationDto(payment.getId(), "tx-123"));

        // Try to schedule another appointment during the same slot
        AppointmentCreateDto secondAppointment = new AppointmentCreateDto();
        secondAppointment.setProviderId(provider.getId());
        secondAppointment.setStartTime(time.plusMinutes(15));
        secondAppointment.setEndTime(time.plusMinutes(60));
        secondAppointment.setDescription("Overlap consultation");

        assertThrows(CustomException.class, () -> {
            appointmentService.createAppointment(customerUser.getId(), secondAppointment);
        });
    }
}
