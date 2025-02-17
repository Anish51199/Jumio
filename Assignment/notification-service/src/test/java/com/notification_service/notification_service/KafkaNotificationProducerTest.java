package com.notification_service.notification_service;

import com.notification_service.notification_service.config.NotificationChannelConfig;
import com.notification_service.notification_service.dto.NotificationContent;
import com.notification_service.notification_service.dto.NotificationRequest;
import com.notification_service.notification_service.entity.NotificationEntity;
import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.enums.NotificationStatus;
import com.notification_service.notification_service.enums.Priority;
import com.notification_service.notification_service.repository.NotificationPreferenceRepository;
import com.notification_service.notification_service.repository.NotificationRepository;
import com.notification_service.notification_service.service.KafkaNotificationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.kafka.core.KafkaTemplate;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class KafkaNotificationProducerTest {

    @Mock
    private KafkaTemplate<Object, String> kafkaTemplate;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private NotificationChannelConfig channelConfig;

    private KafkaNotificationProducer kafkaNotificationProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the channel-to-topic mapping
        when(channelConfig.getTopics()).thenReturn(Map.of(
                "EMAIL", "email-topic",
                "SMS", "sms-topic",
                "PUSH", "push-topic"
        ));

        // Create the KafkaNotificationProducer instance
        kafkaNotificationProducer = new KafkaNotificationProducer(
                kafkaTemplate,
                notificationRepository,
                notificationPreferenceRepository,
                channelConfig
        );
    }

    NotificationContent sendNotificationContent() {
        return new NotificationContent(
                "Test Header",
                "Test message",
                "http://example.com/image.jpg",
                "http://example.com/cta"
        );
    }


    @Test
    void sendNotification_invalidChannel_throwsException() {
        String invalidChannel = "INVALID"; // Invalid channel that is not in the enum

        NotificationRequest request = new NotificationRequest(
                "user123",
                sendNotificationContent(),
                NotificationChannel.valueOf("PUSH"), // A valid channel here, as the invalid one will be handled below
                LocalDateTime.now(),
                Priority.HIGH,
                false
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            NotificationChannel invalidNotificationChannel = NotificationChannel.valueOf(invalidChannel);
            request.setChannel(invalidNotificationChannel);
            kafkaNotificationProducer.sendNotification(request);
        });

        // Verify the exception message
        assertTrue(thrown.getMessage().contains("No enum constant"));
        assertTrue(thrown.getMessage().contains(invalidChannel));
    }

    @Test
    void saveNotification_success() {
        NotificationRequest request = new NotificationRequest(
                "user123",
                sendNotificationContent(),
                NotificationChannel.EMAIL,
                LocalDateTime.now(),
                Priority.MEDIUM,
                false
        );

        NotificationEntity savedNotification = new NotificationEntity();
        savedNotification.setUserId(request.getUserId());
        savedNotification.setContent(request.getContent());
        savedNotification.setChannel(request.getChannel());
        savedNotification.setStatus(NotificationStatus.PENDING);
        savedNotification.setScheduledTime(LocalDateTime.now());
        savedNotification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(savedNotification);

        NotificationEntity result = kafkaNotificationProducer.saveNotification(request, NotificationChannel.EMAIL);

        assertEquals(request.getUserId(), result.getUserId());
        assertEquals(NotificationStatus.PENDING, result.getStatus());
        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
    }

    @Test
    void getUserNotifications_success() {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId("user123");
        notification.setContent(sendNotificationContent());
        notification.setChannel(NotificationChannel.EMAIL);

        when(notificationRepository.findByUserId("user123")).thenReturn(List.of(notification));

        List<NotificationEntity> notifications = kafkaNotificationProducer.getUserNotifications("user123");

        assertEquals(1, notifications.size());
    }
}

