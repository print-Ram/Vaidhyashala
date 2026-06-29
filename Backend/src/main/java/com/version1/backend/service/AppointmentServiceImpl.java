package com.version1.backend.service;

import com.version1.backend.dto.AppointmentCreateDto;
import com.version1.backend.dto.CalendarEventResult;
import com.version1.backend.exception.CustomException;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.pojo.*;
import com.version1.backend.repository.AppointmentRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.repository.EmailNotificationRepository;
import com.version1.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.version1.backend.dto.AppointmentResponseDto;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Override
    @Transactional
    public Appointment createAppointment(UUID customerUserId, AppointmentCreateDto dto) {
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        User provider = userRepository.findById(dto.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + dto.getProviderId()));

        if (provider.getRole() != Role.PROVIDER) {
            throw new CustomException("Selected user is not a provider", HttpStatus.BAD_REQUEST);
        }

        // 1. Check for overlapping appointments
        List<Appointment> overlaps = appointmentRepository.findOverlappingAppointments(
                dto.getProviderId(),
                dto.getStartTime(),
                dto.getEndTime(),
                AppointmentStatus.CONFIRMED
        );

        if (!overlaps.isEmpty()) {
            throw new CustomException("This slot is already booked for the provider", HttpStatus.CONFLICT);
        }

        // 2. Create the internal appointment record
        Appointment appointment = Appointment.builder()
                .customer(customer)
                .provider(provider)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(AppointmentStatus.PENDING)
                .description(dto.getDescription())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 3. Integrate with Google Calendar API (using Service Account to modify organization calendar)
        String meetLink = null;
        try {
            CalendarEventResult calResult = googleCalendarService.createEvent(
                    customer.getUser().getEmail(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getDescription()
            );
            savedAppointment.setGoogleCalendarEventId(calResult.getEventId());
            savedAppointment.setMeetLink(calResult.getMeetLink());
            meetLink = calResult.getMeetLink();
            savedAppointment.setStatus(AppointmentStatus.CONFIRMED);
            savedAppointment = appointmentRepository.save(savedAppointment);
        } catch (Exception e) {
            throw new CustomException("Failed to schedule Google Calendar event: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 4. Trigger Asynchronous Confirmation Email
        String customerEmail = customer.getUser().getEmail();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        emailService.sendAppointmentConfirmationEmail(customerEmail, customerName, dto.getStartTime(), meetLink);

        // 5. Schedule 24-Hour Reminder notification entry in database
        LocalDateTime reminderTime = dto.getStartTime().minusHours(24);
        EmailNotification reminder = EmailNotification.builder()
                .appointment(savedAppointment)
                .type(NotificationType.REMINDER)
                .recipientEmail(customerEmail)
                .status(NotificationStatus.PENDING)
                .scheduledSendTime(reminderTime)
                .build();
        emailNotificationRepository.save(reminder);

        return savedAppointment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByCustomerUserId(UUID customerUserId) {
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        return appointmentRepository.findByCustomerId(customer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByProviderId(UUID providerId) {
        return appointmentRepository.findByProviderId(providerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto.ProviderInfo> getAllProviders() {
        return userRepository.findByRole(Role.PROVIDER).stream()
                .map(user -> new AppointmentResponseDto.ProviderInfo(user.getId(), user.getEmail(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
