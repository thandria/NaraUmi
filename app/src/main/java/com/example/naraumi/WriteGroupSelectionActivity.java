package com.example.naraumi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WriteGroupSelectionActivity extends BaseActivity {


        private static final int LAYOUT_RESOURCE = R.layout.activity_match_group_selection;
    
        private static final String ACTIVITY_TITLE_PREFIX = "WRITE: ";


        private String lessonType;
    
        private boolean isPhraseMode;
    
        private int basicsLessonIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_RESOURCE);

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
            TextView groupButton = createGroupButton(groupName);
            groupContainer.addView(groupButton);
        }
    }


        private TextView createGroupButton(String groupName) {
        TextView groupButton = AppButtonStyle.createButton(this, groupName);
        
        groupButton.setOnClickListener(v -> launchWritePractice(groupName));
        
        return groupButton;
    }
    
        private void launchWritePractice(String selectedGroup) {
        Intent practiceIntent = new Intent(this, WritePracticeActivity.class);
        practiceIntent.putExtra("KANA_TYPE", lessonType);
        practiceIntent.putExtra("KANA_GROUP", selectedGroup);
        practiceIntent.putExtra("PHRASE_MODE", isPhraseMode);
        startActivity(practiceIntent);
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