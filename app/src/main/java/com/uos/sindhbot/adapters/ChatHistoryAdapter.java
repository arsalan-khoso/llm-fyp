package com.uos.sindhbot.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uos.sindhbot.R;
import com.uos.sindhbot.utils.ChatHistoryManager;

import java.util.Calendar;
import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.HistoryViewHolder> {
    
    private List<ChatHistoryManager.ChatSession> history;
    private OnHistoryItemClickListener listener;
    
    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(ChatHistoryManager.ChatSession session);
    }
    
    public ChatHistoryAdapter(List<ChatHistoryManager.ChatSession> history, OnHistoryItemClickListener listener) {
        this.history = history;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_history, parent, false);
        return new HistoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChatHistoryManager.ChatSession session = history.get(position);
        
        // Get first user message as title
        String title = "New Chat";
        if (session.messages != null && !session.messages.isEmpty()) {
            for (com.uos.sindhbot.models.Message msg : session.messages) {
                if (msg.isUser()) {
                    String msgText = msg.getText();
                    if (msgText.length() > 50) {
                        title = msgText.substring(0, 50) + "...";
                    } else {
                        title = msgText;
                    }
                    break;
                }
            }
        }
        
        holder.textViewTitle.setText(title);
        
        // Format date
        if (session.timestamp > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(session.timestamp);
            String date = DateFormat.format("MMM dd, yyyy hh:mm a", cal).toString();
            holder.textViewDate.setText(date);
        } else {
            holder.textViewDate.setText("");
        }
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onHistoryItemClick(session);
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return history != null ? history.size() : 0;
    }
    
    public void updateHistory(List<ChatHistoryManager.ChatSession> newHistory) {
        this.history = newHistory;
        notifyDataSetChanged();
    }
    
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDate;
        
        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewChatTitle);
            textViewDate = itemView.findViewById(R.id.textViewChatDate);
        }
    }
}

