package com.EmailNotification.email.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationChannel {
    EMAIL, SMS, PUSH;

    @JsonCreator
    public static NotificationChannel fromString(String value) {
        for (NotificationChannel channel : NotificationChannel.values()) {
            if (channel.name().equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown channel: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

