package com.EmailNotification.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserProfileService {

    private static final String USER_SERVICE_URL = "http://localhost:8081/users/{userId}/email";

    @Autowired
    private RestTemplate restTemplate;

    public String getEmailByUserId(String userId) {
        try {
            // GET request to the UserService to fetch the email
            return restTemplate.getForObject(USER_SERVICE_URL, String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }
}

