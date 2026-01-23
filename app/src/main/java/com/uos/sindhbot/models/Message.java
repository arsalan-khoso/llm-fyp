package com.uos.sindhbot.models;

public class Message {
    private String text;
    private boolean isUser;
    private String timestamp;
    private String language;

    public Message(String text, boolean isUser, String timestamp) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    public Message(String text, boolean isUser, String timestamp, String language) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

