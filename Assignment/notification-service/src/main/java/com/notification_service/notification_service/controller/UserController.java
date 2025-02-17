package com.notification_service.notification_service.controller;

import com.notification_service.notification_service.dto.*;
import com.notification_service.notification_service.entity.NotificationPreference;
import com.notification_service.notification_service.entity.User;
import com.notification_service.notification_service.entity.UserFcmToken;
import com.notification_service.notification_service.enums.NotificationChannel;
import com.notification_service.notification_service.repository.NotificationPreferenceRepository;
import com.notification_service.notification_service.repository.UserFcmTokenRepository;
import com.notification_service.notification_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
        // Save user info
        User user = new User(null, request.getName(), request.getEmail(), request.getPhoneNumber());
        user = userRepository.save(user); // Save to DB, retrieves generated userId

        // Using generated userId for NotificationPreference
        String generatedUserId = user.getUserId();

        Set<NotificationChannel> enabledChannels = request.getPreferences() != null
                ? request.getPreferences().stream()
                .filter(pref -> pref.isEnabled())
                .map(pref -> pref.getChannel())
                .collect(Collectors.toSet())
                : Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.PUSH); // Default enabled

        NotificationPreference preference = new NotificationPreference(generatedUserId, enabledChannels);
        preferenceRepository.save(preference); // Save preferences with a valid userId


        return ResponseEntity.ok(user);
    }


    @PutMapping("/update-user")
    public ResponseEntity<User> updateUser(@RequestBody UserUpdateRequest request) {
        // Fetch the user by userId
        Optional<User> optionalUser = userRepository.findById(request.getUserId());

        if (!optionalUser.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        // Update user fields
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Save updated user information
        user = userRepository.save(user);

        return ResponseEntity.ok(user); // Return updated user
    }


    @PutMapping("/preferences")
    public ResponseEntity<String> updatePreferences(
            @RequestBody NotificationPreferencesUpdateRequest request) {

        // Extract userId
        String userId = request.getUserId();

        // Convert preferences to Set<NotificationChannel> (Only enabled ones)
        Set<NotificationChannel> enabledChannels = request.getPreferences().stream()
                .filter(NotificationPreferenceRequest::isEnabled) // Keep only enabled ones
                .map(NotificationPreferenceRequest::getChannel)
                .collect(Collectors.toSet());

        // Fetch existing preferences
        Optional<NotificationPreference> existingPreference = preferenceRepository.findById(userId);

        if (existingPreference.isPresent()) {
            NotificationPreference preference = existingPreference.get();
            preference.setEnabledChannels(enabledChannels);
            preferenceRepository.save(preference);
        } else {
            NotificationPreference newPreference = new NotificationPreference(userId, enabledChannels);
            preferenceRepository.save(newPreference);
        }

        return ResponseEntity.ok("Notification preferences updated successfully!");
    }


    @GetMapping("/preferences/{userId}")
    public ResponseEntity<Set<NotificationChannel>> getUserSubscribedChannels(@PathVariable String userId) {
        Optional<NotificationPreference> preference = preferenceRepository.findById(userId);

        if (preference.isPresent()) {
            return ResponseEntity.ok(preference.get().getEnabledChannels());
        } else {
            return ResponseEntity.ok(Set.of()); // Return an empty set if no preferences found
        }
    }


    @PostMapping("/fmToken")
    public ResponseEntity<UserFcmToken> updateOrCreateFcmToken(@RequestBody FcmTokenUpdateRequest request) {
        String userId = request.getUserId();

        Optional<UserFcmToken> optionalUserFcmToken = userFcmTokenRepository.findById(userId);

        UserFcmToken userFcmToken;

        // If the token already exists, update it
        if (optionalUserFcmToken.isPresent()) {
            userFcmToken = optionalUserFcmToken.get();
            userFcmToken.setFcmToken(request.getFcmToken());
        } else {
            // If it doesn't exist, create a new UserFcmToken
            userFcmToken = new UserFcmToken(userId, request.getFcmToken());
        }

        // Save the FCM token (either updated or new)
        userFcmToken = userFcmTokenRepository.save(userFcmToken);

        // Return the saved or updated FCM token
        return ResponseEntity.ok(userFcmToken);
    }

    @GetMapping("/{userId}/email")
    public String getUserEmail(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(User::getEmail).orElse(null);
    }


    @GetMapping("/{userId}/phoneNumber")
    public String getUserPhoneNumber(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(User::getPhoneNumber).orElse(null);
    }

    @GetMapping("/{userId}/fcmToken")
    public String getUserFcmToken(@PathVariable String userId) {
        Optional<UserFcmToken> userFcmToken = userFcmTokenRepository.findById(userId);
        return userFcmToken.map(UserFcmToken::getFcmToken).orElse(null);
    }

}

