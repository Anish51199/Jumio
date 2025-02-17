package com.notification_service.notification_service.dto;

import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String userId;
    private NotificationChannel channel;
    private NotificationContent content;
    private Priority priority;
    private boolean isBath;
}

