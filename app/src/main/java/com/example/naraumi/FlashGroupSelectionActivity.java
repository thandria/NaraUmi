package com.example.naraumi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class FlashGroupSelectionActivity extends BaseActivity {


        private static final int LAYOUT_RESOURCE = R.layout.activity_match_group_selection;
    
        private static final int GROUP_BUTTON_BACKGROUND = R.drawable.rounded_header_bg;
    
        private static final int BUTTON_PADDING = 24;
    
        private static final int BUTTON_MARGIN = 24;
    
        private static final String ACTIVITY_TITLE_PREFIX = "FLASHCARDS: ";
    
        private static final String FLASHCARD_DATA_SEPARATOR = "|";


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
        button.setOnClickListener(v -> launchFlashcardReview(groupName));
    }
    
        private void launchFlashcardReview(String selectedGroup) {
                ArrayList<String> flashcardData = convertLessonDataToFlashcardFormat(selectedGroup);
        
        Intent flashcardIntent = new Intent(this, FlashcardReviewActivity.class);
        flashcardIntent.putStringArrayListExtra("collection_words", flashcardData);
        flashcardIntent.putExtra("collection_name", selectedGroup);
        flashcardIntent.putExtra("IS_LESSON_DATA", true);
        
                flashcardIntent.putExtra("KANA_TYPE", lessonType);
        flashcardIntent.putExtra("KANA_GROUP", selectedGroup);
        flashcardIntent.putExtra("PHRASE_MODE", isPhraseMode);
        flashcardIntent.putExtra("BASICS_CIRCLE_INDEX", basicsLessonIndex);
        
        startActivity(flashcardIntent);
    }


        private ArrayList<String> convertLessonDataToFlashcardFormat(String groupName) {
        String[][] rawLessonData = KanaContentProvider.getKanaGroup(lessonType, groupName);
        ArrayList<String> formattedFlashcardData = new ArrayList<>();
        
        for (String[] dataEntry : rawLessonData) {
            String formattedEntry = formatDataEntryForFlashcard(dataEntry);
            formattedFlashcardData.add(formattedEntry);
        }
        
        return formattedFlashcardData;
    }
    
        private String formatDataEntryForFlashcard(String[] dataEntry) {
        String japaneseText = dataEntry[0].trim();
        String englishMeaning = dataEntry[1].trim();
        String romajiReading = (dataEntry.length > 2) ? dataEntry[2].trim() : "";
        
        return japaneseText + FLASHCARD_DATA_SEPARATOR + 
               englishMeaning + FLASHCARD_DATA_SEPARATOR + 
               romajiReading;
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