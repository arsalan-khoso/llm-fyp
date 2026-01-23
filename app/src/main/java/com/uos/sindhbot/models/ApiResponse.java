package com.uos.sindhbot.models;

public class ApiResponse {
    private String answer;
    private String source;
    private String language;
    private boolean success;
    private String error;

    public ApiResponse() {
    }

    public ApiResponse(String answer, String source, String language) {
        this.answer = answer;
        this.source = source;
        this.language = language;
        this.success = true;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

