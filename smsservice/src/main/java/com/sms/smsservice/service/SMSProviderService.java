package com.sms.smsservice.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SMSProviderService {

    @Value("${sms.auth_token}")
    private String authToken;


    public void sendSms(String to, String message) throws UnirestException {
        if (to == null || to.isEmpty() || message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Phone number and message must not be empty");
        }

        to = to.trim();

        String formattedPhoneNumber = to.startsWith("+91") ? to : "+91" + to;

        // API ISSUE
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8).substring(0,6);
        System.out.println("AuthToken: " + authToken);

        String url = String.format("https://2factor.in/API/V1/%s/SMS/%s/%s", authToken, formattedPhoneNumber, encodedMessage);

        //HTTP request
        HttpResponse<JsonNode> httpResponse = Unirest.get(url).asJson();

        // Check the response status and log the result
        if (httpResponse.getStatus() == 200) {
            System.out.println("Message sent successfully.");
        } else {
            System.out.println("Failed to send message. Status: " + httpResponse.getStatus());
            System.out.println("Response: " + httpResponse.getBody());
        }
    }
}

