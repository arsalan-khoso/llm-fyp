package com.uos.sindhbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.uos.sindhbot.adapters.ChatAdapter;
import com.uos.sindhbot.adapters.ChatHistoryAdapter;
import com.uos.sindhbot.api.ApiClient;
import com.uos.sindhbot.api.ApiService;
import com.uos.sindhbot.models.ApiRequest;
import com.uos.sindhbot.models.ApiResponse;
import com.uos.sindhbot.models.Message;
import com.uos.sindhbot.utils.AnimationUtils;
import com.uos.sindhbot.utils.ChatHistoryManager;
import com.uos.sindhbot.utils.CustomSnackbar;
import com.uos.sindhbot.utils.LanguageDetector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.speech.tts.TextToSpeech;
import java.util.Locale;
import java.util.UUID;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private MaterialButton buttonSend;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private ApiService apiService;
    
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ImageButton buttonMenu;
    private TextView textViewProfile;
    private ChatHistoryManager chatHistoryManager;
    private RecyclerView recyclerViewHistory;
    private ChatHistoryAdapter historyAdapter;
    private TextView textViewNoHistory;
    private TextToSpeech textToSpeech;
    private String currentSessionId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            
            // Critical UI setup first (must be on main thread)
            initializeViews();
            setupRecyclerView();
            setupSendButton();
            setupNewChatButton();
            
            // Initialize TextToSpeech
            textToSpeech = new TextToSpeech(this, this);
            
            // Defer non-critical setup to avoid blocking
            recyclerViewChat.post(new Runnable() {
                @Override
                public void run() {
                    setupToolbar();
                    setupDrawer();
                    setupHistoryRecyclerView();
                    setupApiService();
                    setupChatHistory();
                    setupProfile();
                }
            });
            
            // Load saved chat and refresh history asynchronously to avoid ANR
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Load data in background
                    if (chatHistoryManager == null) {
                        chatHistoryManager = new ChatHistoryManager(MainActivity.this);
                    }
                    // Load current session ID
                    currentSessionId = chatHistoryManager.getCurrentSessionId();
                    
                    List<Message> savedMessages = chatHistoryManager.loadCurrentChat();
                    List<ChatHistoryManager.ChatSession> history = chatHistoryManager.getChatHistory();
                    
                    // Update UI on main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Initialize messages if needed
                            if (messages == null) {
                                messages = new ArrayList<>();
                            }
                            if (chatAdapter == null) {
                                chatAdapter = new ChatAdapter(messages);
                                if (recyclerViewChat != null) {
                                    recyclerViewChat.setAdapter(chatAdapter);
                                }
                            }
                            
                            // Load saved messages
                            if (savedMessages != null && !savedMessages.isEmpty()) {
                                messages.clear();
                                messages.addAll(savedMessages);
                                if (chatAdapter != null) {
                                    chatAdapter.notifyDataSetChanged();
                                }
                                initializeAdapterListener();
                                recyclerViewChat.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollToBottom();
                                    }
                                }, 100);
                            } else {
                                addWelcomeMessage();
                            }
                            
                            // Refresh history list
                            refreshHistoryList();
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Try to continue with minimal setup
        }
    }
    
    private void initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            drawerLayout = findViewById(R.id.drawerLayout);
            buttonMenu = findViewById(R.id.buttonMenu);
            textViewProfile = findViewById(R.id.textViewProfile);
            recyclerViewChat = findViewById(R.id.recyclerViewChat);
            editTextMessage = findViewById(R.id.editTextMessage);
            buttonSend = findViewById(R.id.buttonSend);
            recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
            textViewNoHistory = findViewById(R.id.textViewNoHistory);
            chatHistoryManager = new ChatHistoryManager(this);
        } catch (Exception e) {
            e.printStackTrace();
            // Don't throw - just log the error and continue with null views
            // The setup methods will handle null checks
        }
    }
    
    private void setupToolbar() {
        try {
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupDrawer() {
        try {
            if (drawerLayout == null || toolbar == null) {
                return;
            }
            
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.app_name, R.string.app_name
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            
            if (buttonMenu != null) {
                buttonMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimationUtils.bounce(v);
                        if (drawerLayout != null) {
                            drawerLayout.openDrawer(GravityCompat.START);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Setup navigation header
        try {
            TextView navProfile = findViewById(R.id.textViewNavProfile);
            TextView navName = findViewById(R.id.textViewNavName);
            TextView navEmail = findViewById(R.id.textViewNavEmail);
            
            // Load user info from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
            String userName = prefs.getString("user_name", "User");
            String userEmail = prefs.getString("user_email", "user@example.com");
            
            if (navName != null) {
                navName.setText(userName);
            }
            if (navEmail != null) {
                navEmail.setText(userEmail);
            }
            
            // Set profile letter - use cyan color to match the image
            if (navProfile != null) {
                String firstLetter = userName.length() > 0 ? String.valueOf(userName.charAt(0)).toUpperCase() : "U";
                navProfile.setText(firstLetter);
                navProfile.setBackgroundColor(ContextCompat.getColor(this, R.color.uos_primary));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Continue without header setup if it fails
        }
    }
    
    private void setupRecyclerView() {
        try {
            if (recyclerViewChat == null) {
                return;
            }
            messages = new ArrayList<>();
            chatAdapter = new ChatAdapter(messages);
            recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewChat.setAdapter(chatAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupApiService() {
        apiService = ApiClient.getApiService();
    }
    
    private void setupHistoryRecyclerView() {
        try {
            if (recyclerViewHistory == null) {
                return;
            }
            recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
            historyAdapter = new ChatHistoryAdapter(new ArrayList<>(), new ChatHistoryAdapter.OnHistoryItemClickListener() {
                @Override
                public void onHistoryItemClick(ChatHistoryManager.ChatSession session) {
                    // Load selected chat
                    if (session.messages != null && !session.messages.isEmpty()) {
                        messages.clear();
                        messages.addAll(session.messages);
                        messages.addAll(session.messages);
                        chatAdapter.notifyDataSetChanged();
                        
                        // Set current session ID
                        currentSessionId = session.id;
                        chatHistoryManager.saveCurrentSessionId(currentSessionId);
                        saveChat(); // Save as current chat so it persists on restart
                        
                        scrollToBottom();
                        if (drawerLayout != null) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        CustomSnackbar.showSuccess(findViewById(android.R.id.content), "Chat loaded successfully");
                    }
                }
            });
            recyclerViewHistory.setAdapter(historyAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void refreshHistoryList() {
        try {
            if (chatHistoryManager == null) {
                chatHistoryManager = new ChatHistoryManager(this);
            }
            
            // Load and display history (this is fast, can run on UI thread)
            List<ChatHistoryManager.ChatSession> history = chatHistoryManager.getChatHistory();
            if (historyAdapter != null) {
                historyAdapter.updateHistory(history);
            }
            
            // Show/hide no history message
            if (textViewNoHistory != null) {
                if (history.isEmpty()) {
                    textViewNoHistory.setVisibility(View.VISIBLE);
                } else {
                    textViewNoHistory.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveCurrentChatToHistory() {
        // Save current chat to history asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (chatHistoryManager == null) {
                        chatHistoryManager = new ChatHistoryManager(MainActivity.this);
                    }
                    
                    // Get all user prompts from current chat
                    List<Message> userMessages = new ArrayList<>();
                    if (messages != null) {
                        for (Message msg : messages) {
                            if (msg.isUser()) {
                                userMessages.add(msg);
                            }
                        }
                    }
                    
                    // Save current chat to history if it has messages
                    if (!userMessages.isEmpty() && messages.size() > 1) {
                        String firstPrompt = userMessages.get(0).getText();
                        String title = firstPrompt.length() > 40 ? firstPrompt.substring(0, 40) + "..." : firstPrompt;
                        String firstPrompt = userMessages.get(0).getText();
                        String title = firstPrompt.length() > 40 ? firstPrompt.substring(0, 40) + "..." : firstPrompt;
                        
                        // Generate ID if needed
                        if (currentSessionId == null) {
                            currentSessionId = UUID.randomUUID().toString();
                            chatHistoryManager.saveCurrentSessionId(currentSessionId);
                        }
                        
                        chatHistoryManager.saveToHistory(messages, title, currentSessionId);
                        
                        // Refresh history list on UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshHistoryList();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private void setupChatHistory() {
        // Auto-save chat every 5 messages
    }
    
    private void loadSavedChat() {
        try {
            if (chatHistoryManager == null) {
                chatHistoryManager = new ChatHistoryManager(this);
            }
            if (messages == null) {
                messages = new ArrayList<>();
            }
            if (chatAdapter == null) {
                chatAdapter = new ChatAdapter(messages);
                if (recyclerViewChat != null) {
                    recyclerViewChat.setAdapter(chatAdapter);
                }
            }
            
            // Load saved messages
            List<Message> savedMessages = chatHistoryManager.loadCurrentChat();
            if (savedMessages != null && !savedMessages.isEmpty()) {
                // Clear existing messages first
                messages.clear();
                // Add saved messages
                messages.addAll(savedMessages);
                // Notify adapter
                if (chatAdapter != null) {
                    chatAdapter.notifyDataSetChanged();
                }
                // Scroll to bottom after a short delay to ensure layout is ready
                recyclerViewChat.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollToBottom();
                    }
                }, 100);
            } else {
                // No saved messages, show welcome message
                addWelcomeMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Add welcome message as fallback
            try {
                if (messages == null) {
                    messages = new ArrayList<>();
                }
                addWelcomeMessage();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void addWelcomeMessage() {
        try {
            if (messages == null || chatAdapter == null) {
                return;
            }
            String welcomeText = getString(R.string.welcome_message);
            String timestamp = getCurrentTimestamp();
            Message welcomeMessage = new Message(welcomeText, false, timestamp);
            messages.add(welcomeMessage);
            chatAdapter.notifyItemInserted(messages.size() - 1);
            scrollToBottom();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupNewChatButton() {
        try {
            ImageButton buttonNewChat = findViewById(R.id.buttonNewChat);
            if (buttonNewChat != null) {
                buttonNewChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimationUtils.bounce(v);
                        startNewChat();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startNewChat() {
        messages.clear();
        chatAdapter.notifyDataSetChanged();
        
        // Reset session
        currentSessionId = null;
        chatHistoryManager.clearCurrentChat();
        
        addWelcomeMessage();
        
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void setupSendButton() {
        try {
            if (buttonSend != null) {
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimationUtils.bounce(v);
                        sendMessage();
                    }
                });
            }
            
            if (editTextMessage != null) {
                editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
                    sendMessage();
                    return true;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeAdapterListener() {
        if (chatAdapter != null) {
            chatAdapter.setOnMessageClickListener(new ChatAdapter.OnMessageClickListener() {
                @Override
                public void onSpeakClick(String text) {
                    speak(text);
                }
            });
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS Language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "TTS Initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    
    private void setupProfile() {
        try {
            if (textViewProfile == null) {
                return;
            }
            SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
            String userName = prefs.getString("user_name", "User");
            
            String firstLetter = userName.length() > 0 ? String.valueOf(userName.charAt(0)).toUpperCase() : "U";
            textViewProfile.setText(firstLetter);
            textViewProfile.setBackgroundColor(getProfileColor(userName));
            
            textViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimationUtils.bounce(v);
                    showProfileDialog();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private int getProfileColor(String name) {
        // Generate consistent color based on name
        int[] colors = {
            Color.parseColor("#FF6F00"), // Orange
            Color.parseColor("#1E88E5"), // Blue
            Color.parseColor("#43A047"), // Green
            Color.parseColor("#E53935"), // Red
            Color.parseColor("#8E24AA"), // Purple
            Color.parseColor("#FB8C00"), // Deep Orange
            Color.parseColor("#00ACC1"), // Cyan
            Color.parseColor("#7CB342")  // Light Green
        };
        
        int hash = name.hashCode();
        return colors[Math.abs(hash) % colors.length];
    }
    
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }
        
        editTextMessage.setText("");
        
        String detectedLanguage = LanguageDetector.detectLanguage(messageText);
        
        String timestamp = getCurrentTimestamp();
        Message userMessage = new Message(messageText, true, timestamp, detectedLanguage);
        messages.add(userMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // Save chat after adding user message
        saveChat();
        
        // Save to history asynchronously
        saveCurrentChatToHistory();
        
        Message loadingMessage = new Message(getString(R.string.loading), false, getCurrentTimestamp());
        messages.add(loadingMessage);
        int loadingPosition = messages.size() - 1;
        chatAdapter.notifyItemInserted(loadingPosition);
        scrollToBottom();
        
        ApiRequest request = new ApiRequest(messageText, detectedLanguage);
        Call<ApiResponse> call = apiService.askQuestion(request);
        
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                messages.remove(loadingPosition);
                chatAdapter.notifyItemRemoved(loadingPosition);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String answer = apiResponse.getAnswer();
                        if (apiResponse.getSource() != null && !apiResponse.getSource().isEmpty()) {
                            answer += "\n\nSource: " + apiResponse.getSource();
                        }
                        Message botMessage = new Message(answer, false, getCurrentTimestamp(), detectedLanguage);
                        messages.add(botMessage);
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        scrollToBottom();
                        
                        // Save chat after bot response
                        saveChat();
                        
                        // Save to history asynchronously
                        saveCurrentChatToHistory();
                    } else {
                        showError(apiResponse.getError() != null ? apiResponse.getError() : getString(R.string.error_generic));
                    }
                } else {
                    showError(getString(R.string.error_generic));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                messages.remove(loadingPosition);
                chatAdapter.notifyItemRemoved(loadingPosition);
                
                showError(getString(R.string.error_network));
                t.printStackTrace();
            }
        });
    }
    
    private void saveChat() {
        // Save current chat
        chatHistoryManager.saveCurrentChat(messages);
    }
    
    private void scrollToBottom() {
        recyclerViewChat.post(new Runnable() {
            @Override
            public void run() {
                if (messages.size() > 0) {
                    int position = messages.size() - 1;
                    recyclerViewChat.smoothScrollToPosition(position);
                    View view = recyclerViewChat.getLayoutManager().findViewByPosition(position);
                    if (view != null) {
                        view.setAlpha(0f);
                        view.setTranslationY(20f);
                        view.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(300)
                                .start();
                    }
                }
            }
        });
    }
    
    private String getCurrentTimestamp() {
        Calendar calendar = Calendar.getInstance();
        return DateFormat.format("hh:mm a", calendar).toString();
    }
    
    private void showError(String errorMessage) {
        CustomSnackbar.showError(findViewById(android.R.id.content), errorMessage);
        Message errorMessageObj = new Message("Sorry, I couldn't process your request. " + errorMessage, false, getCurrentTimestamp());
        messages.add(errorMessageObj);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }
    
    
    private void clearChat() {
        messages.clear();
        chatHistoryManager.clearCurrentChat();
        chatAdapter.notifyDataSetChanged();
        addWelcomeMessage();
        // Refresh history list asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshHistoryList();
                    }
                });
            }
        }).start();
    }
    
    private void showChatHistory() {
        List<ChatHistoryManager.ChatSession> history = chatHistoryManager.getChatHistory();
        if (history.isEmpty()) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Chat History");
            builder.setMessage("No chat history available. Start a new conversation to see your chat history here.");
            builder.setPositiveButton("OK", null);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.show();
        } else {
            showChatHistoryDialog(history);
        }
    }
    
    private void showProfileDialog() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User");
        String userEmail = prefs.getString("user_email", "user@example.com");
        String userId = prefs.getString("user_id", "N/A");
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Profile");
        
        String profileInfo = "Name: " + userName + "\n\n" +
                            "Email: " + userEmail + "\n\n" +
                            "User ID: " + userId;
        
        builder.setMessage(profileInfo);
        builder.setPositiveButton("Close", null);
        builder.setNegativeButton("Logout", (dialog, which) -> {
            logout();
        });
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.show();
    }
    
    private void showSettingsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Settings");
        
        String[] settings = {
            "Notifications: Enabled",
            "Language: Auto-detect",
            "Clear Chat History"
        };
        
        builder.setItems(settings, (dialog, which) -> {
            if (which == 2) { // Clear History
                showClearHistoryDialog();
            }
        });
        builder.setPositiveButton("Close", null);
        builder.setIcon(android.R.drawable.ic_menu_preferences);
        builder.show();
    }
    
    private void showClearHistoryDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Clear History");
        builder.setMessage("Are you sure you want to delete all chat history?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            chatHistoryManager.clearAllHistory();
            refreshHistoryList();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
    
    private void showAboutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("About UoS Sindh Bot");
        
        String aboutText = "Version: 1.0.0\n\n" +
                          "University of Sindh\n" +
                          "Sindh Bot\n\n" +
                          "An AI-powered chatbot designed to help students and visitors get information about the University of Sindh.\n\n" +
                          "Powered by OpenAI GPT";
        
        builder.setMessage(aboutText);
        builder.setPositiveButton("Close", null);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.show();
    }
    
    private void showChatHistoryDialog(List<ChatHistoryManager.ChatSession> history) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chat History (" + history.size() + " chats)");
        
        // Create list of chat titles
        String[] chatTitles = new String[history.size()];
        for (int i = 0; i < history.size(); i++) {
            ChatHistoryManager.ChatSession session = history.get(i);
            String title = session.title != null && !session.title.isEmpty() 
                ? session.title 
                : "Chat " + (i + 1);
            
            // Add date if available
            if (session.timestamp > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(session.timestamp);
                String date = DateFormat.format("MMM dd, yyyy", cal).toString();
                title += " - " + date;
            }
            
            chatTitles[i] = title;
        }
        
        builder.setItems(chatTitles, (dialog, which) -> {
            // Load selected chat
            ChatHistoryManager.ChatSession selectedSession = history.get(which);
            if (selectedSession.messages != null && !selectedSession.messages.isEmpty()) {
                messages.clear();
                messages.addAll(selectedSession.messages);
                chatAdapter.notifyDataSetChanged();
                scrollToBottom();
                CustomSnackbar.showSuccess(findViewById(android.R.id.content), "Chat loaded successfully");
            }
        });
        
        builder.setNegativeButton("Close", null);
        builder.setIcon(android.R.drawable.ic_menu_recent_history);
        builder.show();
    }
    
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        try {
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        // Save chat when app goes to background
        saveChat();
    }
    
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
        // Save chat before destroying
        saveChat();
    }
}
