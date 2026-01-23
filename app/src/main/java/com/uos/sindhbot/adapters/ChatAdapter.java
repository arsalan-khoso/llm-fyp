package com.uos.sindhbot.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.uos.sindhbot.R;
import com.uos.sindhbot.models.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    
    private List<Message> messages;
    
    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        
        holder.textViewMessage.setText(message.getText());
        holder.textViewTime.setText(message.getTimestamp());
        
        // Get the root LinearLayout from itemView
        LinearLayout rootLayout = (LinearLayout) holder.itemView;
        LinearLayout.LayoutParams messageParams = (LinearLayout.LayoutParams) holder.layoutMessage.getLayoutParams();
        
        if (message.isUser()) {
            holder.layoutMessage.setBackgroundResource(R.drawable.message_user_background);
            // User messages: white text on cyan background
            holder.textViewMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.textViewTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            rootLayout.setGravity(Gravity.END);
            if (messageParams != null) {
                messageParams.gravity = Gravity.END;
            }
            rootLayout.setPadding(60, 8, 16, 8);
        } else {
            holder.layoutMessage.setBackgroundResource(R.drawable.message_bot_background);
            // Bot messages: white text on dark gray background
            holder.textViewMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.uos_text_primary));
            holder.textViewTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.uos_text_secondary));
            // Show copy button for bot messages
            if (holder.buttonCopy != null) {
                holder.buttonCopy.setVisibility(View.VISIBLE);
                holder.buttonCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyToClipboard(holder.itemView.getContext(), message.getText());
                    }
                });
            }
            rootLayout.setGravity(Gravity.START);
            if (messageParams != null) {
                messageParams.gravity = Gravity.START;
            }
            rootLayout.setPadding(16, 8, 60, 8);
        }
        
        // Hide copy button for user messages
        if (message.isUser() && holder.buttonCopy != null) {
            holder.buttonCopy.setVisibility(View.GONE);
        }
        
        if (messageParams != null) {
            holder.layoutMessage.setLayoutParams(messageParams);
        }
        
        // Animate message appearance
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(20f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(Math.min(position * 30, 300))
                .start();
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    private void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Message", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMessage;
        TextView textViewMessage;
        TextView textViewTime;
        ImageButton buttonCopy;
        
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMessage = itemView.findViewById(R.id.layoutMessage);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            buttonCopy = itemView.findViewById(R.id.buttonCopy);
        }
    }
}

