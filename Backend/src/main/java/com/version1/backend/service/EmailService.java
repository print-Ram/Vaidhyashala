package com.version1.backend.service;

import java.time.LocalDateTime;

public interface EmailService {
    void sendAppointmentConfirmationEmail(String toEmail, String customerName, LocalDateTime dateTime, String meetLink);
    void sendAppointmentReminderEmail(String toEmail, String customerName, LocalDateTime dateTime, String meetLink);
}
