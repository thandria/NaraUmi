package com.example.naraumi;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;


public class AppButtonStyle {


    private static final int DEFAULT_BUTTON_PADDING = 24;
    private static final int DEFAULT_TOP_MARGIN = 24;

    public static TextView createButton(Context context, String buttonText) {
        return createButton(context, buttonText, R.drawable.rounded_header_bg);
    }
    

    public static TextView createButton(Context context, String buttonText, int backgroundResourceId) {
        TextView styledButton = new TextView(context);
        
        configureButtonText(styledButton, buttonText);
        configureButtonStyling(context, styledButton, backgroundResourceId);
        configureButtonLayout(styledButton);
        
        return styledButton;
    }
    

    public static TextView createCompactButton(Context context, String buttonText) {
        TextView compactButton = createButton(context, buttonText);

        int compactPadding = DEFAULT_BUTTON_PADDING / 2;
        compactButton.setPadding(compactPadding, compactPadding, compactPadding, compactPadding);
        
        return compactButton;
    }

    private static void configureButtonText(TextView button, String text) {
        button.setText(text);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
    }
    

    private static void configureButtonStyling(Context context, TextView button, int backgroundResourceId) {
        button.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        button.setBackgroundResource(backgroundResourceId);
        button.setPadding(DEFAULT_BUTTON_PADDING, DEFAULT_BUTTON_PADDING, 
                         DEFAULT_BUTTON_PADDING, DEFAULT_BUTTON_PADDING);
    }
    

    private static void configureButtonLayout(TextView button) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, DEFAULT_TOP_MARGIN, 0, 0);
        button.setLayoutParams(layoutParams);
    }
    

    public static void applyClickEffects(TextView button) {
        button.setClickable(true);
        button.setFocusable(true);
        
        //press effect try
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        });
    }
    

    public static TextView createCustomSizedButton(Context context, String buttonText, 
                                                  int widthDp, int heightDp) {
        TextView customButton = createButton(context, buttonText);
        
        // convert dp to pixels
        float density = context.getResources().getDisplayMetrics().density;
        int widthPx = (int) (widthDp * density);
        int heightPx = (int) (heightDp * density);
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(widthPx, heightPx);
        layoutParams.setMargins(0, DEFAULT_TOP_MARGIN, 0, 0);
        customButton.setLayoutParams(layoutParams);
        
        return customButton;
    }
} 