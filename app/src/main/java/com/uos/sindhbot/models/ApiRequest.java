package com.uos.sindhbot.models;

public class ApiRequest {
    private String question;
    private String language;

    public ApiRequest(String question, String language) {
        this.question = question;
        this.language = language;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

