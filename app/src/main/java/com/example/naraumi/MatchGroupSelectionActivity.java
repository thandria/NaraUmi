package com.example.naraumi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class MatchGroupSelectionActivity extends BaseActivity {


        private static final int GROUP_BUTTON_BACKGROUND = R.drawable.rounded_header_bg;
    
        private static final int BUTTON_PADDING = 24;
    
        private static final int BUTTON_MARGIN = 24;
    
        private static final String ACTIVITY_TITLE_PREFIX = "MATCH: ";


        private String lessonType;
    
        private boolean isPhraseMode;
    
        private int basicsLessonIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_group_selection);

        extractIntentData();
        setupHeaderDisplay();
        setupGroupSelection();
    }


        private void extractIntentData() {
        lessonType = getIntent().getStringExtra("KANA_TYPE");
        isPhraseMode = getIntent().getBooleanExtra("PHRASE_MODE", false);
        basicsLessonIndex = getIntent().getIntExtra("BASICS_CIRCLE_INDEX", 0);
    }
    
        private void setupHeaderDisplay() {
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(ACTIVITY_TITLE_PREFIX + lessonType);
        }
    }
    
        private void setupGroupSelection() {
        LinearLayout groupContainer = findViewById(R.id.match_group_container);
        String[] availableGroups = getAvailableGroups();

        for (String groupName : availableGroups) {
            Button groupButton = createGroupButton(groupName);
            groupContainer.addView(groupButton);
        }
    }


        private Button createGroupButton(String groupName) {
        Button groupButton = new Button(this);
        
        setupButtonAppearance(groupButton, groupName);
        setupButtonLayout(groupButton);
        setupButtonClickHandler(groupButton, groupName);
        
        return groupButton;
    }
    
        private void setupButtonAppearance(Button button, String groupName) {
        button.setText(groupName);
        button.setAllCaps(false);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        button.setBackgroundResource(GROUP_BUTTON_BACKGROUND);
        button.setPadding(BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING);
    }
    
        private void setupButtonLayout(Button button) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, BUTTON_MARGIN, 0, 0);
        button.setLayoutParams(layoutParams);
    }
    
        private void setupButtonClickHandler(Button button, String groupName) {
        button.setOnClickListener(v -> launchMatchingGame(groupName));
    }
    
        private void launchMatchingGame(String selectedGroup) {
        Intent matchingIntent = new Intent(this, MatchTilesActivity.class);
        matchingIntent.putExtra("KANA_TYPE", lessonType);
        matchingIntent.putExtra("KANA_GROUP", selectedGroup);
        matchingIntent.putExtra("PHRASE_MODE", isPhraseMode);
        startActivity(matchingIntent);
    }


        private String[] getAvailableGroups() {
        if (isPhraseMode) {
            return getPhraseModeGroups();
        } else {
            return getCharacterModeGroups();
        }
    }
    
        private String[] getPhraseModeGroups() {
        switch (lessonType) {
            case "BASICS 1":
                return getBasicsGroups();
            case "BASICS 2":
                return getBasics2Groups();
            case "ADVANCED 1":
                return getAdvanced1Groups();
            case "ADVANCED 2":
                return getAdvanced2Groups();
            default:
                return new String[]{"Misc Phrases"};
        }
    }
    
        private String[] getCharacterModeGroups() {
        switch (lessonType) {
            case "HIRAGANA":
                return getHiraganaGroups();
            case "KATAKANA":
                return getKatakanaGroups();
            default:
                return new String[]{};
        }
    }


        private String[] getBasicsGroups() {
        if (basicsLessonIndex == 0) {
            return new String[]{"Greetings 1", "Greetings 2"};
        } else if (basicsLessonIndex == 1) {
            return new String[]{"Office Items"};
        } else {
            return new String[]{"Misc Phrases"};
        }
    }
    
        private String[] getBasics2Groups() {
        if (basicsLessonIndex == 0) {
            return new String[]{"Classroom 1", "Classroom 2"};
        } else if (basicsLessonIndex == 1) {
            return new String[]{"Class Items"};
        } else {
            return new String[]{"Misc Phrases"};
        }
    }
    
        private String[] getAdvanced1Groups() {
        if (basicsLessonIndex == 0) {
            return new String[]{"Travel 1", "Travel 2"};
        } else if (basicsLessonIndex == 1) {
            return new String[]{"Food Items", "Restaurant"};
        } else {
            return new String[]{"Misc Phrases"};
        }
    }
    
        private String[] getAdvanced2Groups() {
        if (basicsLessonIndex == 0) {
            return new String[]{"Business 1", "Business 2"};
        } else if (basicsLessonIndex == 1) {
            return new String[]{"Formal Greetings", "Polite Speech"};
        } else {
            return new String[]{"Misc Phrases"};
        }
    }
    
        private String[] getHiraganaGroups() {
        return new String[]{"あーか", "さーた", "なーは", "ま", "やーら", "わーんーを"};
    }
    
        private String[] getKatakanaGroups() {
        return new String[]{"アーカ", "サーター", "ナーハ", "マ", "ヤーラ", "ワーンーヲ"};
    }
}