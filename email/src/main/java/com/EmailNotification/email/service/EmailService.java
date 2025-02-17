package com.EmailNotification.email.service;

import com.EmailNotification.email.dto.NotificationContent;
import com.EmailNotification.email.dto.NotificationMessage;
import com.EmailNotification.email.dto.Priority;
import com.EmailNotification.email.entity.FailedNotification;
import com.EmailNotification.email.repository.FailedNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final FailedNotificationRepository failedNotificationRepository;
    private final ObjectMapper objectMapper;  // Inject ObjectMapper for JSON parsing
    private final UserProfileService userProfileService;

    private static final int MAX_RETRIES = 3;
    private static final int BATCH_SIZE = 10;  // Maximum number of emails in one batch

    private final List<NotificationMessage> notificationBatch = new ArrayList<>();  // Batch to accumulate notifications

    @KafkaListener(topics = "email-topic", groupId = "email-service-group", containerFactory = "notificationKafkaListenerFactory")
    public void consumeEmailNotification(String message) {
        log.info("Received message: {}", message);

        if (message == null || message.isEmpty()) {
            log.error("Received null or empty notification message!");
            return;
        }

        // Clean and parse the message
        message = cleanString(message);
        log.info("Cleaned message: {}", message);

        try {
            // Extract fields from the message
            String userId = extractValue(message, "\"userId\":\"", "\"");
            String channelStr = extractValue(message, "\"channel\":\"", "\"");
            String priorityStr = extractValue(message, "\"priority\":\"", "\"");
            Priority priority = Priority.valueOf(priorityStr.toUpperCase());
            boolean isBatch = Boolean.parseBoolean(extractValue(message, "\"isBatch\":\"", "\""));

            // Extract notification content
            String header = extractValue(message, "\"header\":\"", "\"");
            String messageContent = extractValue(message, "\"message\":\"", "\"");
            String imageUrl = extractValue(message, "\"imageUrl\":", ",");
            String ctaUrl = extractValue(message, "\"ctaUrl\":", "}");  // `ctaUrl` appears at the end of the message

            // Handle null values for imageUrl and ctaUrl
            if ("null".equals(imageUrl)) {
                imageUrl = null;
            }
            if ("null".equals(ctaUrl)) {
                ctaUrl = null;
            }

            // Create the NotificationContent object
            NotificationContent content = new NotificationContent(header, messageContent, imageUrl, ctaUrl);

            // Create the NotificationMessage object
            NotificationMessage notification = new NotificationMessage(userId, channelStr, content, priority, isBatch);

            log.info("Notification created: {}", notification);

            // Process notification based on whether it's a batch or normal
            if (notification.isBatch()) {
                notificationBatch.add(notification);  // Add to batch for later processing
                // If batch size is reached, process the batch
                if (notificationBatch.size() >= BATCH_SIZE) {
                    sendBatchEmails(notificationBatch);
                    notificationBatch.clear();  // Clear the batch after sending
                }
            } else {
                // Send email immediately for non-batch notification
                sendEmailAsync(notification);
            }

        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
        }
    }

    /**
     * Method to send emails asynchronously for non-batch notifications.
     *
     * @param notification The notification message.
     */
    public void sendEmailAsync(NotificationMessage notification) {
        try {
            String email = userProfileService.getEmailByUserId(notification.getUserId());
            if (email == null) {
                log.error("Failed to retrieve email for user {}", notification.getUserId());
                return;  // Skip if email not found
            }

            sendEmail(email, notification);  // Send the email
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    /**
     * Method to send a batch of notifications.
     *
     * @param batch The list of notifications to send in the batch.
     */
    public void sendBatchEmails(List<NotificationMessage> batch) {
        // Sort notifications by priority (HIGH -> MEDIUM -> LOW)
        batch.sort(Comparator.comparing(NotificationMessage::getPriority).reversed());

        // Send emails asynchronously for all notifications in the batch
        batch.forEach(this::sendEmailAsync);
        log.info("Batch emails sent successfully.");
    }

    /**
     * Method to send a single email.
     *
     * @param to The recipient email address.
     * @param message The content of the notification.
     */
    private void sendEmail(String to, NotificationMessage message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(message.getContent().getHeader());
        mailMessage.setText(message.getContent().getMessage());

        try {
            mailSender.send(mailMessage);
            log.info("Sent email to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            logFailedNotification(message.getUserId(), to, message.getContent().getHeader(), message.getContent().getMessage(), e.getMessage());
        }
    }

    /**
     * Method to log failed notifications.
     *
     * @param userId The user ID.
     * @param subject The email subject.
     * @param body The email body.
     * @param error The error message.
     */
    public void logFailedNotification(String userId, String email, String subject, String body, String error) {
        FailedNotification failedNotification = FailedNotification.builder()
                .userId(userId)
                .email(email)
                .subject(subject)
                .body(body)
                .errorMessage(error)
                .failedAt(LocalDateTime.now())
                .build();

        failedNotificationRepository.save(failedNotification);
        log.info("Failed email notification saved for userId: {}", userId);
    }

    /**
     * Utility method to extract a value from a JSON-like string.
     *
     * @param message The message string.
     * @param startDelimiter The start delimiter.
     * @param endDelimiter The end delimiter.
     * @return The extracted value or null if not found.
     */
    public String extractValue(String message, String startDelimiter, String endDelimiter) {
        if (message == null || startDelimiter == null || endDelimiter == null) {
            return null;
        }

        int startIndex = message.indexOf(startDelimiter);
        if (startIndex == -1) return null;

        startIndex += startDelimiter.length();
        int endIndex = message.indexOf(endDelimiter, startIndex);
        if (endIndex == -1) return null;

        return message.substring(startIndex, endIndex).trim();  // Extract and trim the value
    }

    /**
     * Method to clean the string by removing escape characters and extra whitespace.
     *
     * @param message The message to clean.
     * @return The cleaned message.
     */
    public String cleanString(String message) {
        if (message == null) return "";

        return message.replaceAll("\\\\", "")  // Remove any backslashes
                .replaceAll("\\n", "")   // Remove new lines
                .replaceAll("\\r", "")   // Remove carriage returns
                .trim();                 // Trim leading and trailing spaces
    }
}
