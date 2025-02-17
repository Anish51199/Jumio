package com.EmailNotification.email.controller;

import com.EmailNotification.email.entity.FailedNotification;
import com.EmailNotification.email.service.FailedNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FailedNotificationController {

    private final FailedNotificationService failedNotificationService;

    // Get all failed notifications
    @GetMapping("/failed-notifications")
    public List<FailedNotification> getAllFailedNotifications() {
        return failedNotificationService.getAllFailedNotifications();
    }

    // Get failed notifications by userId
    @GetMapping("/failed-notifications/user/{userId}")
    public List<FailedNotification> getFailedNotificationsByUserId(@PathVariable String userId) {
        return failedNotificationService.getFailedNotificationsByUserId(userId);
    }

    // Get failed notifications by email
    @GetMapping("/failed-notifications/email/{email}")
    public List<FailedNotification> getFailedNotificationsByEmail(@PathVariable String email) {
        return failedNotificationService.getFailedNotificationsByEmail(email);
    }

    // Get failed notifications by error message
    @GetMapping("/failed-notifications/error")
    public List<FailedNotification> getFailedNotificationsByErrorMessage(@RequestParam String errorMessage) {
        return failedNotificationService.getFailedNotificationsByErrorMessage(errorMessage);
    }
}

