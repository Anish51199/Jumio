package com.notification_service.notification_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationContent {
    private String header;
    private String message;
    private String imageUrl;
    private String ctaUrl;  // link
}

