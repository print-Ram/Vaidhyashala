package com.version1.backend.service;

import java.time.LocalDateTime;

public interface EmailService {
    void sendAppointmentConfirmationEmail(String toEmail, String customerName, LocalDateTime dateTime, String meetLink);
    void sendAppointmentReminderEmail(String toEmail, String customerName, LocalDateTime dateTime, String meetLink);

    /**
     * Notifies the PROVIDER admin when a new doctor registers and needs approval.
     */
    void sendAdminDoctorRegistrationNotification(String doctorName, String doctorEmail,
                                                  String specialization, String department);

    /**
     * Notifies the PROVIDER admin when a doctor opts out of the scheme.
     */
    void sendAdminDoctorOptOutNotification(String doctorName, String doctorEmail, String reason);
}

