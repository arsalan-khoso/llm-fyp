package com.uos.sindhbot.api;

import com.uos.sindhbot.models.ApiRequest;
import com.uos.sindhbot.models.ApiResponse;
import com.uos.sindhbot.models.AuthRequest;
import com.uos.sindhbot.models.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Header;

public interface ApiService {
    // Authentication
    @POST("api/auth/signup")
    Call<AuthResponse> signup(@Body AuthRequest.SignupRequest request);
    
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest.LoginRequest request);
    
    @POST("api/auth/forgot-password")
    Call<AuthResponse> forgotPassword(@Body AuthRequest.ForgotPasswordRequest request);
    
    // Chat
    @POST("api/ask")
    Call<ApiResponse> askQuestion(@Body ApiRequest request);
    
    @POST("api/ask")
    Call<ApiResponse> askQuestion(@Header("Authorization") String token, @Body ApiRequest request);
}

