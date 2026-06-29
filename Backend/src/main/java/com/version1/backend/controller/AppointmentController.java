package com.version1.backend.controller;

import com.version1.backend.dto.AppointmentCreateDto;
import com.version1.backend.dto.AppointmentResponseDto;
import com.version1.backend.pojo.Appointment;
import com.version1.backend.security.UserPrincipal;
import com.version1.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AppointmentCreateDto createDto) {
        Appointment appointment = appointmentService.createAppointment(userPrincipal.getId(), createDto);
        // Customers booking never need to see customer info in the response
        return new ResponseEntity<>(toDto(appointment, false), HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/appointments
     * PROVIDER: Returns ALL appointments with full customer info.
     * CUSTOMER: Returns only their own appointments (meet link visible, no other customer's data).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isProvider = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER"));
        List<Appointment> appointments = isProvider
                ? appointmentService.getAllAppointments()
                : appointmentService.getAppointmentsByCustomerUserId(userPrincipal.getId());

        return ResponseEntity.ok(appointments.stream()
                .map(a -> toDto(a, isProvider))
                .collect(Collectors.toList()));
    }

    /**
     * GET /api/v1/appointments/me
     * PROVIDER: Returns appointments booked with them (with customer info).
     * CUSTOMER: Returns their own appointments (with meet link).
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isProvider = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER"));
        List<Appointment> appointments = isProvider
                ? appointmentService.getAppointmentsByProviderId(userPrincipal.getId())
                : appointmentService.getAppointmentsByCustomerUserId(userPrincipal.getId());

        return ResponseEntity.ok(appointments.stream()
                .map(a -> toDto(a, isProvider))
                .collect(Collectors.toList()));
    }

    /**
     * GET /api/v1/appointments/providers
     * Returns list of all available providers (id, name, email) for booking dropdowns.
     */
    @GetMapping("/providers")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<List<AppointmentResponseDto.ProviderInfo>> getProviders() {
        return ResponseEntity.ok(appointmentService.getAllProviders());
    }

    /**
     * Maps an Appointment entity to AppointmentResponseDto.
     * @param appointment   The appointment entity.
     * @param includeCustomer Whether to include customer info (PROVIDER only).
     */
    private AppointmentResponseDto toDto(Appointment appointment, boolean includeCustomer) {
        AppointmentResponseDto.ProviderInfo providerInfo = new AppointmentResponseDto.ProviderInfo(
                appointment.getProvider().getId(),
                appointment.getProvider().getEmail(), // Provider name is their email (User entity has no separate name field)
                appointment.getProvider().getEmail()
        );

        AppointmentResponseDto.CustomerInfo customerInfo = null;
        if (includeCustomer && appointment.getCustomer() != null) {
            customerInfo = new AppointmentResponseDto.CustomerInfo(
                    appointment.getCustomer().getId(),
                    appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName(),
                    appointment.getCustomer().getUser().getEmail()
            );
        }

        return AppointmentResponseDto.builder()
                .id(appointment.getId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .description(appointment.getDescription())
                .meetLink(appointment.getMeetLink())
                .googleCalendarEventId(appointment.getGoogleCalendarEventId())
                .createdAt(appointment.getCreatedAt())
                .provider(providerInfo)
                .customer(customerInfo)
                .build();
    }
}
