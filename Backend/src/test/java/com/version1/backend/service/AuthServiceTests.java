package com.version1.backend.service;

import com.version1.backend.dto.UserRegistrationDto;
import com.version1.backend.pojo.User;
import com.version1.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.version1.backend.repository.AddressRepository;
import com.version1.backend.repository.AppointmentRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.repository.DoctorProfileRepository;
import com.version1.backend.repository.EmailNotificationRepository;
import com.version1.backend.repository.PaymentRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender mailSender; // Mock mail sender to avoid connection failures in tests

    @MockitoBean
    private GoogleCalendarService googleCalendarService; // Mock GCal api call

    @BeforeEach
    void setUp() {
        emailNotificationRepository.deleteAll();
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        addressRepository.deleteAll();
        customerProfileRepository.deleteAll();
        doctorProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUserSuccess() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setPassword("SecretPass123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreetAddress("123 Main St");
        dto.setCity("Springfield");
        dto.setState("IL");
        dto.setPostalCode("62701");
        dto.setCountry("USA");

        authService.register(dto);

        assertTrue(userRepository.existsByEmail("test@example.com"));
        User user = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(user);
        assertTrue(passwordEncoder.matches("SecretPass123", user.getPasswordHash()));
    }
}
