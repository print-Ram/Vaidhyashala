package com.version1.backend.service;

import com.version1.backend.dto.AppointmentCreateDto;
import com.version1.backend.dto.AppointmentResponseDto;
import com.version1.backend.pojo.Appointment;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    Appointment createAppointment(UUID customerUserId, AppointmentCreateDto dto);
    List<Appointment> getAppointmentsByCustomerUserId(UUID customerUserId);
    List<Appointment> getAppointmentsByDoctorId(UUID doctorId);
    List<Appointment> getAllAppointments();
    List<AppointmentResponseDto.DoctorInfo> getAllDoctors();
}
