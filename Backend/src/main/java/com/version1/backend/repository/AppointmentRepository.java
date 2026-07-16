package com.version1.backend.repository;

import com.version1.backend.pojo.Appointment;
import com.version1.backend.enums.AppointmentStatus;
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
    List<Appointment> findByDoctorId(UUID doctorId);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.status = :status " +
           "AND ((a.startTime >= :start AND a.startTime < :end) " +
           "OR (a.endTime > :start AND a.endTime <= :end) " +
           "OR (a.startTime <= :start AND a.endTime >= :end))")
    List<Appointment> findOverlappingAppointments(
            @Param("doctorId") UUID doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") AppointmentStatus status
    );

    /**
     * Count CONFIRMED appointments between a specific doctor and customer.
     * Used to determine if the upcoming booking is the customer's FIRST appointment
     * with this doctor, in order to apply the 50% first-appointment discount.
     */
    @Query("SELECT COUNT(a) FROM Appointment a " +
           "WHERE a.doctor.id = :doctorUserId " +
           "AND a.customer.id = :customerId " +
           "AND a.status = :status")
    long countByDoctorUserIdAndCustomerIdAndStatus(
            @Param("doctorUserId") UUID doctorUserId,
            @Param("customerId") UUID customerId,
            @Param("status") AppointmentStatus status
    );

    /**
     * Count CONFIRMED appointments for a doctor within a date-time range.
     * Used by the Next Availability service to compute booked slot counts
     * for the coming 3 days.
     */
    @Query("SELECT COUNT(a) FROM Appointment a " +
           "WHERE a.doctor.id = :doctorUserId " +
           "AND a.status = :status " +
           "AND a.startTime >= :rangeStart " +
           "AND a.startTime < :rangeEnd")
    long countByDoctorUserIdAndStatusAndStartTimeBetween(
            @Param("doctorUserId") UUID doctorUserId,
            @Param("status") AppointmentStatus status,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );

    /**
     * Fetch appointments for a doctor within a time window (for slot-grid overlap check).
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.doctor.id = :doctorUserId " +
           "AND a.status = :status " +
           "AND a.startTime >= :rangeStart " +
           "AND a.startTime < :rangeEnd")
    List<Appointment> findByDoctorUserIdAndStatusAndStartTimeBetween(
            @Param("doctorUserId") UUID doctorUserId,
            @Param("status") AppointmentStatus status,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );
}

