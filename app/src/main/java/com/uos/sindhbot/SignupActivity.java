package com.uos.sindhbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class SignupActivity extends AppCompatActivity {
    
    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private MaterialButton buttonSignUp;
    private TextView textViewLogin;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_signup);
            
            initializeViews();
            setupApiService();
            setupClickListeners();
            animateViews();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupApiService() {
        try {
            apiService = ApiClient.getApiService();
        } catch (Exception e) {
            e.printStackTrace();
            // API service will be null, but we'll handle it in performSignUp
        }
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
        try {
            editTextFullName = findViewById(R.id.editTextFullName);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
            buttonSignUp = findViewById(R.id.buttonSignUp);
            textViewLogin = findViewById(R.id.textViewLogin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupClickListeners() {
        try {
            if (buttonSignUp != null) {
                buttonSignUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptSignUp();
                    }
                });
            }
            
            if (textViewLogin != null) {
                textViewLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimationUtils.bounce(v);
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void attemptSignUp() {
        if (editTextFullName == null || editTextEmail == null || 
            editTextPassword == null || editTextConfirmPassword == null) {
            Toast.makeText(this, "Please wait, app is initializing...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        // Validation
        if (TextUtils.isEmpty(fullName)) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            return;
        }
        
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
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return;
        }
        
        // Animate button press
        if (buttonSignUp != null) {
            AnimationUtils.bounce(buttonSignUp);
            
            // Show loading
            buttonSignUp.setEnabled(false);
            buttonSignUp.setText("Creating account...");
            buttonSignUp.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        if (buttonSignUp != null) {
                            buttonSignUp.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                        }
                    })
                    .start();
        }
        
        // Call backend API
        performSignUp(fullName, email, password);
    }
    
    private void performSignUp(String fullName, String email, String password) {
        if (apiService == null) {
            CustomSnackbar.showError(findViewById(android.R.id.content), 
                "API service not initialized. Please restart the app.");
            if (buttonSignUp != null) {
                buttonSignUp.setEnabled(true);
                buttonSignUp.setText("Create Account");
            }
            return;
        }
        
        try {
            AuthRequest.SignupRequest request = new AuthRequest.SignupRequest(fullName, email, password);
            Call<AuthResponse> call = apiService.signup(request);
            
            call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (buttonSignUp != null) {
                    buttonSignUp.setEnabled(true);
                    buttonSignUp.setText("Create Account");
                }
                
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();
                        if (authResponse != null && authResponse.isSuccess()) {
                            // Save token and user info
                            SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            
                            if (authResponse.getToken() != null && !authResponse.getToken().isEmpty()) {
                                editor.putString("token", authResponse.getToken());
                            }
                            
                            if (authResponse.getUser() != null) {
                                AuthResponse.User user = authResponse.getUser();
                                if (user.getFull_name() != null) {
                                    editor.putString("user_name", user.getFull_name());
                                }
                                if (user.getEmail() != null) {
                                    editor.putString("user_email", user.getEmail());
                                }
                                if (user.getUser_id() != null) {
                                    editor.putString("user_id", user.getUser_id());
                                }
                            }
                            editor.apply();
                            
                            String message = authResponse.getMessage();
                            if (message == null || message.isEmpty()) {
                                message = "Account created successfully";
                            }
                            CustomSnackbar.showSuccess(findViewById(android.R.id.content), message);
                            
                            // Navigate on main thread after a very short delay to ensure UI is ready
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            navigateToMain();
                                        }
                                    }, 500);
                                }
                            });
                        } else {
                            String errorMsg = authResponse != null && authResponse.getMessage() != null 
                                    ? authResponse.getMessage() 
                                    : "Signup failed. Please try again.";
                            CustomSnackbar.showError(findViewById(android.R.id.content), errorMsg);
                        }
                    } else {
                        String errorMsg = "Signup failed. Please try again.";
                        if (response.code() == 400) {
                            errorMsg = "Email already registered or invalid data.";
                        }
                        CustomSnackbar.showError(findViewById(android.R.id.content), errorMsg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CustomSnackbar.showError(findViewById(android.R.id.content), 
                        "Error processing response: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (buttonSignUp != null) {
                    buttonSignUp.setEnabled(true);
                    buttonSignUp.setText("Create Account");
                }
                CustomSnackbar.showError(findViewById(android.R.id.content), 
                    "Network error. Please check your connection.");
                t.printStackTrace();
            }
            });
        } catch (Exception e) {
            e.printStackTrace();
            CustomSnackbar.showError(findViewById(android.R.id.content), 
                "Error making signup request: " + e.getMessage());
            if (buttonSignUp != null) {
                buttonSignUp.setEnabled(true);
                buttonSignUp.setText("Create Account");
            }
        }
    }
    
    private void navigateToMain() {
        try {
            if (isFinishing() || isDestroyed()) {
                return; // Activity is already finishing, don't navigate
            }
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            // Finish after transition starts
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing() && !isDestroyed()) {
                        finish();
                    }
                }
            }, 300);
        } catch (Exception e) {
            e.printStackTrace();
            // If navigation fails, try again without flags
            try {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception ex) {
                ex.printStackTrace();
                // Last resort: just finish this activity
                finish();
            }
        }
    }
}

