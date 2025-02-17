package com.notification_service.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private List<NotificationPreferenceRequest> preferences; // List of preferences
}

