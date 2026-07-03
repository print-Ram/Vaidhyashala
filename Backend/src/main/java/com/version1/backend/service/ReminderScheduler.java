package com.version1.backend.service;

import com.version1.backend.pojo.CustomerProfile;
import com.version1.backend.pojo.EmailNotification;
import com.version1.backend.enums.NotificationStatus;
import com.version1.backend.repository.EmailNotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ReminderScheduler {

    @Autowired
    private EmailNotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "${app.scheduler.reminder-cron:0 */15 * * * *}") // Run every 15 minutes
    @Transactional
    public void sendReminders() {
        log.info("Reminder Scheduler execution started at {}", LocalDateTime.now());

        List<EmailNotification> pendingNotifications = notificationRepository
                .findByStatusAndScheduledSendTimeBefore(NotificationStatus.PENDING, LocalDateTime.now());

        log.info("Found {} pending reminders to dispatch.", pendingNotifications.size());

        for (EmailNotification notification : pendingNotifications) {
            try {
                CustomerProfile customer = notification.getAppointment().getCustomer();
                String customerName = customer.getFirstName() + " " + customer.getLastName();

                emailService.sendAppointmentReminderEmail(
                        notification.getRecipientEmail(),
                        customerName,
                        notification.getAppointment().getStartTime(),
                        notification.getAppointment().getMeetLink()
                );

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);

                log.info("Reminder sent successfully for appointment ID: {}", notification.getAppointment().getId());
            } catch (Exception e) {
                log.error("Failed to send reminder for notification ID: {}", notification.getId(), e);
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
            }
        }
    }
}
