package com.notification_service.notification_service.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class NotificationPreferencesUpdateRequest {
    private String userId;
    private List<NotificationPreferenceRequest> preferences;
}

