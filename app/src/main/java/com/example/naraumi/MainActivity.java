package com.example.naraumi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import java.io.IOException;

/**
 * The home screen of the app, displaying the main lesson roadmap.
 */
public class MainActivity extends BaseActivity {

        private static final String[] LESSON_CATEGORIES = {
        "KANA", "BASICS 1", "BASICS 2", "ADVANCED 1", "ADVANCED 2"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_main);

        checkForFirstLaunch();
        initialiseDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        buildLessonRoadmap();
        
        if (ActivityRefreshManager.isRefreshNeeded()) {
            ActivityRefreshManager.refreshCurrentActivity(this);
        }
    }

        private void checkForFirstLaunch() {
        SharedPreferences userPrefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = userPrefs.getBoolean("first_launch", true);
        boolean tutorialCompleted = userPrefs.getBoolean("tutorial_completed", false);

        if (isFirstLaunch && !tutorialCompleted) {
            showWelcomeTutorialOverlay();
        }
    }

        private void showWelcomeTutorialOverlay() {
        View tutorialOverlay = findViewById(R.id.tutorial_overlay);
        if (tutorialOverlay == null) return;

                tutorialOverlay.setVisibility(View.VISIBLE);
        tutorialOverlay.setAlpha(0f);
        tutorialOverlay.animate().alpha(1f).setDuration(300).start();

        SharedPreferences userPrefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);

        tutorialOverlay.findViewById(R.id.start_tutorial_btn).setOnClickListener(v -> {
            hideTutorialOverlayAndStart(tutorialOverlay, userPrefs, true);
        });

        tutorialOverlay.findViewById(R.id.skip_tutorial_btn).setOnClickListener(v -> {
            hideTutorialOverlayAndStart(tutorialOverlay, userPrefs, false);
        });
    }

        private void hideTutorialOverlayAndStart(View overlay, SharedPreferences prefs, boolean startTutorial) {
        overlay.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            overlay.setVisibility(View.GONE);
            
            prefs.edit()
                    .putBoolean("first_launch", false)
                    .putBoolean("tutorial_seen", true)
                    .apply();

            if (startTutorial) {
                new TutorialSequenceManager(MainActivity.this).startTutorial();
            }
        }).start();
    }

        private void initialiseDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        private void buildLessonRoadmap() {
        LinearLayout lessonContainer = findViewById(R.id.lesson_container);
        if (lessonContainer == null) return;

        lessonContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int categoryIndex = 0; categoryIndex < LESSON_CATEGORIES.length; categoryIndex++) {
            final String categoryTitle = LESSON_CATEGORIES[categoryIndex];
            
            View categorySection = createCategorySection(inflater, lessonContainer, categoryTitle);
            
            LinearLayout buttonContainer = categorySection.findViewById(R.id.button_container);
            
            String[] lessonGroups = getLessonGroupsForCategory(categoryTitle);
            
            createLessonButtons(inflater, buttonContainer, categoryTitle, lessonGroups, categoryIndex);
            
            lessonContainer.addView(categorySection);
        }
    }

        private View createCategorySection(LayoutInflater inflater, LinearLayout parent, String categoryTitle) {
        View categorySection = inflater.inflate(R.layout.lesson_section, parent, false);
        
        TextView titleText = categorySection.findViewById(R.id.section_title);
        titleText.setText(categoryTitle);
        
        return categorySection;
    }

        private String[] getLessonGroupsForCategory(String categoryTitle) {
        switch (categoryTitle) {
            case "KANA":
                return new String[]{"HIRAGANA", "KATAKANA"};
            case "BASICS 1":
                return new String[]{"Greetings", "Office Items"};
            case "BASICS 2":
                return new String[]{"Classroom", "Class Items"};
            case "ADVANCED 1":
                return new String[]{"Travel", "Food & Dining"};
            case "ADVANCED 2":
                return new String[]{"Business", "Formal Speech"};
            default:
                return new String[]{"Coming Soon"};
        }
    }

        private void createLessonButtons(LayoutInflater inflater, LinearLayout buttonContainer,
                                   String categoryTitle, String[] lessonGroups, int categoryIndex) {
        
        for (int groupIndex = 0; groupIndex < lessonGroups.length; groupIndex++) {
            final String groupLabel = lessonGroups[groupIndex];
            final int finalGroupIndex = groupIndex;
            
            TextView lessonButton = (TextView) inflater.inflate(R.layout.lesson_section_button, buttonContainer, false);
            lessonButton.setText(groupLabel);
            
            boolean isCompleted = isLessonGroupCompleted(categoryTitle, groupLabel);
            lessonButton.setBackgroundResource(isCompleted ? 
                R.drawable.rmap_green_button : R.drawable.rmap_grey_button);
            
            setLessonButtonLayoutParams(lessonButton);
            
            lessonButton.setOnClickListener(v -> {
                navigateToLessonActivity(categoryTitle, groupLabel, finalGroupIndex);
            });
            
            buttonContainer.addView(lessonButton);
        }
    }

        private boolean isLessonGroupCompleted(String categoryTitle, String groupLabel) {
        if ("KANA".equals(categoryTitle)) {
            SharedPreferences progressPrefs = getSharedPreferences("JapLearnProgress", MODE_PRIVATE);
            return isKanaGroupCompleted(progressPrefs, groupLabel);
        } else {
            return LessonProgressManager.areAllSubgroupsCompleted(this, categoryTitle, groupLabel);
        }
    }

        private boolean isKanaGroupCompleted(SharedPreferences prefs, String kanaType) {
        int totalGroups = prefs.getInt(kanaType + "_TOTAL_GROUPS", -1);
        int completedGroups = LessonProgressManager.countUniqueCompletedGroups(this, kanaType);
        return (totalGroups > 0 && completedGroups == totalGroups);
    }

        private void setLessonButtonLayoutParams(TextView button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            getResources().getDimensionPixelSize(R.dimen.rmap_grey_button)
        );
        params.setMargins(16, 0, 16, 0);
        button.setLayoutParams(params);
    }

        private void navigateToLessonActivity(String categoryTitle, String groupLabel, int groupIndex) {
        Intent lessonIntent = new Intent(MainActivity.this, LessonActivity.class);
        
        if ("KANA".equals(categoryTitle)) {
            lessonIntent.putExtra("KANA_TYPE", groupLabel);
        } else {
            String lessonType = categoryTitle.replace(" ", "_");
            lessonIntent.putExtra("LESSON_TYPE", lessonType);
            lessonIntent.putExtra("BASICS_CIRCLE_INDEX", groupIndex);
        }
        
        startActivity(lessonIntent);
    }
}
