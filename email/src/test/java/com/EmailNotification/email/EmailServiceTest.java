package com.EmailNotification.email;


import com.EmailNotification.email.dto.NotificationContent;
import com.EmailNotification.email.dto.NotificationMessage;
import com.EmailNotification.email.dto.Priority;
import com.EmailNotification.email.entity.FailedNotification;
import com.EmailNotification.email.repository.FailedNotificationRepository;
import com.EmailNotification.email.service.EmailService;
import com.EmailNotification.email.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@EnableKafka
@EmbeddedKafka
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private FailedNotificationRepository failedNotificationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private EmailService emailService;

    private NotificationMessage notificationMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up a test notification message
        notificationMessage = new NotificationMessage(
                "user123",
                "EMAIL",
                new NotificationContent("Test Header", "Test message", "http://example.com/image.jpg", "http://example.com/cta"),
                Priority.HIGH,
                false // Not a batch message
        );
    }

    @Test
    void testConsumeEmailNotification_ValidMessage() {
        String message = "{\"userId\":\"user123\",\"channel\":\"EMAIL\",\"priority\":\"HIGH\",\"isBatch\":\"false\",\"header\":\"Test Header\",\"message\":\"Test message\",\"imageUrl\":\"http://example.com/image.jpg\",\"ctaUrl\":\"http://example.com/cta\"}";

        when(userProfileService.getEmailByUserId(anyString())).thenReturn("user123@example.com");

        emailService.consumeEmailNotification(message);

        // Verify sendEmailAsync was called with the correct NotificationMessage
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testConsumeEmailNotification_InvalidMessage() {
        String invalidMessage = "";

        emailService.consumeEmailNotification(invalidMessage);

        verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailAsync_ValidEmail() {
        when(userProfileService.getEmailByUserId(anyString())).thenReturn("user123@example.com");

        emailService.sendEmailAsync(notificationMessage);

        // Verify that the sendEmail method was called with the correct email
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailAsync_UserNotFound() {
        when(userProfileService.getEmailByUserId(anyString())).thenReturn(null);

        emailService.sendEmailAsync(notificationMessage);

        // Verify that sendEmail was not called due to missing email
        verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendBatchEmails() {
        List<NotificationMessage> batch = new ArrayList<>();
        batch.add(notificationMessage);  // add one notification

        when(userProfileService.getEmailByUserId(anyString())).thenReturn("user123@example.com");

        emailService.sendBatchEmails(batch);

        // Verify that sendEmailAsync was called for each notification in the batch
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testLogFailedNotification() {
        String error = "SMTP Error";
        FailedNotification failedNotification = FailedNotification.builder()
                .userId("user123")
                .email("user123@example.com")
                .subject("Test Header")
                .body("Test message")
                .errorMessage(error)
                .failedAt(java.time.LocalDateTime.now())
                .build();

        emailService.logFailedNotification(failedNotification.getUserId(), failedNotification.getEmail(),
                failedNotification.getSubject(), failedNotification.getBody(), failedNotification.getErrorMessage());

        // Verify that the failed notification is saved
        verify(failedNotificationRepository, times(1)).save(any(FailedNotification.class));
    }

    @Test
    void testExtractValue_Valid() {
        String message = "{\"userId\":\"user123\",\"channel\":\"EMAIL\",\"priority\":\"HIGH\"}";
        String extractedValue = emailService.extractValue(message, "\"userId\":\"", "\"");

        assert extractedValue.equals("user123");
    }

    @Test
    void testExtractValue_Invalid() {
        String message = "{\"userId\":\"user123\",\"channel\":\"EMAIL\",\"priority\":\"HIGH\"}";
        String extractedValue = emailService.extractValue(message, "\"nonexistentField\":\"", "\"");

        assert extractedValue == null;
    }

}

