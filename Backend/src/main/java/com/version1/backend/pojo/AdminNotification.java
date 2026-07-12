package com.version1.backend.pojo;

import com.version1.backend.enums.AdminNotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AdminNotification represents an inbox item for the PROVIDER (clinic admin).
 * Created automatically when doctors register, opt out, or need re-approval.
 */
@Entity
@Table(name = "admin_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The type of event that triggered this notification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminNotificationType type;

    /** The DoctorProfile UUID that this notification relates to. */
    @Column(name = "reference_doctor_id", nullable = false)
    private UUID referenceDoctorId;

    /** Human-readable summary message for the admin. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Whether the admin has acknowledged / acted on this notification.
     * false = PENDING, true = ACKNOWLEDGED.
     */
    @Column(name = "is_acknowledged", nullable = false)
    @Builder.Default
    private boolean isAcknowledged = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
