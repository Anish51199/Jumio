package com.sms.smsservice.controller;

import com.sms.smsservice.entity.FailedSMSNotification;
import com.sms.smsservice.repository.FailedSmsNotificationRepository;
import com.sms.smsservice.service.SMSService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SMSService smsService;
    private final FailedSmsNotificationRepository failedSmsNotificationRepository;

    @GetMapping("/failed")
    public List<FailedSMSNotification> getFailedSmsNotifications() {
        return failedSmsNotificationRepository.findAll();
    }
}

