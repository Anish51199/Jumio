package com.EmailNotification.email.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationContent {

    private String header;
    private String message;
    private String imageUrl;
    private String ctaUrl;

    @JsonCreator
    public NotificationContent(
            @JsonProperty("header") String header,
            @JsonProperty("message") String message,
            @JsonProperty("imageUrl") String imageUrl,
            @JsonProperty("ctaUrl") String ctaUrl) {
        this.header = header;
        this.message = message;
        this.imageUrl = imageUrl;
        this.ctaUrl = ctaUrl;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCtaUrl() {
        return ctaUrl;
    }

    public void setCtaUrl(String ctaUrl) {
        this.ctaUrl = ctaUrl;
    }
}
