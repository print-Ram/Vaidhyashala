package com.version1.backend.repository;

import com.version1.backend.enums.DoctorStatus;
import com.version1.backend.pojo.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {

    /** Find a doctor's profile by their User account ID. */
    Optional<DoctorProfile> findByUserId(UUID userId);

    /** Find all doctors with a specific status (e.g. PENDING_APPROVAL for admin review). */
    List<DoctorProfile> findByStatus(DoctorStatus status);

    /** Check whether a User already has a DoctorProfile (prevent duplicate registration). */
    boolean existsByUserId(UUID userId);
}
