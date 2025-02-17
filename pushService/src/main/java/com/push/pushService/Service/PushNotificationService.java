package com.push.pushService.Service;

import com.google.firebase.messaging.*;
import com.push.pushService.dto.NotificationContent;
import com.push.pushService.dto.Priority;
import com.push.pushService.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PushNotificationService {


    private static final String SERVER_KEY = "FCM_SERVER_KEY"; // Get from Firebase Console
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private static final int MAX_RETRIES = 5; // Maximum number of retries
    private static final long BACKOFF_INTERVAL = 1000; // Initial backoff interval in milliseconds (1 second)

    @KafkaListener(topics = "push-topic", groupId = "push-service-group", containerFactory = "pushKafkaListenerFactory")
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

            NotificationContent content = new NotificationContent(header, messageContent, imageUrl, ctaUrl);

            NotificationMessage notification = new NotificationMessage(userId, channelStr, content, priority, isBatch);

            log.info("Notification created: {}", notification);

            if (notification.isBatch()) {
              //  Send batch
            } else {
               // Send single
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void sendPushNotification(String to, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(to)  // FCM token
                    .setNotification(notification)
                    .build();

            // Retry logic with exponential backoff
            boolean success = sendWithRetries(message, MAX_RETRIES, BACKOFF_INTERVAL);

            if (success) {
                System.out.println("Push notification sent successfully!");
            } else {
                System.err.println("Failed to send notification after retries");
            }
        } catch (Exception e) {
            System.err.println("Error sending push notification: " + e.getMessage());
        }
    }

    public void sendBulkPushNotifications(List<String> tokens, String title, String body) {
        try {
            // Prepare messages for all tokens
            List<Message> messages = tokens.stream().map(token -> {
                Notification notification = Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build();

                return Message.builder()
                        .setToken(token)
                        .setNotification(notification)
                        .build();
            }).collect(Collectors.toList());

            // Send bulk notifications
            BatchResponse batchResponse = FirebaseMessaging.getInstance().sendAll(messages);

            // Handle individual message results
            for (SendResponse response : batchResponse.getResponses()) {
                if (response.isSuccessful()) {
                    System.out.println("Message sent successfully: " + response.getMessageId());
                } else {
                    System.err.println("Message failed: " + response.getException().getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending bulk push notifications: " + e.getMessage());
        }
    }

    /**
     * Retries sending the push notification with exponential backoff.
     *
     * @param message   The message to be sent.
     * @param retries   The number of retries.
     * @param backoff   The initial backoff interval.
     * @return true if the message is successfully sent, false otherwise.
     */
    private boolean sendWithRetries(Message message, int retries, long backoff) {
        int attempts = 0;
        while (attempts < retries) {
            try {
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("Push notification sent successfully: " + response);
                return true; // Successful send
            } catch (Exception e) {
                attempts++;
                System.err.println("Error sending push notification (attempt " + attempts + "): " + e.getMessage());
                if (attempts >= retries) {
                    return false; // Max retries reached, failure
                }

                // Exponential backoff before retry
                long waitTime = backoff * (long) Math.pow(2, attempts);
                try {
                    TimeUnit.MILLISECONDS.sleep(waitTime); // Sleep before retrying
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // Restore interrupted state
                }
            }
        }
        return false; // If all attempts failed
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

    private String cleanString(String message) {
        if (message == null) return "";

        return message.replaceAll("\\\\", "")  // Remove any backslashes
                .replaceAll("\\n", "")   // Remove new lines
                .replaceAll("\\r", "")   // Remove carriage returns
                .trim();                 // Trim leading and trailing spaces
    }
}

