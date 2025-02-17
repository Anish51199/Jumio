package com.notification_service.notification_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.notification_service.dto.NotificationContent;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JsonConverter implements AttributeConverter<NotificationContent, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(NotificationContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON", e);
        }
    }

    @Override
    public NotificationContent convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, NotificationContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }
}

