package com.version1.backend.repository;

import com.version1.backend.pojo.EmailNotification;
import com.version1.backend.pojo.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, UUID> {
    List<EmailNotification> findByStatusAndScheduledSendTimeBefore(NotificationStatus status, LocalDateTime time);
}
