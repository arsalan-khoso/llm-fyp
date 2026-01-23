package com.uos.sindhbot.utils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.uos.sindhbot.R;

public class CustomSnackbar {
    
    public static void showSuccess(View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.uos_primary));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(parent.getContext().getResources().getColor(R.color.white));
        snackbar.show();
    }
    
    public static void showError(View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.uos_accent));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(parent.getContext().getResources().getColor(R.color.white));
        snackbar.show();
    }
    
    public static void showInfo(View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.uos_text_secondary));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(parent.getContext().getResources().getColor(R.color.white));
        snackbar.show();
    }
}

