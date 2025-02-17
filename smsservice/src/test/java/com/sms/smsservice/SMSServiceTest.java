package com.sms.smsservice;


import com.mashape.unirest.http.exceptions.UnirestException;
import com.sms.smsservice.dto.NotificationContent;
import com.sms.smsservice.dto.Priority;
import com.sms.smsservice.dto.SMSNotificationMessage;
import com.sms.smsservice.repository.FailedSmsNotificationRepository;
import com.sms.smsservice.entity.FailedSMSNotification;
import com.sms.smsservice.service.SMSProviderService;
import com.sms.smsservice.service.SMSService;
import com.sms.smsservice.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.*;

public class SMSServiceTest {

    @Mock private UserProfileService userProfileService;
    @Mock private FailedSmsNotificationRepository failedSMSNotificationRepository;
    @Mock private SMSProviderService smsProviderService;

    private SMSService smsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        smsService = new SMSService(userProfileService, failedSMSNotificationRepository, smsProviderService);
    }

    // Test the SMS notification processing when message is valid
//    @Test
//    void testConsumeSMSNotification() throws UnirestException {
//        String message = "{\"userId\":\"123\",\"channel\":\"sms\",\"priority\":\"HIGH\",\"isBatch\":\"false\",\"header\":\"Test Header\",\"message\":\"Test message\",\"imageUrl\":null,\"ctaUrl\":null}";
//
//        // Mock methods to return valid values
//        when(userProfileService.getPhoneNumberByUserId("123")).thenReturn("1234567890");
//
//        smsService.consumeSMSNotification(message);
//
//        verify(smsProviderService, times(1)).sendSms("1234567890", "Test message");
//    }

//    @Test
//    void testSendSMSWithRetries() throws Exception {
//        // Arrange
//        SMSNotificationMessage notificationMessage = new SMSNotificationMessage(
//                "123", "sms", new NotificationContent("Test Header", "Test message", null, null), Priority.HIGH, false);
//
//        when(userProfileService.getPhoneNumberByUserId("123")).thenReturn("1234567890");
//
//        // Mock the sending method to simulate success on first attempt
//        doNothing().when(smsProviderService).sendSms(anyString(), anyString());
//
//        CountDownLatch latch = new CountDownLatch(1);
//
//        doAnswer(invocation -> {
//            latch.countDown();
//            return null;
//        }).when(smsProviderService).sendSms(anyString(), anyString());
//
//        smsService.sendSMSWithRetries(notificationMessage);
//
//        latch.await();
//
//        verify(smsProviderService, times(1)).sendSms("1234567890", "Test message");
//    }


    // Test handling failure and logging failed SMS
    @Test
    void testLogFailedSMS() {
        SMSNotificationMessage notificationMessage = new SMSNotificationMessage(
                "123", "sms", new NotificationContent("Test Header", "Test message", null, null), Priority.HIGH, false);

        smsService.logFailedSMS(notificationMessage, "1234567890", "Some error");

        verify(failedSMSNotificationRepository, times(1)).save(any(FailedSMSNotification.class));
    }

    // Test process batch queue
//    @Test
//    void testProcessBatchQueue() throws InterruptedException, UnirestException {
//        BlockingQueue<SMSNotificationMessage> smsQueue = new LinkedBlockingQueue<>();
//        smsQueue.offer(new SMSNotificationMessage("123", "sms", new NotificationContent("Test Header", "Test message", null, null), Priority.HIGH, true));
//        smsQueue.offer(new SMSNotificationMessage("124", "sms", new NotificationContent("Test Header 2", "Test message 2", null, null), Priority.LOW, true));
//
//        Thread batchProcessorThread = new Thread(() -> smsService.processBatchQueue());
//        batchProcessorThread.start();
//
//        Thread.sleep(5000);
//
//        verify(smsProviderService, atLeastOnce()).sendSms(anyString(), anyString());
//    }
}

