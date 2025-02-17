package com.notification_service.notification_service.dto;

import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String userId;
    private NotificationContent content;
    private NotificationChannel channel;
    private LocalDateTime scheduledTime;
    private Priority priority;
    private boolean isBatch;
}

