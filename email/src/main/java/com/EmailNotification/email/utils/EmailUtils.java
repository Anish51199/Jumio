package com.EmailNotification.email.utils;


import org.springframework.stereotype.Component;

@Component
public class EmailUtils {
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

