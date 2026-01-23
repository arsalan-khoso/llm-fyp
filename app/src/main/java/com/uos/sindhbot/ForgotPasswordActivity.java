package com.uos.sindhbot;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.uos.sindhbot.api.ApiClient;
import com.uos.sindhbot.api.ApiService;
import com.uos.sindhbot.models.AuthRequest;
import com.uos.sindhbot.models.AuthResponse;
import com.uos.sindhbot.utils.AnimationUtils;
import com.uos.sindhbot.utils.CustomSnackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    
    private ImageButton buttonBack;
    private TextInputEditText editTextEmail;
    private MaterialButton buttonReset;
    private TextView textViewLogin;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        initializeViews();
        setupApiService();
        setupClickListeners();
        animateViews();
    }
    
    private void setupApiService() {
        apiService = ApiClient.getApiService();
    }
    
    private void animateViews() {
        View logo = findViewById(R.id.imageViewLogo);
        if (logo != null) {
            logo.setAlpha(0f);
            logo.setScaleX(0.5f);
            logo.setScaleY(0.5f);
            logo.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setStartDelay(100)
                    .start();
        }
        
        View cardView = findViewById(android.R.id.content);
        if (cardView != null) {
            cardView.setAlpha(0f);
            cardView.setTranslationY(50f);
            cardView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(300)
                    .start();
        }
    }
    
    private void initializeViews() {
        buttonBack = findViewById(R.id.buttonBack);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonReset = findViewById(R.id.buttonReset);
        textViewLogin = findViewById(R.id.textViewLogin);
    }
    
    private void setupClickListeners() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationUtils.bounce(v);
                onBackPressed();
            }
        });
        
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationUtils.bounce(v);
                attemptReset();
            }
        });
        
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationUtils.bounce(v);
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }
    
    private void attemptReset() {
        String email = editTextEmail.getText().toString().trim();
        
        // Validation
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }
        
        // Animate button press
        AnimationUtils.bounce(buttonReset);
        
        // Show loading
        buttonReset.setEnabled(false);
        buttonReset.setText("Sending...");
        buttonReset.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    buttonReset.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
        
        // Call backend API
        performReset(email);
    }
    
    private void performReset(String email) {
        AuthRequest.ForgotPasswordRequest request = new AuthRequest.ForgotPasswordRequest(email);
        Call<AuthResponse> call = apiService.forgotPassword(request);
        
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                buttonReset.setEnabled(true);
                buttonReset.setText("Send Reset Link");
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    CustomSnackbar.showSuccess(findViewById(android.R.id.content), 
                        authResponse.getMessage());
                    
                    // Navigate back to login
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    }, 1500);
                } else {
                    CustomSnackbar.showError(findViewById(android.R.id.content), 
                        "Failed to send reset link. Please try again.");
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                buttonReset.setEnabled(true);
                buttonReset.setText("Send Reset Link");
                CustomSnackbar.showError(findViewById(android.R.id.content), 
                    "Network error. Please check your connection.");
                t.printStackTrace();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

