package com.version1.backend.repository;

import com.version1.backend.pojo.Appointment;
import com.version1.backend.pojo.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByCustomerId(UUID customerId);
    List<Appointment> findByProviderId(UUID providerId);

    @Query("SELECT a FROM Appointment a WHERE a.provider.id = :providerId " +
           "AND a.status = :status " +
           "AND ((a.startTime >= :start AND a.startTime < :end) " +
           "OR (a.endTime > :start AND a.endTime <= :end) " +
           "OR (a.startTime <= :start AND a.endTime >= :end))")
    List<Appointment> findOverlappingAppointments(
            @Param("providerId") UUID providerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") AppointmentStatus status
    );
}
