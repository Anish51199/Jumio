package com.notification_service.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.notification_service.config.NotificationChannelConfig;
import com.notification_service.notification_service.dto.NotificationMessage;
import com.notification_service.notification_service.dto.NotificationRequest;
import com.notification_service.notification_service.entity.NotificationEntity;
import com.notification_service.notification_service.entity.NotificationPreference;
import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.enums.NotificationStatus;
import com.notification_service.notification_service.repository.NotificationPreferenceRepository;
import com.notification_service.notification_service.repository.NotificationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import java.util.*;

@Service
public class KafkaNotificationProducer {

    private final KafkaTemplate<Object, String> kafkaTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    // Dynamic channel-to-topic mapping
    private final Map<NotificationChannel, String> channelToTopicMap;

    public KafkaNotificationProducer(
            KafkaTemplate<Object, String> kafkaTemplate,
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository notificationPreferenceRepository,
            NotificationChannelConfig channelConfig) {

        this.kafkaTemplate = kafkaTemplate;
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;

        // Convert string keys from properties to enum
        this.channelToTopicMap = new EnumMap<>(NotificationChannel.class);
        channelConfig.getTopics().forEach((key, value) -> {
            NotificationChannel channel = NotificationChannel.valueOf(key.toUpperCase());
            channelToTopicMap.put(channel, value);
        });
    }

    // Send notification to a specific channel
    public void sendNotification(NotificationRequest request) {
        NotificationChannel channel = request.getChannel();
        String topic = channelToTopicMap.get(channel); ///  email-topic

        if (topic == null) {
            throw new IllegalArgumentException("Unsupported notification channel: " + channel);
        }

        // Save notification
        saveNotification(request, channel);

       if( request.getScheduledTime() == null) {
           // Send to Kafka
           NotificationMessage message = new NotificationMessage(request.getUserId(), channel, request.getContent(), request.getPriority(), request.isBatch());

           ObjectMapper objectMapper = new ObjectMapper(); // Bean
           try {
               String jsonMessage = objectMapper.writeValueAsString(message);
               kafkaTemplate.send(topic, jsonMessage);
           } catch (JsonProcessingException e) {
               e.printStackTrace();
           }
       }
    }

    // Send notification to all subscribed channels
    public void sendNotificationToAllChannels(NotificationRequest request) {
        Optional<NotificationPreference> preferenceOpt = notificationPreferenceRepository.findById(request.getUserId());

        if (preferenceOpt.isEmpty()) {
            throw new RuntimeException("User preferences not found");
        }

        Set<NotificationChannel> subscribedChannels = preferenceOpt.get().getEnabledChannels();

        if( request.getScheduledTime() == null) {
            for (NotificationChannel channel : subscribedChannels) {
                sendNotification(new NotificationRequest(request.getUserId(), request.getContent(), channel, request.getScheduledTime(), request.getPriority(), request.isBatch()));
            }
        }
    }

    // Save notification before sending
    public NotificationEntity saveNotification(NotificationRequest request, NotificationChannel channel) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(request.getUserId());
        notification.setContent(request.getContent());
        notification.setChannel(channel);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setScheduledTime(request.getScheduledTime() != null ? request.getScheduledTime() : LocalDateTime.now());
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Retrieve all notifications for a user
    public List<NotificationEntity> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }
}
