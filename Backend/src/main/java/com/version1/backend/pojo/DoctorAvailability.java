package com.version1.backend.pojo;

import com.version1.backend.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DoctorAvailability defines a doctor's available time window for a specific date
 * OR a recurring weekday. The slot grid is derived by splitting the window into
 * slot_duration_minutes intervals at query time (no pre-generated slot rows).
 */
@Entity
@Table(name = "doctor_availabilities",
        indexes = {
            @Index(name = "idx_avail_doctor_date", columnList = "doctor_id, availability_date"),
            @Index(name = "idx_avail_doctor_dow",  columnList = "doctor_id, day_of_week")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * FK to doctor_profiles. Note: provider_id in appointments points to users.id.
     * doctor_profiles.user_id links back to that same users.id, so resolution is:
     * appointments.provider_id → users.id → doctor_profiles.user_id.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    /**
     * Specific calendar date this availability applies to.
     * If set, takes priority over day_of_week for that date.
     */
    @Column(name = "availability_date")
    private LocalDate availabilityDate;

    /**
     * Recurring weekday (e.g. "MONDAY", "TUESDAY").
     * Used as fallback when no specific date override exists.
     */
    @Column(name = "day_of_week", length = 10)
    private String dayOfWeek;

    /** Window start time, e.g. 09:00. */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Window end time, e.g. 13:00. */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Duration of each individual slot in minutes. Default: 30. */
    @Column(name = "slot_duration_minutes", nullable = false)
    @Builder.Default
    private int slotDurationMinutes = 30;

    /** Consultation fee for slots in this window (in INR or configured currency). */
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    /** Doctor-set demand label for this window. */
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_status", nullable = false)
    @Builder.Default
    private SlotStatus slotStatus = SlotStatus.NORMAL;

    /** Percentage discount for this window (0 = no offer). */
    @Column(name = "offer_percent", nullable = false)
    @Builder.Default
    private int offerPercent = 0;

    /** Human-readable offer label, e.g. "Festival Discount", "Weekend Special". */
    @Column(name = "offer_label")
    private String offerLabel;

    /** Whether this availability window is currently active. */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
