package com.example.naraumi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.Arrays;
import java.util.HashSet;

public class LessonActivity extends BaseActivity {
    
    private static final String PROGRESS_PREFS = "JapLearnProgress";
    private static final String[] ACTIVITY_TYPES = {"MATCH", "FLASH", "WRITE"};
    private static final String LESSON_TYPE_BASICS_1 = "BASICS_1";
    private static final String LESSON_TYPE_BASICS_2 = "BASICS_2";
    private static final String LESSON_TYPE_ADVANCED_1 = "ADVANCED_1";
    private static final String LESSON_TYPE_ADVANCED_2 = "ADVANCED_2";
    
    private String currentKanaType;
    private String selectedKanaGroup;
    private String currentLessonType;
    private boolean isPhraseMode = false;
    private int basicsCircleIndex = 0;
    private int totalGroupsToComplete = 0;
    private String[] phraseGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        extractIntentData();
        initialiseUIComponents();
        setupActivityButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLessonDisplay();
        setupActivityButtons();
        
        if (ActivityRefreshManager.isRefreshNeeded()) {
            ActivityRefreshManager.clearRefreshNeeded();
        }
    }
    
        private void extractIntentData() {
        currentKanaType = getIntent().getStringExtra("KANA_TYPE");
        currentLessonType = getIntent().getStringExtra("LESSON_TYPE");
        basicsCircleIndex = getIntent().getIntExtra("BASICS_CIRCLE_INDEX", 0);
    }
    
        private void initialiseUIComponents() {
        TextView headerTitle = findViewById(R.id.header_title);
        TextView kanaTypeHeader = findViewById(R.id.kana_type_header);

        if (currentKanaType != null) {
            headerTitle.setText("KANA");
            kanaTypeHeader.setText(currentKanaType);
            setupKanaLearningGroups();
        } else {
            setupPhraseLearning(headerTitle, kanaTypeHeader);
        }
    }
    
        private void setupPhraseLearning(TextView headerTitle, TextView kanaTypeHeader) {
        isPhraseMode = true;
        
        switch (currentLessonType) {
            case LESSON_TYPE_BASICS_1:
                currentKanaType = "BASICS 1";
                headerTitle.setText("LESSON: BASICS 1");
                break;
            case LESSON_TYPE_BASICS_2:
                currentKanaType = "BASICS 2";
                headerTitle.setText("LESSON: BASICS 2");
                break;
            case LESSON_TYPE_ADVANCED_1:
                currentKanaType = "ADVANCED 1";
                headerTitle.setText("LESSON: ADVANCED 1");
                break;
            case LESSON_TYPE_ADVANCED_2:
                currentKanaType = "ADVANCED 2";
                headerTitle.setText("LESSON: ADVANCED 2");
                break;
            default:
                headerTitle.setText("Unknown Lesson");
                kanaTypeHeader.setText("");
                return;
        }
        
        setupPhraseGroups(kanaTypeHeader);
    }
    
        private void refreshLessonDisplay() {
        TextView kanaTypeHeader = findViewById(R.id.kana_type_header);
        
        if (isPhraseBasedLesson()) {
            setupPhraseGroups(kanaTypeHeader);
        } else {
            setupKanaLearningGroups();
        }
    }
    
        private boolean isPhraseBasedLesson() {
        return "BASICS 1".equals(currentKanaType) || "BASICS 2".equals(currentKanaType) || 
               "ADVANCED 1".equals(currentKanaType) || "ADVANCED 2".equals(currentKanaType);
    }
    
        private void setupKanaLearningGroups() {
        FlexboxLayout groupContainer = findViewById(R.id.kana_container);
        groupContainer.removeAllViews();

        String[] kanaGroups = getKanaCharacterGroups(currentKanaType);
        totalGroupsToComplete = kanaGroups.length;

        SharedPreferences progressPrefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE);
        progressPrefs.edit().putInt(currentKanaType + "_TOTAL_GROUPS", totalGroupsToComplete).apply();

        for (String group : kanaGroups) {
            addGroupBubble(groupContainer, group);
        }
    }

        private void setupPhraseGroups(TextView kanaTypeHeader) {
        FlexboxLayout groupContainer = findViewById(R.id.kana_container);
        groupContainer.removeAllViews();

        determinePhraseLessonContent(kanaTypeHeader);
        totalGroupsToComplete = phraseGroups.length;

        SharedPreferences progressPrefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE);
        progressPrefs.edit().putInt("BASICS_TOTAL_GROUPS", totalGroupsToComplete).apply();

        for (String group : phraseGroups) {
            addGroupBubble(groupContainer, group);
        }
    }
    
        private void determinePhraseLessonContent(TextView kanaTypeHeader) {
        if ("BASICS 1".equals(currentKanaType)) {
            setupBasicsContent(kanaTypeHeader);}
        else if ("BASICS 2".equals(currentKanaType)) {
            setupBasics2Content(kanaTypeHeader);
        } else if ("ADVANCED 1".equals(currentKanaType)) {
            setupAdvanced1Content(kanaTypeHeader);
        } else if ("ADVANCED 2".equals(currentKanaType)) {
            setupAdvanced2Content(kanaTypeHeader);
        } else {
            setupBasicsContent(kanaTypeHeader);
        }
    }
    
        private void setupBasics2Content(TextView kanaTypeHeader) {
        if (basicsCircleIndex == 0) {
            phraseGroups = new String[]{"Classroom 1", "Classroom 2"};
            kanaTypeHeader.setText("Classroom Vocabulary");
            saveGroupMapping("BASICS_2Classroom", phraseGroups);
        } else if (basicsCircleIndex == 1) {
            phraseGroups = new String[]{"Class Items"};
            kanaTypeHeader.setText("Class Items");
            saveGroupMapping("BASICS_2Class Items", phraseGroups);
        } else {
            phraseGroups = new String[]{"Classroom 1"};
            kanaTypeHeader.setText("Misc Phrases");
        }
    }
    
        private void setupAdvanced1Content(TextView kanaTypeHeader) {
        if (basicsCircleIndex == 0) {
            phraseGroups = new String[]{"Travel 1", "Travel 2"};
            kanaTypeHeader.setText("Travel Japanese");
            saveGroupMapping("ADVANCED_1Travel", phraseGroups);
        } else if (basicsCircleIndex == 1) {
            phraseGroups = new String[]{"Food Items", "Restaurant"};
            kanaTypeHeader.setText("Food & Dining");
            saveGroupMapping("ADVANCED_1Food & Dining", phraseGroups);
        } else {
            phraseGroups = new String[]{"Travel 1"};
            kanaTypeHeader.setText("Misc Phrases");
        }
    }
    
        private void setupAdvanced2Content(TextView kanaTypeHeader) {
        if (basicsCircleIndex == 0) {
            phraseGroups = new String[]{"Business 1", "Business 2"};
            kanaTypeHeader.setText("Business Japanese");
            saveGroupMapping("ADVANCED_2Business", phraseGroups);
        } else if (basicsCircleIndex == 1) {
            phraseGroups = new String[]{"Formal Greetings", "Polite Speech"};
            kanaTypeHeader.setText("Formal Speech");
            saveGroupMapping("ADVANCED_2Formal Speech", phraseGroups);
        } else {
            phraseGroups = new String[]{"Business 1"};
            kanaTypeHeader.setText("Misc Phrases");
        }
    }
    
        private void setupBasicsContent(TextView kanaTypeHeader) {
        if (basicsCircleIndex == 0) {
            phraseGroups = new String[]{"Greetings 1", "Greetings 2"};
            kanaTypeHeader.setText("Everyday Greetings");
            saveGroupMapping("BASICS_1Greetings", phraseGroups);
        } else if (basicsCircleIndex == 1) {
            phraseGroups = new String[]{"Office Items"};
            kanaTypeHeader.setText("Office Items");
            saveGroupMapping("BASICS_1Office Items", phraseGroups);
        } else {
            phraseGroups = new String[]{"Greetings 1"};
            kanaTypeHeader.setText("Misc Phrases");
        }
    }
    
        private void saveGroupMapping(String key, String[] groups) {
        SharedPreferences progressPrefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE);
        progressPrefs.edit().putStringSet(key, new HashSet<>(Arrays.asList(groups))).apply();
    }

        private void addGroupBubble(FlexboxLayout container, String groupName) {
        TextView groupBubble = new TextView(this);

        int bubbleWidth = (int) (getResources().getDimensionPixelSize(R.dimen.circle_button_size) * 1.4f);
        int bubbleHeight = getResources().getDimensionPixelSize(R.dimen.circle_button_size);
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(bubbleWidth, bubbleHeight);
        layoutParams.setMargins(16, 16, 16, 16);
        groupBubble.setLayoutParams(layoutParams);

        groupBubble.setSingleLine(false);
        groupBubble.setMaxLines(2);
        groupBubble.setEllipsize(TextUtils.TruncateAt.END);
        groupBubble.setGravity(Gravity.CENTER);
        groupBubble.setText(groupName);

        boolean hasCompletedLessons = LessonProgressManager.hasAnyCompletedLessons(
            this, currentKanaType, groupName);

        groupBubble.setBackgroundResource(hasCompletedLessons ?
            R.drawable.lesson_group_btn_completed : R.drawable.lesson_group_btn);

        groupBubble.setTextColor(Color.WHITE);
        groupBubble.setTextSize(16);
        groupBubble.setClickable(true);
        groupBubble.setFocusable(true);

        groupBubble.setOnClickListener(v -> openGroupLearningActivity(groupName));

        container.addView(groupBubble);
    }
    
        private void openGroupLearningActivity(String groupName) {
        selectedKanaGroup = groupName;
        Intent intent = new Intent(this, KanaLearningActivity.class);
        intent.putExtra("KANA_TYPE", currentKanaType);
        intent.putExtra("KANA_GROUP", groupName);
        intent.putExtra("PHRASE_MODE", isPhraseMode);
        startActivity(intent);
    }
    
        private void setupActivityButtons() {
        FlexboxLayout activitiesContainer = findViewById(R.id.activities_container);
        activitiesContainer.removeAllViews();

        int buttonSize = getResources().getDimensionPixelSize(R.dimen.circle_button_size);
        int buttonMargin = getResources().getDimensionPixelSize(R.dimen.circle_button_margin);

        LinearLayout mainActivitiesRow = createMainActivitiesRow(buttonSize, buttonMargin);
        activitiesContainer.addView(mainActivitiesRow);

        if (shouldShowReadAndPointButton()) {
            LinearLayout readPointRow = createReadAndPointRow(buttonSize, buttonMargin);
            activitiesContainer.addView(readPointRow);
        }
    }
    
        private LinearLayout createMainActivitiesRow(int buttonSize, int buttonMargin) {
        LinearLayout activitiesRow = new LinearLayout(this);
        activitiesRow.setOrientation(LinearLayout.HORIZONTAL);
        activitiesRow.setGravity(Gravity.CENTER_HORIZONTAL);
        activitiesRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        for (String activityType : ACTIVITY_TYPES) {
            TextView activityButton = createActivityButton(activityType, buttonSize, buttonMargin);
            activitiesRow.addView(activityButton);
        }

        return activitiesRow;
    }
    
        private TextView createActivityButton(String activityType, int buttonSize, int buttonMargin) {
        TextView activityButton = new TextView(this);
        
        int buttonWidth = (int) (buttonSize * 1.4f);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttonWidth, buttonSize);
        buttonParams.setMargins(buttonMargin, 0, buttonMargin, 0);
        activityButton.setLayoutParams(buttonParams);

        activityButton.setSingleLine(false);
        activityButton.setMaxLines(2);
        activityButton.setText(activityType);
        activityButton.setTextColor(Color.WHITE);
        activityButton.setTextSize(14);
        activityButton.setGravity(Gravity.CENTER);
        activityButton.setClickable(true);
        activityButton.setFocusable(true);

        boolean isActivityCompleted = checkActivityCompletion(activityType);
        activityButton.setBackgroundResource(isActivityCompleted ?
            R.drawable.lesson_group_btn_completed : R.drawable.lesson_group_btn);

        activityButton.setOnClickListener(v -> handleActivityClick(activityType));

        return activityButton;
    }
    
        private boolean checkActivityCompletion(String activityType) {
        if ("KANA".equals(currentKanaType)) {
            return checkKanaActivityCompletion(activityType);
        } else if (isPhraseBasedLesson()) {
            return checkPhraseActivityCompletion(activityType);
        }
        return false;
    }
    
        private boolean checkKanaActivityCompletion(String activityType) {
        String[] kanaGroups = getKanaCharacterGroups(currentKanaType);
        for (String kanaGroup : kanaGroups) {
            String lessonKey = LessonProgressManager.createLessonKey(currentKanaType, kanaGroup, activityType);
            if (LessonProgressManager.isLessonCompleted(this, lessonKey)) {
                return true;
            }
        }
        return false;
    }
    
        private boolean checkPhraseActivityCompletion(String activityType) {
        for (String phraseGroup : phraseGroups) {
            String lessonKey = LessonProgressManager.createLessonKey(currentKanaType, phraseGroup, activityType);
            if (LessonProgressManager.isLessonCompleted(this, lessonKey)) {
                return true;
            }
        }
        return false;
    }
    
        private boolean shouldShowReadAndPointButton() {
        return isPhraseBasedLesson() && basicsCircleIndex == 1;
    }
    
        private LinearLayout createReadAndPointRow(int buttonSize, int buttonMargin) {
        LinearLayout readPointRow = new LinearLayout(this);
        readPointRow.setOrientation(LinearLayout.HORIZONTAL);
        readPointRow.setGravity(Gravity.CENTER_HORIZONTAL);
        readPointRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView readPointButton = createReadAndPointButton(buttonSize, buttonMargin);
        readPointRow.addView(readPointButton);

        return readPointRow;
    }
    
        private TextView createReadAndPointButton(int buttonSize, int buttonMargin) {
        TextView readPointButton = new TextView(this);
        
        int buttonWidth = (int) (buttonSize * 1.4f);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttonWidth, buttonSize);
        buttonParams.setMargins(buttonMargin, buttonMargin * 2, buttonMargin, 0);
        readPointButton.setLayoutParams(buttonParams);

        readPointButton.setText("READ\n&\nPOINT");
        readPointButton.setTextColor(Color.WHITE);
        readPointButton.setTextSize(14);
        readPointButton.setGravity(Gravity.CENTER);
        readPointButton.setBackgroundResource(R.drawable.lesson_group_btn);
        readPointButton.setMaxLines(3);
        readPointButton.setClickable(true);
        readPointButton.setFocusable(true);

        readPointButton.setOnClickListener(v -> openReadAndPointActivity());

        return readPointButton;
    }
    
        private void openReadAndPointActivity() {
        Intent readIntent = new Intent(this, ReadAndPointActivity.class);
        readIntent.putExtra("LESSON_TYPE", currentKanaType);
        readIntent.putExtra("BASICS_CIRCLE_INDEX", basicsCircleIndex);
        startActivity(readIntent);
    }

        private void handleActivityClick(String activityType) {
        Intent activityIntent = null;
        
        switch (activityType) {
            case "MATCH":
                activityIntent = new Intent(this, MatchGroupSelectionActivity.class);
                break;
            case "FLASH":
                activityIntent = new Intent(this, FlashGroupSelectionActivity.class);
                break;
            case "WRITE":
                activityIntent = new Intent(this, WriteGroupSelectionActivity.class);
                break;
        }

        if (activityIntent != null) {
            activityIntent.putExtra("KANA_TYPE", currentKanaType);
            activityIntent.putExtra("PHRASE_MODE", isPhraseMode);
            activityIntent.putExtra("BASICS_CIRCLE_INDEX", basicsCircleIndex);
            startActivity(activityIntent);
        }
    }

        private String[] getKanaCharacterGroups(String kanaType) {
        switch (kanaType) {
            case "HIRAGANA":
                return new String[]{"あーか", "さーた", "なーは", "ま", "やーら", "わーんーを"};
            case "KATAKANA":
                return new String[]{"アーカ", "サーター", "ナーハ", "マ", "ヤーラ", "ワーンーヲ"};
            case "REVIEW":
                return new String[]{"Mixed Practice", "Weak Points", "Speed Test"};
            default:
                return new String[]{};
        }
    }
}