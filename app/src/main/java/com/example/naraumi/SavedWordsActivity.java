package com.example.naraumi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class SavedWordsActivity extends BaseActivity {

    private static final String APP_PREFERENCES = "JapLearnPrefs";
    private static final String SAVED_WORDS_KEY = "saved_words";
    private static final String WORD_COMPONENT_DELIMITER = "\\|";
    private static final String DISPLAY_DELIMITER = " | ";
    private static final int MIN_WORD_COMPONENTS = 1;
    
    private LinearLayout savedWordsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_words);

        initialiseUI();
        loadAndDisplaySavedWords();
    }
    
        private void initialiseUI() {
        TextView headerTitle = findViewById(R.id.header_title);
        headerTitle.setText("Saved Words");
        
        savedWordsContainer = findViewById(R.id.saved_words_container);
    }
    
        private void loadAndDisplaySavedWords() {
        Set<String> savedWords = getSavedWordsFromPreferences();
        
        if (savedWords.isEmpty()) {
            displayEmptyState();
        } else {
            displaySavedWords(savedWords);
        }
    }
    
        private Set<String> getSavedWordsFromPreferences() {
        SharedPreferences appPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        return appPreferences.getStringSet(SAVED_WORDS_KEY, new HashSet<>());
    }

        private void displayEmptyState() {
        TextView emptyStateMessage = createEmptyStateView();
        savedWordsContainer.addView(emptyStateMessage);
    }
    
        private TextView createEmptyStateView() {
        TextView emptyStateView = new TextView(this);
        emptyStateView.setText("No saved words yet.\n\nSave words while studying to see them here!");
        emptyStateView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        emptyStateView.setGravity(Gravity.CENTER);
        emptyStateView.setTextSize(18);
        emptyStateView.setPadding(32, 64, 32, 64);
        
        return emptyStateView;
    }
    
        private void displaySavedWords(Set<String> savedWords) {
        for (String wordEntry : savedWords) {
            SavedWordData wordData = parseWordEntry(wordEntry);
            
            if (wordData.isValid()) {
                TextView wordItemView = createWordItemView(wordData);
                savedWordsContainer.addView(wordItemView);
            }
        }
    }
    
        private TextView createWordItemView(SavedWordData wordData) {
        TextView wordItemView = new TextView(this);
        
        configureWordItemText(wordItemView, wordData);
        configureWordItemStyling(wordItemView);
        configureWordItemLayout(wordItemView);
        
        wordItemView.setOnClickListener(v -> openWordDetailView(wordData));
        
        return wordItemView;
    }
    
        private void configureWordItemText(TextView wordItemView, SavedWordData wordData) {
        String displayText = wordData.getKanji() + DISPLAY_DELIMITER + wordData.getKana();
        wordItemView.setText(displayText);
        wordItemView.setTextSize(20);
    }
    
        private void configureWordItemStyling(TextView wordItemView) {
        wordItemView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        wordItemView.setPadding(32, 24, 32, 24);
        wordItemView.setBackgroundResource(R.drawable.rounded_square_purple);
    }
    
        private void configureWordItemLayout(TextView wordItemView) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 24);
        wordItemView.setLayoutParams(layoutParams);
    }

        private void openWordDetailView(SavedWordData wordData) {
        Intent detailIntent = new Intent(this, ResultActivity.class);
        
        detailIntent.putExtra("word_kanji", wordData.getKanji());
        detailIntent.putExtra("word_kana", wordData.getKana());
        detailIntent.putExtra("word_raw_def", wordData.getRawDefinition());

        startActivity(detailIntent);
    }

        private SavedWordData parseWordEntry(String wordEntry) {
        if (wordEntry == null || wordEntry.isEmpty()) {
            return new SavedWordData();
        }
        
        String[] wordComponents = wordEntry.split(WORD_COMPONENT_DELIMITER);
        
        if (wordComponents.length < MIN_WORD_COMPONENTS) {
            return new SavedWordData();
        }
        
        return new SavedWordData(
            getComponentSafely(wordComponents, 0),              getComponentSafely(wordComponents, 1),              getComponentSafely(wordComponents, 2),              getComponentSafely(wordComponents, 3)           );
    }
    
        private String getComponentSafely(String[] components, int index) {
        if (components != null && index >= 0 && index < components.length) {
            return components[index];
        }
        return "";
    }
    
        private static class SavedWordData {
        private final String kanji;
        private final String kana;
        private final String definition;
        private final String rawDefinition;
        
        public SavedWordData() {
            this(null, null, null, null);
        }
        
        public SavedWordData(String kanji, String kana, String definition, String rawDefinition) {
            this.kanji = kanji != null ? kanji : "";
            this.kana = kana != null ? kana : "";
            this.definition = definition != null ? definition : "";
            this.rawDefinition = rawDefinition != null ? rawDefinition : "";
        }
        
        public String getKanji() { return kanji; }
        public String getKana() { return kana; }
        public String getDefinition() { return definition; }
        public String getRawDefinition() { return rawDefinition; }
        
                public boolean isValid() {
            return kanji != null && !kanji.isEmpty();
        }
    }
}