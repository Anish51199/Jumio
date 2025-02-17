package com.notification_service.notification_service.entity;

import com.notification_service.notification_service.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
public class NotificationPreference {

    @Id
    @Column(name = "user_id") // Explicitly map the column
    private String userId; // manually assigned

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_notification_channels", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private Set<NotificationChannel> enabledChannels = new HashSet<>();

    public NotificationPreference(String userId, Set<NotificationChannel> enabledChannels) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty for NotificationPreference");
        }
        this.userId = userId;
        this.enabledChannels = enabledChannels;
    }
}


