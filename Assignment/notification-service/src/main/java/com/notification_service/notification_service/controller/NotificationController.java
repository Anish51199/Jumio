package com.notification_service.notification_service.controller;


import com.notification_service.notification_service.dto.NotificationPreferenceRequest;
import com.notification_service.notification_service.dto.NotificationRequest;
import com.notification_service.notification_service.entity.NotificationEntity;
import com.notification_service.notification_service.entity.NotificationPreference;
import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.repository.NotificationPreferenceRepository;
import com.notification_service.notification_service.repository.NotificationRepository;
import com.notification_service.notification_service.service.KafkaNotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final KafkaNotificationProducer producer;

    @Autowired
    public NotificationController(KafkaNotificationProducer producer) {
        this.producer = producer;
    }

    // Send notification to a specific channel
    @PostMapping("/send/specific")
    public ResponseEntity<String> sendNotificationToSpecificChannel(@RequestBody NotificationRequest request) {
        producer.sendNotification(request);
        return ResponseEntity.ok("Notification sent successfully to " + request.getChannel());
    }

    // Send notification to all channels user is subscribed to
    @PostMapping("/send/all")
    public ResponseEntity<String> sendNotificationToAllChannels(@RequestBody NotificationRequest request) {
        producer.sendNotificationToAllChannels(request);
        return ResponseEntity.ok("Notification sent successfully to all subscribed channels");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationEntity>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(producer.getUserNotifications(userId));
    }

}

