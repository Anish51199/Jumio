package com.push.pushService.config;


import ch.qos.logback.classic.Logger;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Configuration
public class FirebaseConfig {

    private static final Logger log = (Logger) LoggerFactory.getLogger(FirebaseConfig.class);

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/firebase.messaging"
    );
    @PostConstruct
    public void initialize() {
        try {
            // Load google-services.json from resources
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("notification.json");

            if (serviceAccount == null) {
                throw new IllegalStateException("google-services.json not found in resources folder");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw new RuntimeException(e);
        }
    }

    public void sendBatchNotification(List<String> tokens, String message) {
        try {
            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .putData("message", message)
                    .addAllTokens(tokens)
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(multicastMessage);
            log.info("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send batch notifications", e);
        }
    }

    private static String getAccessToken() throws IOException {
        // Load the service account file from the resources folder
        InputStream serviceAccount = FirebaseConfig.class.getClassLoader().getResourceAsStream("notification.json");

        if (serviceAccount == null) {
            throw new IOException("Service account file not found in resources.");
        }

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(serviceAccount)
                .createScoped(SCOPES);
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }

}