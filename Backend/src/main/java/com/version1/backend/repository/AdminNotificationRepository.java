package com.version1.backend.repository;

import com.version1.backend.pojo.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, UUID> {

    /** All unacknowledged notifications for the admin inbox. */
    List<AdminNotification> findByIsAcknowledgedFalseOrderByCreatedAtDesc();

    /** All notifications (for admin history view), newest first. */
    List<AdminNotification> findAllByOrderByCreatedAtDesc();

    /** All notifications related to a specific doctor. */
    List<AdminNotification> findByReferenceDoctorIdOrderByCreatedAtDesc(UUID doctorId);
}
