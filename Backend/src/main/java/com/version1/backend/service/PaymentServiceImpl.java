package com.version1.backend.service;

import com.version1.backend.dto.CalendarEventResult;
import com.version1.backend.dto.PaymentCheckoutDto;
import com.version1.backend.dto.PaymentVerificationDto;
import com.version1.backend.enums.AppointmentStatus;
import com.version1.backend.enums.NotificationStatus;
import com.version1.backend.enums.NotificationType;
import com.version1.backend.enums.PaymentStatus;
import com.version1.backend.exception.CustomException;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.pojo.Appointment;
import com.version1.backend.pojo.CustomerProfile;
import com.version1.backend.pojo.EmailNotification;
import com.version1.backend.pojo.Payment;
import com.version1.backend.payment.PaymentGateway;
import com.version1.backend.payment.PaymentGatewayOrder;
import com.version1.backend.payment.PaymentGatewayVerification;
import com.version1.backend.repository.AppointmentRepository;
import com.version1.backend.repository.EmailNotificationRepository;
import com.version1.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Autowired
    private PaymentGateway paymentGateway;

    @Override
    @Transactional
    public Payment checkout(PaymentCheckoutDto dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + dto.getAppointmentId()));

        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            throw new CustomException("Appointment is already confirmed", HttpStatus.BAD_REQUEST);
        }

        // Return existing payment if already created
        Payment payment = paymentRepository.findByAppointmentId(appointment.getId())
                .orElseGet(() -> {
                    BigDecimal baseFee = appointment.getConsultationFee() != null ? appointment.getConsultationFee() : BigDecimal.ZERO;
                    BigDecimal discountFactor = BigDecimal.valueOf(100 - appointment.getDiscountPercent());
                    BigDecimal amount = baseFee.multiply(discountFactor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    Payment pendingPayment = Payment.builder()
                            .appointment(appointment)
                            .amount(amount)
                            .status(PaymentStatus.PENDING)
                            .build();

                    return paymentRepository.save(pendingPayment);
                });

        if (payment.getGatewayOrderId() == null) {
            PaymentGatewayOrder order = paymentGateway.createOrder(payment);
            payment.setGateway(paymentGateway.getName());
            payment.setGatewayOrderId(order.orderId());
            payment = paymentRepository.save(payment);
        }

        return payment;
    }

    @Override
    @Transactional
    public Payment verifyPayment(PaymentVerificationDto dto) throws Exception {
        Payment payment = paymentRepository.findById(dto.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + dto.getPaymentId()));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return payment;
        }

        PaymentGatewayVerification verification = paymentGateway.verify(payment, dto.getTransactionId());
        if (!verification.successful()) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new CustomException("Payment verification failed", HttpStatus.BAD_REQUEST);
        }

        // Mark payment as completed only after gateway verification succeeds
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(verification.transactionId());
        Payment savedPayment = paymentRepository.save(payment);

        // Update appointment status to CONFIRMED
        Appointment appointment = payment.getAppointment();
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        CustomerProfile customer = appointment.getCustomer();

        // Integrate with Google Calendar API
        String meetLink = null;
        try {
            CalendarEventResult calResult = googleCalendarService.createEvent(
                    customer.getUser().getEmail(),
                    appointment.getStartTime(),
                    appointment.getEndTime(),
                    appointment.getDescription()
            );
            appointment.setGoogleCalendarEventId(calResult.getEventId());
            appointment.setMeetLink(calResult.getMeetLink());
            meetLink = calResult.getMeetLink();
        } catch (Exception e) {
            // Log calendar failure, but do not fail verification transaction since payment went through.
        }

        appointmentRepository.save(appointment);

        // Trigger Asynchronous Confirmation Email
        if (customer.getUser() != null && customer.getUser().getEmail() != null) {
            emailService.sendAppointmentConfirmationEmail(
                    customer.getUser().getEmail(),
                    customer.getFirstName() + " " + customer.getLastName(),
                    appointment.getStartTime(),
                    meetLink
            );

            // Schedule 24-Hour Reminder notification entry in database
            LocalDateTime reminderTime = appointment.getStartTime().minusHours(24);
            EmailNotification reminder = EmailNotification.builder()
                    .appointment(appointment)
                    .type(NotificationType.REMINDER)
                    .recipientEmail(customer.getUser().getEmail())
                    .status(NotificationStatus.PENDING)
                    .scheduledSendTime(reminderTime)
                    .build();
            emailNotificationRepository.save(reminder);
        }

        return savedPayment;
    }
}
