package com.uos.sindhbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;

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

public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textViewSignUp;
    private TextView textViewForgotPassword;
    private TextView textViewSkip;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            
            initializeViews();
            setupApiService();
            setupClickListeners();
            animateViews();
        } catch (Exception e) {
            e.printStackTrace();
            // If initialization fails, show error and try to continue
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupApiService() {
        try {
            apiService = ApiClient.getApiService();
        } catch (Exception e) {
            e.printStackTrace();
            // API service will be null, but we'll handle it in performLogin
        }
    }
    
    private void animateViews() {
        View logo = findViewById(R.id.imageViewLogo);
        
        // Animate logo
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
        
        // Animate login card
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setAlpha(0f);
            rootView.setTranslationY(50f);
            rootView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(300)
                    .start();
        }
    }
    
    private void initializeViews() {
        try {
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            buttonLogin = findViewById(R.id.buttonLogin);
            textViewSignUp = findViewById(R.id.textViewSignUp);
            textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
            textViewSkip = findViewById(R.id.textViewSkip);
        } catch (Exception e) {
            e.printStackTrace();
            // Views might be null, but setupClickListeners will handle it
        }
    }
    
    private void setupClickListeners() {
        try {
            if (buttonLogin != null) {
                buttonLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptLogin();
                    }
                });
            }
            
            if (textViewSignUp != null) {
                textViewSignUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimationUtils.bounce(v);
                        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
            }
            
            if (textViewForgotPassword != null) {
                textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimationUtils.bounce(v);
                        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
            }
            
            if (textViewSkip != null) {
                textViewSkip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimationUtils.bounce(v);
                        // Skip authentication for testing
                        navigateToMain();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void attemptLogin() {
        if (editTextEmail == null || editTextPassword == null) {
            Toast.makeText(this, "Please wait, app is initializing...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        // Validation
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            AnimationUtils.bounce(editTextEmail);
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            AnimationUtils.bounce(editTextEmail);
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            AnimationUtils.bounce(editTextPassword);
            return;
        }
        
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            AnimationUtils.bounce(editTextPassword);
            return;
        }
        
        // Animate button press
        if (buttonLogin != null) {
            AnimationUtils.bounce(buttonLogin);
            
            // Show loading
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Signing in...");
            buttonLogin.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        if (buttonLogin != null) {
                            buttonLogin.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                        }
                    })
                    .start();
        }
        
        // Call backend API
        performLogin(email, password);
    }
    
    private void performLogin(String email, String password) {
        if (apiService == null) {
            CustomSnackbar.showError(findViewById(android.R.id.content), 
                "API service not initialized. Please restart the app.");
            if (buttonLogin != null) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Sign In");
            }
            return;
        }
        
        try {
            AuthRequest.LoginRequest request = new AuthRequest.LoginRequest(email, password);
            Call<AuthResponse> call = apiService.login(request);
            
            call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("Sign In");
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
                                message = "Login successful";
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
                                    : "Login failed. Please try again.";
                            CustomSnackbar.showError(findViewById(android.R.id.content), errorMsg);
                        }
                    } else {
                        CustomSnackbar.showError(findViewById(android.R.id.content), 
                            "Login failed. Please try again.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CustomSnackbar.showError(findViewById(android.R.id.content), 
                        "Error processing response: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("Sign In");
                }
                CustomSnackbar.showError(findViewById(android.R.id.content), 
                    "Network error. Please check your connection.");
                t.printStackTrace();
            }
            });
        } catch (Exception e) {
            e.printStackTrace();
            CustomSnackbar.showError(findViewById(android.R.id.content), 
                "Error making login request: " + e.getMessage());
            if (buttonLogin != null) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Sign In");
            }
        }
    }
    
    private void navigateToMain() {
        try {
            if (isFinishing() || isDestroyed()) {
                return; // Activity is already finishing, don't navigate
            }
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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

