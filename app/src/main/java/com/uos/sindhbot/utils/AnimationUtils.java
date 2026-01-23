package com.uos.sindhbot.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

public class AnimationUtils {
    
    public static void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);
    }
    
    public static void fadeOut(View view, Runnable onComplete) {
        view.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }
    
    public static void scaleIn(View view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.8f, 1.0f,
                0.8f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(300);
        scaleAnimation.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(scaleAnimation);
    }
    
    public static void slideInFromRight(View view) {
        view.setTranslationX(view.getWidth());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);
    }
    
    public static void slideInFromBottom(View view) {
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);
    }
    
    public static void bounce(View view) {
        view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(150)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }
}

