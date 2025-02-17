package com.sms.smsservice.service;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.sms.smsservice.dto.NotificationContent;
import com.sms.smsservice.dto.Priority;
import com.sms.smsservice.dto.SMSNotificationMessage;
import com.sms.smsservice.entity.FailedSMSNotification;
import com.sms.smsservice.repository.FailedSmsNotificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class SMSService {

    private final UserProfileService userProfileService;
    private final FailedSmsNotificationRepository failedSMSNotificationRepository;
    private final SMSProviderService smsProviderService;

    @Value("${sms.retries}")
    private int maxRetries;

    @Value("${sms.batch-size}")
    private int batchSize;

    private final BlockingQueue<SMSNotificationMessage> smsQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void startBatchProcessor() {
        new Thread(this::processBatchQueue).start();
    }

    @KafkaListener(topics = "sms-topic", groupId = "sms-service-group", containerFactory = "smsKafkaListenerFactory")
    public void consumeSMSNotification(String message) {

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
            SMSNotificationMessage notification = new SMSNotificationMessage(userId, channelStr, content, priority, isBatch);

            log.info("Notification created: {}", notification);

            if (notification.isBatch()) {
                smsQueue.offer(notification);
            } else {
                sendSMSWithRetries(notification);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void processBatchQueue() {
        while (true) {
            try {
                if (smsQueue.size() >= batchSize) {
                    List<SMSNotificationMessage> batch = List.copyOf(smsQueue);
                    smsQueue.clear();
                    batch.sort(Comparator.comparing(SMSNotificationMessage::getPriority).reversed());
                    batch.forEach(this::sendSMSWithRetries);
                }
                Thread.sleep(3000); // Process every 3 seconds
            } catch (InterruptedException e) {
                log.error("Batch processing interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Async
    public void sendSMSWithRetries(SMSNotificationMessage request) {
        int attempt = 0;
        boolean success = false;
        String phoneNumber = userProfileService.getPhoneNumberByUserId(request.getUserId());
        log.info("Phone Number: {}", phoneNumber);
        while (attempt < maxRetries && !success) {
            attempt++;
            try {
                sendSMS(phoneNumber, request.getContent().getMessage());
                success = true;
                log.info("SMS sent successfully to {} on attempt {}", phoneNumber, attempt);
            } catch (Exception e) {
                log.error("Failed to send SMS on attempt {}: {}", attempt, e.getMessage());
                if (attempt == maxRetries) {
                    logFailedSMS(request, phoneNumber, e.getMessage());
                }
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);  // 1 2 4 8
                } catch (InterruptedException interruptedException) {
                    log.warn("Retry interrupted, stopping further attempts");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void sendSMS(String phoneNumber, String message) throws UnirestException {
        log.info("Sending SMS to {}: {}", phoneNumber, message);
        smsProviderService.sendSms(phoneNumber, message);
    }

    public void logFailedSMS(SMSNotificationMessage request, String phoneNumber, String error) {
        FailedSMSNotification failedSMS = new FailedSMSNotification(
                null,
                request.getUserId(),
                phoneNumber,
                request.getContent().getMessage(),
                error,
                LocalDateTime.now()
        );
        failedSMSNotificationRepository.save(failedSMS);
        log.info("Failed SMS logged for {}", phoneNumber);
    }

    private String extractValue(String message, String startDelimiter, String endDelimiter) {
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

    public String cleanString(String message) {
        if (message == null) return "";

        return message.replaceAll("\\\\", "")  // Remove any backslashes
                .replaceAll("\\n", "")   // Remove new lines
                .replaceAll("\\r", "")   // Remove carriage returns
                .trim();                 // Trim leading and trailing spaces
    }
}
