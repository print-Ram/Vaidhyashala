package com.version1.backend.service;

import com.version1.backend.dto.SpecialRequestStatusUpdateDto;
import com.version1.backend.dto.SpecialSlotRequestCreateDto;
import com.version1.backend.dto.SpecialSlotRequestResponseDto;
import com.version1.backend.enums.AppointmentStatus;
import com.version1.backend.enums.PaymentStatus;
import com.version1.backend.enums.SlotStatus;
import com.version1.backend.enums.SpecialRequestStatus;
import com.version1.backend.exception.CustomException;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.pojo.Appointment;
import com.version1.backend.pojo.CustomerProfile;
import com.version1.backend.pojo.DoctorAvailability;
import com.version1.backend.pojo.DoctorProfile;
import com.version1.backend.pojo.Payment;
import com.version1.backend.pojo.SpecialSlotRequest;
import com.version1.backend.repository.AppointmentRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.repository.DoctorAvailabilityRepository;
import com.version1.backend.repository.DoctorProfileRepository;
import com.version1.backend.repository.PaymentRepository;
import com.version1.backend.repository.SpecialSlotRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpecialSlotRequestServiceImpl implements SpecialSlotRequestService {

    @Autowired
    private SpecialSlotRequestRepository specialSlotRequestRepository;

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

    @Override
    @Transactional
    public SpecialSlotRequestResponseDto createSpecialRequest(UUID customerUserId, SpecialSlotRequestCreateDto dto) {
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        DoctorProfile doctor = doctorProfileRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + dto.getDoctorId()));

        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
            throw new CustomException("Start time must be before end time", HttpStatus.BAD_REQUEST);
        }

        SpecialSlotRequest request = SpecialSlotRequest.builder()
                .customer(customer)
                .doctor(doctor)
                .requestedDate(dto.getRequestedDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .notes(dto.getNotes())
                .status(SpecialRequestStatus.PENDING)
                .build();

        return toDto(specialSlotRequestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialSlotRequestResponseDto> getDoctorRequests(UUID doctorUserId) {
        return specialSlotRequestRepository.findByDoctorUserId(doctorUserId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialSlotRequestResponseDto> getCustomerRequests(UUID customerUserId) {
        return specialSlotRequestRepository.findByCustomerUserId(customerUserId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SpecialSlotRequestResponseDto updateRequestStatus(UUID doctorUserId, UUID requestId, SpecialRequestStatusUpdateDto dto) {
        SpecialSlotRequest request = specialSlotRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Special request not found: " + requestId));

        if (!request.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new CustomException("You are not authorized to update this request", HttpStatus.FORBIDDEN);
        }

        if (request.getStatus() != SpecialRequestStatus.PENDING) {
            throw new CustomException("Request has already been processed", HttpStatus.BAD_REQUEST);
        }

        if (dto.getStatus() == SpecialRequestStatus.APPROVED) {
            // 1. Resolve standard fee for the doctor
            BigDecimal consultationFee = BigDecimal.valueOf(500.00); // default fallback
            List<DoctorAvailability> activeAvailabilities = availabilityRepository.findByDoctorIdAndIsActiveTrue(request.getDoctor().getId());
            for (DoctorAvailability avail : activeAvailabilities) {
                if (avail.getConsultationFee() != null && avail.getConsultationFee().compareTo(BigDecimal.ZERO) > 0) {
                    consultationFee = avail.getConsultationFee();
                    break;
                }
            }

            // 2. Calculate duration
            int durationMins = (int) Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

            // 3. Create DoctorAvailability window for this custom slot
            DoctorAvailability customAvailability = DoctorAvailability.builder()
                    .doctor(request.getDoctor())
                    .availabilityDate(request.getRequestedDate())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .slotDurationMinutes(durationMins > 0 ? durationMins : 30)
                    .consultationFee(consultationFee)
                    .slotStatus(SlotStatus.NORMAL)
                    .isActive(true)
                    .build();
            availabilityRepository.save(customAvailability);

            // 4. Create the PENDING appointment
            long priorCount = appointmentRepository.countByDoctorUserIdAndCustomerIdAndStatus(
                    request.getDoctor().getUser().getId(),
                    request.getCustomer().getId(),
                    AppointmentStatus.CONFIRMED
            );
            boolean isFirst = (priorCount == 0);
            int discountPercent = isFirst ? 50 : 0;

            Appointment appointment = Appointment.builder()
                    .customer(request.getCustomer())
                    .doctor(request.getDoctor().getUser())
                    .startTime(request.getRequestedDate().atTime(request.getStartTime()))
                    .endTime(request.getRequestedDate().atTime(request.getEndTime()))
                    .status(AppointmentStatus.PENDING)
                    .description("Special Slot: " + request.getNotes())
                    .consultationFee(consultationFee)
                    .discountPercent(discountPercent)
                    .isFirstAppointment(isFirst)
                    .build();
            Appointment savedAppointment = appointmentRepository.save(appointment);

            // 5. Create PENDING payment
            BigDecimal discountFactor = BigDecimal.valueOf(100 - discountPercent);
            BigDecimal finalAmount = consultationFee.multiply(discountFactor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            Payment payment = Payment.builder()
                    .appointment(savedAppointment)
                    .amount(finalAmount)
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            request.setStatus(SpecialRequestStatus.APPROVED);
        } else if (dto.getStatus() == SpecialRequestStatus.REJECTED) {
            request.setStatus(SpecialRequestStatus.REJECTED);
        }

        return toDto(specialSlotRequestRepository.save(request));
    }

    private SpecialSlotRequestResponseDto toDto(SpecialSlotRequest r) {
        String customerName = r.getCustomer().getFirstName() + (r.getCustomer().getLastName() != null ? " " + r.getCustomer().getLastName() : "");
        String doctorName = r.getDoctor().getFirstName() + (r.getDoctor().getLastName() != null ? " " + r.getDoctor().getLastName() : "");

        return SpecialSlotRequestResponseDto.builder()
                .id(r.getId())
                .customerId(r.getCustomer().getId())
                .customerName(customerName)
                .doctorId(r.getDoctor().getId())
                .doctorName(doctorName)
                .requestedDate(r.getRequestedDate())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .notes(r.getNotes())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
