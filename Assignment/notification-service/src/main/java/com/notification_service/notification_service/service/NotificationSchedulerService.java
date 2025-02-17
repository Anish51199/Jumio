package com.notification_service.notification_service.service;

import com.notification_service.notification_service.dto.NotificationRequest;
import com.notification_service.notification_service.entity.NotificationEntity;
import com.notification_service.notification_service.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationSchedulerService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private KafkaNotificationProducer producer;

    @Scheduled(fixedRate = 60000) // Runs every minute
    @Transactional
    public void processScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationEntity> dueNotifications = notificationRepository.findDueNotifications(now);

        for (NotificationEntity notification : dueNotifications) {
            boolean locked = notificationRepository.lockNotification(notification.getId()) > 0;

            if (locked) { // If successfully locked, send notification
                NotificationRequest request = new NotificationRequest(
                        notification.getUserId(),
                        notification.getContent(),
                        notification.getChannel(),
                        notification.getScheduledTime(),
                        notification.getPriority(),
                        notification.isBatch()
                );

                producer.sendNotification(request); // Now this method exists

                if (notification.isRecurring()) {
                    notification.setScheduledTime(getNextOccurrence(notification.getScheduledTime(), notification.getRecurrencePattern()));
                    notificationRepository.save(notification);
                } else {
                    notificationRepository.delete(notification); // Delete if it's one-time
                }
            }
        }
    }

    public LocalDateTime getNextOccurrence(LocalDateTime lastTime, String pattern) {
        return switch (pattern) {
            case "DAILY" -> lastTime.plusDays(1);
            case "WEEKLY" -> lastTime.plusWeeks(1);
            case "MONTHLY" -> lastTime.plusMonths(1);
            default -> lastTime;
        };
    }
}

