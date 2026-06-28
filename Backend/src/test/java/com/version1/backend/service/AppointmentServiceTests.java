package com.version1.backend.service;

import com.version1.backend.dto.AppointmentCreateDto;
import com.version1.backend.exception.CustomException;
import com.version1.backend.pojo.*;
import com.version1.backend.repository.AddressRepository;
import com.version1.backend.repository.AppointmentRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.repository.EmailNotificationRepository;
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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@SpringBootTest
@ActiveProfiles("local")
@EnableAutoConfiguration(exclude = {
    org.springframework.cloud.autoconfigure.RefreshAutoConfiguration.class
})
public class AppointmentServiceTests {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

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
        appointmentRepository.deleteAll();
        addressRepository.deleteAll();
        customerProfileRepository.deleteAll();
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
    void testCreateAppointmentSuccess() {
        Mockito.when(googleCalendarService.createEvent(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyString()))
                .thenReturn("gcal-event-123");

        AppointmentCreateDto dto = new AppointmentCreateDto();
        dto.setProviderId(provider.getId());
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        dto.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(45));
        dto.setDescription("General checkup");

        Appointment appointment = appointmentService.createAppointment(customerUser.getId(), dto);

        assertNotNull(appointment);
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals("gcal-event-123", appointment.getGoogleCalendarEventId());
    }

    @Test
    void testCreateAppointmentOverlapConflict() {
        Mockito.when(googleCalendarService.createEvent(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyString()))
                .thenReturn("gcal-event-123");

        LocalDateTime time = LocalDateTime.now().plusDays(2);

        AppointmentCreateDto firstAppointment = new AppointmentCreateDto();
        firstAppointment.setProviderId(provider.getId());
        firstAppointment.setStartTime(time);
        firstAppointment.setEndTime(time.plusMinutes(45));
        firstAppointment.setDescription("First consultation");

        appointmentService.createAppointment(customerUser.getId(), firstAppointment);

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
