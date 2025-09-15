package com.example.naraumi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class BaseActivity extends AppCompatActivity {
    

    private static final long DOUBLE_TAP_EXIT_TIMEOUT = 2 * 1000L; // 2 seconds
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        applyThemeColors();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateStreakDisplay();
        handleActivityRefresh();
        setupNavigationBar();

        setupWindowInsets();
    }

    private void applyThemeColors() {
        getWindow().setStatusBarColor(getColor(R.color.bg_Purple));
        getWindow().setNavigationBarColor(getColor(R.color.bg_Purple));
    }

    private void updateStreakDisplay() {
        TextView streakNumber = findViewById(R.id.streak_number);
        if (streakNumber != null) {
            int currentStreak = StreakManager.getCurrentStreak(this);
            streakNumber.setText(String.valueOf(currentStreak));
        }
    }

    private void handleActivityRefresh() {
        if (!(this instanceof MainActivity)) {
            ActivityRefreshManager.refreshCurrentActivity(this);
        }
    }

    private void setupNavigationBar() {
        View navigationBar = findViewById(R.id.float_nav);
        if (navigationBar == null) return;


        View scanButton = findViewById(R.id.nav_app_scan);
        if (scanButton != null) {
            scanButton.setOnClickListener(v -> navigateToActivity(ScanActivity.class));
        }

        View roadmapButton = findViewById(R.id.nav_app_rmap);
        if (roadmapButton != null) {
            roadmapButton.setOnClickListener(v -> navigateToActivity(MainActivity.class));
        }

        View decksButton = findViewById(R.id.nav_app_decks);
        if (decksButton != null) {
            decksButton.setOnClickListener(v -> navigateToActivity(DecksActivity.class));
        }
    }


    private void navigateToActivity(Class<?> targetActivityClass) {
        Intent navigationIntent = createNavigationIntent(targetActivityClass);
        startActivity(navigationIntent);
        overridePendingTransition(0, 0); // no animation
    }


    private Intent createNavigationIntent(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private void setupWindowInsets() {
        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView.getChildCount() > 0) {
            View contentView = rootView.getChildAt(0);
            if (contentView != null) {
                applyWindowInsets(contentView);
            }
        }
    }


    private void applyWindowInsets(View contentView) {
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            if (contentView.getId() == R.id.root_frame) {
                View mainLayout = contentView.findViewById(R.id.main);
                if (mainLayout != null) {
                    mainLayout.setPadding(
                        systemBars.left, 
                        systemBars.top, 
                        systemBars.right, 
                        systemBars.bottom
                    );
                }
            } else {

                contentView.setPadding(
                    systemBars.left, 
                    systemBars.top, 
                    systemBars.right, 
                    systemBars.bottom
                );
            }

            return insets;
        });
    }


    @Override
    public void onBackPressed() {
        if (this instanceof MainActivity) {
            handleMainActivityBackPress();
        } else {
            super.onBackPressed();
        }
    }


    private void handleMainActivityBackPress() {
        long currentTime = System.currentTimeMillis();
        
        if (lastBackPressTime + DOUBLE_TAP_EXIT_TIMEOUT > currentTime) {
            // if second tap within timeout then quit app
            super.onBackPressed();
        } else {
            // first tap warn to exit
            Toast.makeText(this, "Tap back once more to exit", Toast.LENGTH_SHORT).show();
            lastBackPressTime = currentTime;
        }
    }
}
