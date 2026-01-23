package com.uos.sindhbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DURATION = 1500; // Reduced to 1.5 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Start navigation check immediately in background
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Check login status in background
                SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                final String token = prefs.getString("token", null);
                
                // Wait for minimum splash duration, then navigate
                try {
                    Thread.sleep(SPLASH_DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Navigate on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (token != null && !token.isEmpty()) {
                            navigateToMain();
                        } else {
                            navigateToLogin();
                        }
                    }
                });
            }
        }).start();
        
        // Animate splash screen (lightweight animations only)
        animateSplashScreen();
    }
    
    private void navigateToMain() {
        try {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            // Use simple transition, no custom animations to reduce load
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            // Finish immediately
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            // If MainActivity fails, try LoginActivity
            try {
                navigateToLogin();
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
    }
    
    private void animateSplashScreen() {
        // Simplified animations to reduce CPU load
        ImageView logo = findViewById(R.id.imageViewLogo);
        TextView appName = findViewById(R.id.textViewAppName);
        TextView subtitle = findViewById(R.id.textViewSubtitle);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView loadingText = findViewById(R.id.textViewLoading);
        
        // Simple fade in for all elements (no complex animations)
        if (logo != null) {
            logo.setAlpha(0f);
            logo.animate().alpha(1f).setDuration(500).start();
        }
        
        if (appName != null) {
            appName.setAlpha(0f);
            appName.animate().alpha(1f).setDuration(500).setStartDelay(200).start();
        }
        
        if (subtitle != null) {
            subtitle.setAlpha(0f);
            subtitle.animate().alpha(0.9f).setDuration(400).setStartDelay(400).start();
        }
        
        if (progressBar != null) {
            progressBar.setAlpha(0f);
            progressBar.animate().alpha(1f).setDuration(300).setStartDelay(600).start();
        }
        
        if (loadingText != null) {
            loadingText.setAlpha(0f);
            loadingText.animate().alpha(0.8f).setDuration(300).setStartDelay(800).start();
        }
    }
    
    private void navigateToLogin() {
        try {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            // Use simple transition, no custom animations to reduce load
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            // Finish immediately
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            // If navigation fails, just finish
            if (!isFinishing() && !isDestroyed()) {
                finish();
            }
        }
    }
}


