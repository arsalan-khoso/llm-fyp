package com.uos.sindhbot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uos.sindhbot.models.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryManager {
    private static final String PREF_NAME = "ChatHistory";
    private static final String KEY_CHAT_HISTORY = "chat_history";
    private static final String KEY_CURRENT_CHAT = "current_chat";
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public ChatHistoryManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // Save current chat messages
    public void saveCurrentChat(List<Message> messages) {
        String json = gson.toJson(messages);
        sharedPreferences.edit().putString(KEY_CURRENT_CHAT, json).apply();
    }
    
    // Load current chat messages
    public List<Message> loadCurrentChat() {
        String json = sharedPreferences.getString(KEY_CURRENT_CHAT, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Message>>(){}.getType();
        List<Message> messages = gson.fromJson(json, type);
        return messages != null ? messages : new ArrayList<>();
    }
    
    // Save chat to history (with timestamp)
    public void saveToHistory(List<Message> messages, String chatTitle) {
        List<ChatSession> history = getChatHistory();
        
        ChatSession session = new ChatSession();
        session.title = chatTitle;
        session.messages = new ArrayList<>(messages);
        session.timestamp = System.currentTimeMillis();
        
        history.add(0, session); // Add to beginning
        
        // Keep only last 50 chats
        if (history.size() > 50) {
            history = history.subList(0, 50);
        }
        
        String json = gson.toJson(history);
        sharedPreferences.edit().putString(KEY_CHAT_HISTORY, json).apply();
    }
    
    // Get all chat history
    public List<ChatSession> getChatHistory() {
        String json = sharedPreferences.getString(KEY_CHAT_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<ChatSession>>(){}.getType();
        List<ChatSession> history = gson.fromJson(json, type);
        return history != null ? history : new ArrayList<>();
    }
    
    // Clear current chat
    public void clearCurrentChat() {
        sharedPreferences.edit().remove(KEY_CURRENT_CHAT).apply();
    }
    
    // Clear all history
    public void clearAllHistory() {
        sharedPreferences.edit().clear().apply();
    }
    
    public static class ChatSession {
        public String title;
        public List<Message> messages;
        public long timestamp;
    }
}

