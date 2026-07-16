package com.version1.backend.service;

import com.version1.backend.dto.AppointmentCreateDto;
import com.version1.backend.dto.CalendarEventResult;
import com.version1.backend.exception.CustomException;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.pojo.*;
import com.version1.backend.enums.AppointmentStatus;
import com.version1.backend.enums.NotificationStatus;
import com.version1.backend.enums.NotificationType;
import com.version1.backend.enums.Role;
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

        User doctor = userRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + dto.getDoctorId()));

        // Accept both DOCTOR (individual doctors) and PROVIDER (admin/owner acting as doctor)
        if (doctor.getRole() != Role.DOCTOR && doctor.getRole() != Role.PROVIDER) {
            throw new CustomException("Selected user is not a doctor or provider", HttpStatus.BAD_REQUEST);
        }

        // 1. Check for overlapping appointments
        List<Appointment> overlaps = appointmentRepository.findOverlappingAppointments(
                dto.getDoctorId(),
                dto.getStartTime(),
                dto.getEndTime(),
                AppointmentStatus.CONFIRMED
        );

        if (!overlaps.isEmpty()) {
            throw new CustomException("This slot is already booked for the doctor", HttpStatus.CONFLICT);
        }

        // 2. First-appointment 50% discount logic
        //    Count all prior CONFIRMED appointments between this doctor and this customer.
        long priorAppointmentCount = appointmentRepository.countByDoctorUserIdAndCustomerIdAndStatus(
                doctor.getId(),
                customer.getId(),
                AppointmentStatus.CONFIRMED
        );
        boolean isFirstAppointment = (priorAppointmentCount == 0);

        // Capture consultation fee from the DTO (populated by the frontend from SlotDto)
        // Apply 50% discount if first appointment; otherwise use any day-offer from DTO
        java.math.BigDecimal consultationFee = dto.getConsultationFee();
        int discountPercent = isFirstAppointment ? 50 : dto.getOfferPercent();

        // 3. Create the internal appointment record
        Appointment appointment = Appointment.builder()
                .customer(customer)
                .doctor(doctor)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(AppointmentStatus.PENDING)
                .description(dto.getDescription())
                .consultationFee(consultationFee)
                .discountPercent(discountPercent)
                .isFirstAppointment(isFirstAppointment)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

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
    public List<Appointment> getAppointmentsByDoctorId(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto.DoctorInfo> getAllDoctors() {
        List<User> doctors = new java.util.ArrayList<>(userRepository.findByRole(Role.DOCTOR));
        doctors.addAll(userRepository.findByRole(Role.PROVIDER));
        return doctors.stream()
                .filter(user -> user.getEmail() != null && !user.getEmail().trim().isEmpty())
                .map(user -> new AppointmentResponseDto.DoctorInfo(user.getId(), user.getEmail(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
