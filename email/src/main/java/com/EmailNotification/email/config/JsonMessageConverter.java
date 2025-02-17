package com.EmailNotification.email.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;

// Add this custom message converter
public class JsonMessageConverter implements MessageConverter {
    private final ObjectMapper objectMapper;

    public JsonMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
        String payload = (String) message.getPayload();
        try {
            return objectMapper.readValue(payload, targetClass);
        } catch (IOException e) {
            throw new MessageConversionException("Failed to convert message payload", e);
        }
    }

    @Override
    public Message<?> toMessage(Object payload, MessageHeaders headers) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            return MessageBuilder.withPayload(json).copyHeaders(headers).build();
        } catch (JsonProcessingException e) {
            throw new MessageConversionException("Failed to convert payload to JSON", e);
        }
    }
}
