package com.notification_service.notification_service.entity;

import com.notification_service.notification_service.dto.NotificationContent;
import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.enums.NotificationStatus;
import com.notification_service.notification_service.enums.Priority;
import com.notification_service.notification_service.utils.JsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Convert(converter = JsonConverter.class)
    private NotificationContent content; // JSON field for message, image, etc.

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private Priority priority;

    private boolean isBatch;

    private LocalDateTime scheduledTime;  // When to send the notification
    private boolean recurring;            // If the notification repeats
    private String recurrencePattern;     // e.g., "DAILY", "WEEKLY", "MONTHLY"

    private LocalDateTime createdAt = LocalDateTime.now();

}

