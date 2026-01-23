package com.uos.sindhbot.models;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private User user;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public static class User {
        private String user_id;
        private String full_name;
        private String email;
        
        public String getUser_id() {
            return user_id;
        }
        
        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }
        
        public String getFull_name() {
            return full_name;
        }
        
        public void setFull_name(String full_name) {
            this.full_name = full_name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}

