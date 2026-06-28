package com.version1.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "no-reply@vaidhyashala.com";

    @Override
    @Async
    public void sendAppointmentConfirmationEmail(String toEmail, String customerName, LocalDateTime dateTime, String meetLink) {
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"));
        String subject = "Appointment Confirmed - Vaidhyashala";

        String meetSection = buildMeetLinkSection(meetLink,
                "Your Google Meet link is ready. You can join directly at the scheduled time:");

        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<div style='background: linear-gradient(135deg, #1a73e8, #0d47a1); padding: 24px; border-radius: 12px 12px 0 0;'>"
                + "<h1 style='color: white; margin: 0; font-size: 22px;'>✅ Appointment Confirmed</h1>"
                + "<p style='color: rgba(255,255,255,0.85); margin: 6px 0 0;'>Vaidhyashala Wellness Consultation</p>"
                + "</div>"
                + "<div style='background: #f9f9f9; padding: 28px; border-radius: 0 0 12px 12px; border: 1px solid #e0e0e0;'>"
                + "<p style='font-size: 16px; color: #333;'>Dear <strong>" + customerName + "</strong>,</p>"
                + "<p style='color: #555; line-height: 1.6;'>Your appointment has been successfully scheduled and confirmed for:</p>"
                + "<div style='background: white; border-left: 4px solid #1a73e8; padding: 16px 20px; border-radius: 6px; margin: 16px 0;'>"
                + "<p style='margin: 0; font-size: 18px; font-weight: bold; color: #1a73e8;'>📅 " + formattedDate + "</p>"
                + "</div>"
                + meetSection
                + "<p style='color: #555; line-height: 1.6; margin-top: 24px;'>If you have any questions, feel free to reach out to us.</p>"
                + "<br/>"
                + "<p style='color: #555;'>Warm regards,<br/><strong>Vaidhyashala Team</strong></p>"
                + "</div>"
                + "</div>";

        sendHtmlMail(toEmail, subject, content);
    }

    @Override
    @Async
    public void sendAppointmentReminderEmail(String toEmail, String customerName, LocalDateTime dateTime, String meetLink) {
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"));
        String subject = "Reminder: Upcoming Appointment - Vaidhyashala";

        String meetSection = buildMeetLinkSection(meetLink,
                "Click the button below to join your session when it's time:");

        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<div style='background: linear-gradient(135deg, #f57c00, #e65100); padding: 24px; border-radius: 12px 12px 0 0;'>"
                + "<h1 style='color: white; margin: 0; font-size: 22px;'>⏰ Appointment Reminder</h1>"
                + "<p style='color: rgba(255,255,255,0.85); margin: 6px 0 0;'>Vaidhyashala Wellness Consultation</p>"
                + "</div>"
                + "<div style='background: #f9f9f9; padding: 28px; border-radius: 0 0 12px 12px; border: 1px solid #e0e0e0;'>"
                + "<p style='font-size: 16px; color: #333;'>Dear <strong>" + customerName + "</strong>,</p>"
                + "<p style='color: #555; line-height: 1.6;'>This is a friendly reminder that your consultation is coming up on:</p>"
                + "<div style='background: white; border-left: 4px solid #f57c00; padding: 16px 20px; border-radius: 6px; margin: 16px 0;'>"
                + "<p style='margin: 0; font-size: 18px; font-weight: bold; color: #f57c00;'>📅 " + formattedDate + "</p>"
                + "</div>"
                + meetSection
                + "<p style='color: #555; line-height: 1.6; margin-top: 24px;'>Please be ready 5 minutes before your scheduled time.</p>"
                + "<br/>"
                + "<p style='color: #555;'>Warm regards,<br/><strong>Vaidhyashala Team</strong></p>"
                + "</div>"
                + "</div>";

        sendHtmlMail(toEmail, subject, content);
    }

    private String buildMeetLinkSection(String meetLink, String introText) {
        if (meetLink != null && !meetLink.isBlank()) {
            return "<div style='background: #e8f0fe; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center;'>"
                    + "<p style='margin: 0 0 12px; color: #444; font-size: 14px;'>" + introText + "</p>"
                    + "<a href='" + meetLink + "' "
                    + "style='display: inline-block; background: #1a73e8; color: white; padding: 14px 28px; "
                    + "border-radius: 8px; text-decoration: none; font-size: 16px; font-weight: bold; letter-spacing: 0.3px;'>"
                    + "📹 Join Google Meet"
                    + "</a>"
                    + "<p style='margin: 12px 0 0; font-size: 12px; color: #777;'>Or copy this link: "
                    + "<a href='" + meetLink + "' style='color: #1a73e8;'>" + meetLink + "</a></p>"
                    + "</div>";
        } else {
            return "<p style='color: #555; line-height: 1.6;'>A Google Calendar invite has been added to your calendar. "
                    + "Please check your calendar for the meeting details.</p>";
        }
    }

    private void sendHtmlMail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
