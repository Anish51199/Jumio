package com.notification_service.notification_service.dto;

import com.notification_service.notification_service.enums.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {
    private NotificationChannel channel;
    private boolean enabled;
}
