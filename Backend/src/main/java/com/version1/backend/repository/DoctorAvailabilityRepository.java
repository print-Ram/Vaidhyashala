package com.version1.backend.repository;

import com.version1.backend.pojo.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {

    /**
     * Find a specific-date override for a doctor (highest priority for slot generation).
     * Returns active records only.
     */
    Optional<DoctorAvailability> findByDoctorIdAndAvailabilityDateAndIsActiveTrue(
            UUID doctorId, LocalDate availabilityDate);

    /**
     * Fallback: find recurring weekday availability for a doctor.
     * Used when no specific-date override exists for the requested date.
     */
    Optional<DoctorAvailability> findByDoctorIdAndDayOfWeekAndAvailabilityDateIsNullAndIsActiveTrue(
            UUID doctorId, String dayOfWeek);

    /** All active availability windows for a doctor (for admin/doctor profile view). */
    List<DoctorAvailability> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    /**
     * Combined lookup: specific date OR matching weekday (for next-availability computation).
     * Returns all active windows for the doctor for a given date or its weekday name.
     */
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId " +
           "AND da.isActive = true " +
           "AND (da.availabilityDate = :date OR " +
           "     (da.availabilityDate IS NULL AND da.dayOfWeek = :dayOfWeek))")
    List<DoctorAvailability> findAvailabilityForDate(
            @Param("doctorId") UUID doctorId,
            @Param("date") LocalDate date,
            @Param("dayOfWeek") String dayOfWeek);

    // -----------------------------------------------------------------------
    // Queries by doctor's User.id (provider_id in appointments = user.id)
    // Allows SlotService to resolve availability directly from provider_id
    // without a separate DoctorProfile lookup.
    // -----------------------------------------------------------------------

    /**
     * Specific-date override — resolved via the doctor's User ID.
     * Priority 1 in slot resolution.
     */
    @Query("SELECT da FROM DoctorAvailability da " +
           "WHERE da.doctor.user.id = :userI AND da.availabilityDate = :date " +
           "AND da.isActive = true")
    Optional<DoctorAvailability> findByDoctorUserIdAndDate(
            @Param("userI") UUID doctorUserId,
            @Param("date") LocalDate date);

    /**
     * Recurring weekday rule — resolved via the doctor's User ID.
     * Priority 2 in slot resolution (fallback when no specific-date override).
     */
    @Query("SELECT da FROM DoctorAvailability da " +
           "WHERE da.doctor.user.id = :userI AND da.dayOfWeek = :weekday " +
           "AND da.availabilityDate IS NULL AND da.isActive = true")
    Optional<DoctorAvailability> findByDoctorUserIdAndWeekday(
            @Param("userI") UUID doctorUserId,
            @Param("weekday") String weekday);

    List<DoctorAvailability> findByDoctorId(UUID doctorId);
}

