package com.uos.sindhbot.models;

public class AuthRequest {
    
    public static class LoginRequest {
        private String email;
        private String password;
        
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    public static class SignupRequest {
        private String full_name;
        private String email;
        private String password;
        
        public SignupRequest(String fullName, String email, String password) {
            this.full_name = fullName;
            this.email = email;
            this.password = password;
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
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    public static class ForgotPasswordRequest {
        private String email;
        
        public ForgotPasswordRequest(String email) {
            this.email = email;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}

