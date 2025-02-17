package com.notification_service.notification_service;

import com.notification_service.notification_service.dto.NotificationContent;
import com.notification_service.notification_service.dto.NotificationRequest;
import com.notification_service.notification_service.entity.NotificationEntity;
import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.enums.NotificationStatus;
import com.notification_service.notification_service.enums.Priority;
import com.notification_service.notification_service.repository.NotificationRepository;
import com.notification_service.notification_service.service.KafkaNotificationProducer;
import com.notification_service.notification_service.service.NotificationSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private KafkaNotificationProducer producer;

    @InjectMocks
    private NotificationSchedulerService notificationSchedulerService;

    private NotificationEntity dueNotification;
    private NotificationEntity recurringNotification;

    @BeforeEach
    void setUp() {
        // Setting up mock notifications
        dueNotification = new NotificationEntity(
                1L, // ID
                "user123", // User ID
                sendNotificationContent(), // Notification content
                NotificationChannel.EMAIL, // Channel
                NotificationStatus.PENDING, // Status
                Priority.HIGH, // Priority
                false, // Is batch
                LocalDateTime.now().minusMinutes(1), // Scheduled time (due)
                false, // Recurring
                null, // No recurrence pattern for non-recurring notification
                LocalDateTime.now() // Created time
        );

        recurringNotification = new NotificationEntity(
                2L, // ID
                "user456", // User ID
                sendNotificationContent(),
                NotificationChannel.SMS,
                NotificationStatus.PENDING,
                Priority.MEDIUM,
                true,
                LocalDateTime.now().minusMinutes(1),
                true,
                "DAILY",
                LocalDateTime.now()
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
    void processScheduledNotifications_sendsNotification_forDueNotification() {
        when(notificationRepository.findDueNotifications(any(LocalDateTime.class)))
                .thenReturn(List.of(dueNotification));
        when(notificationRepository.lockNotification(dueNotification.getId())).thenReturn(1);

        notificationSchedulerService.processScheduledNotifications();

        // Verify that the KafkaNotificationProducer's sendNotification method was called
        verify(producer, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void processScheduledNotifications_doesNotSendNotification_whenLockFails() {
        when(notificationRepository.findDueNotifications(any(LocalDateTime.class)))
                .thenReturn(List.of(dueNotification));
        when(notificationRepository.lockNotification(dueNotification.getId())).thenReturn(0);

        notificationSchedulerService.processScheduledNotifications();

        // Verify that the KafkaNotificationProducer's sendNotification method was not called
        verify(producer, times(0)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void processScheduledNotifications_savesRecurringNotification_withNextOccurrence() {
        when(notificationRepository.findDueNotifications(any(LocalDateTime.class)))
                .thenReturn(List.of(recurringNotification));
        when(notificationRepository.lockNotification(recurringNotification.getId())).thenReturn(1);
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(recurringNotification);

        notificationSchedulerService.processScheduledNotifications();

        // Verify that the next occurrence was set and the notification was saved
        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
    }

    @Test
    void processScheduledNotifications_deletesOneTimeNotification() {
        // Mock
        dueNotification.setRecurring(false); // One-time notification
        when(notificationRepository.findDueNotifications(any(LocalDateTime.class)))
                .thenReturn(List.of(dueNotification));
        when(notificationRepository.lockNotification(dueNotification.getId())).thenReturn(1);

        notificationSchedulerService.processScheduledNotifications();

        // Verify that the notification was deleted (because it is one-time)
        verify(notificationRepository, times(1)).delete(dueNotification);
    }

    @Test
    void getNextOccurrence_returnsCorrectNextTime_forDailyPattern() {
        LocalDateTime lastTime = LocalDateTime.of(2025, 2, 17, 10, 0, 0, 0);

        LocalDateTime nextOccurrence = notificationSchedulerService.getNextOccurrence(lastTime, "DAILY");

        // Verify that the next occurrence is 1 day later
        assertEquals(lastTime.plusDays(1), nextOccurrence);
    }

    @Test
    void getNextOccurrence_returnsCorrectNextTime_forWeeklyPattern() {
        LocalDateTime lastTime = LocalDateTime.of(2025, 2, 17, 10, 0, 0, 0);

        LocalDateTime nextOccurrence = notificationSchedulerService.getNextOccurrence(lastTime, "WEEKLY");

        // Verify that the next occurrence is 1 week later
        assertEquals(lastTime.plusWeeks(1), nextOccurrence);
    }

    @Test
    void getNextOccurrence_returnsCorrectNextTime_forMonthlyPattern() {
        LocalDateTime lastTime = LocalDateTime.of(2025, 2, 17, 10, 0, 0, 0);

        LocalDateTime nextOccurrence = notificationSchedulerService.getNextOccurrence(lastTime, "MONTHLY");

        // Verify that the next occurrence is 1 month later
        assertEquals(lastTime.plusMonths(1), nextOccurrence);
    }

    @Test
    void getNextOccurrence_returnsSameTime_whenPatternIsUnknown() {
        LocalDateTime lastTime = LocalDateTime.of(2025, 2, 17, 10, 0, 0, 0);

        LocalDateTime nextOccurrence = notificationSchedulerService.getNextOccurrence(lastTime, "UNKNOWN");

        // Verify that the next occurrence is the same as the last time
        assertEquals(lastTime, nextOccurrence);
    }
}

