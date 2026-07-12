package com.version1.backend.pojo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.version1.backend.enums.AppointmentStatus;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(name = "google_calendar_event_id")
    private String googleCalendarEventId;

    @Column(name = "meet_link")
    private String meetLink;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Consultation fee captured at the time of booking (snapshot from DoctorAvailability).
     * Immutable after booking to preserve pricing history.
     */
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    /**
     * Discount percentage applied at booking time.
     * 50 if this is the patient's first appointment with this doctor.
     * Otherwise equals the availability window's offerPercent (0 if none).
     */
    @Column(name = "discount_percent")
    @Builder.Default
    private Integer discountPercent = 0;

    public int getDiscountPercent() {
        return discountPercent != null ? discountPercent : 0;
    }

    /**
     * True when this is the customer's very first appointment with this doctor.
     * Automatically set during booking to trigger the 50% first-appointment discount.
     */
    @Column(name = "is_first_appointment", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean isFirstAppointment = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
