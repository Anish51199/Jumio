package com.EmailNotification.email.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationMessage {
    private String userId;
    private String channel;
    private NotificationContent content;

    private Priority priority;

    private boolean isBatch;

    public NotificationMessage() {
    }

    @JsonCreator
    public NotificationMessage(
            @JsonProperty("userId") String userId,
            @JsonProperty("channel") String channel,
            @JsonProperty("content") NotificationContent content,
            @JsonProperty("priority") Priority priority,
            @JsonProperty("isBatch") Boolean isBatch) {
        this.userId = userId;
        this.channel = channel;
        this.content = content;
        this.priority = priority;
        this.isBatch = isBatch;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public NotificationContent getContent() {
        return content;
    }

    public void setContent(NotificationContent content) {
        this.content = content;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isBatch() {
        return isBatch;
    }

    public void setBatch(boolean batch) {
        isBatch = batch;
    }
}

