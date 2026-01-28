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
    
    // Save current chat sessions ID
    public void saveCurrentSessionId(String sessionId) {
        sharedPreferences.edit().putString("current_session_id", sessionId).apply();
    }

    public String getCurrentSessionId() {
        return sharedPreferences.getString("current_session_id", null);
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
    public void saveToHistory(List<Message> messages, String chatTitle, String sessionId) {
        List<ChatSession> history = getChatHistory();
        
        // Check if session with this ID already exists
        int existingIndex = -1;
        if (sessionId != null) {
            for (int i = 0; i < history.size(); i++) {
                if (sessionId.equals(history.get(i).id)) {
                    existingIndex = i;
                    break;
                }
            }
        }

        ChatSession session;
        if (existingIndex != -1) {
            // Update existing session
            session = history.get(existingIndex);
            session.messages = new ArrayList<>(messages);
            session.timestamp = System.currentTimeMillis();
            // Don't overwrite title if it's already set and not default, unless it was "New Chat"
            if (session.title == null || session.title.equals("New Chat") || session.title.isEmpty()) {
                 session.title = chatTitle;
            }
            
            // Move to top
            history.remove(existingIndex);
            history.add(0, session);
        } else {
            // Create new session
            session = new ChatSession();
            session.id = sessionId;
            session.title = chatTitle;
            session.messages = new ArrayList<>(messages);
            session.timestamp = System.currentTimeMillis();
            history.add(0, session);
        }
        
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
        // Migration: Assign IDs to old sessions if missing
        if (history != null) {
            for (ChatSession session : history) {
                if (session.id == null) {
                    session.id = java.util.UUID.randomUUID().toString();
                }
            }
        }
        return history != null ? history : new ArrayList<>();
    }
    
    // Clear current chat
    public void clearCurrentChat() {
        sharedPreferences.edit().remove(KEY_CURRENT_CHAT).remove("current_session_id").apply();
    }
    
    // Clear all history
    public void clearAllHistory() {
        sharedPreferences.edit().clear().apply();
    }
    
    public static class ChatSession {
        public String id;
        public String title;
        public List<Message> messages;
        public long timestamp;
    }
}

