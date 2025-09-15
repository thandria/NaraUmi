package com.example.naraumi;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class ResultActivity extends BaseActivity implements TTSHelper.TTSReadyListener {

        private WordInfo currentWord = new WordInfo();
    
        private TextView meaningText;
    private ImageButton playButton, favoriteButton;
    private TTSHelper ttsHelper;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setupUIComponents();
        setupTTSHelper();
        findAndDisplayWord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsHelper != null) ttsHelper.shutdown();
    }

    
        private void setupUIComponents() {
        meaningText = findViewById(R.id.meaning_text);
        playButton = findViewById(R.id.play_audio_button);
        favoriteButton = findViewById(R.id.save_wrd_btn);
        
                playButton.setOnClickListener(v -> playPronunciation());
        playButton.setOnLongClickListener(v -> {
                        ttsHelper.showTTSSetupSteps();
            Toast.makeText(this, "Long press detected - showing TTS setup", Toast.LENGTH_SHORT).show();
            return true;
        });
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.extra_button_2).setOnClickListener(v -> finish());
        
                TextView kanaText = findViewById(R.id.main_kana);
        kanaText.setOnClickListener(this::showKanaPopup);
        
                database = new DatabaseHelper(this);
        try {
            database.createDatabase();
        } catch (Exception e) {
            android.util.Log.e("ResultActivity", "Error initializing database", e);
            Toast.makeText(this, "Error loading dictionary database", Toast.LENGTH_SHORT).show();
        }
    }

        private void setupTTSHelper() {
        ttsHelper = new TTSHelper(this, this);
    }

    
        private void findAndDisplayWord() {
        String[] searchWords = getIntent().getStringArrayExtra("detected_words");
        String savedWordRawDef = getIntent().getStringExtra("saved_word_raw_def");
        String mainMeaning = getIntent().getStringExtra("main_meaning"); 
                String wordKanji = getIntent().getStringExtra("word_kanji");
        String wordKana = getIntent().getStringExtra("word_kana");
        String wordRawDef = getIntent().getStringExtra("word_raw_def");

        if (wordKanji != null && wordKana != null && wordRawDef != null) {
                        currentWord.set(wordKanji, wordKana, wordRawDef);
            updateDisplay();
            return;
        }

        if (searchWords == null || searchWords.length == 0) {
            showMessage("No words detected.");
            return;
        }

                if (savedWordRawDef != null) {
            findSavedWord(savedWordRawDef);
        } else {
            searchForWords(searchWords);
        }
    }

        private void searchForWords(String[] searchWords) {
        DatabaseHelper.DictionarySearchHelper searchHelper = new DatabaseHelper.DictionarySearchHelper(this);
        
        android.util.Log.d("ResultActivity", "Searching for words: " + java.util.Arrays.toString(searchWords));
        
        for (String word : searchWords) {
            if (word == null || word.trim().length() < 1) continue;
            
            String cleanWord = word.trim();
            android.util.Log.d("ResultActivity", "Searching for: '" + cleanWord + "'");
            
            DatabaseHelper.DictionarySearchHelper.SearchResult result = searchHelper.searchWord(cleanWord);
            
            if (result.isFound()) {
                android.util.Log.d("ResultActivity", "Found match: " + result.getKanji() + " / " + result.getKana());
                currentWord.set(result.getKanji(), result.getKana(), result.getRawDefinition());
                updateDisplay();
                return;
            }
        }
        
        android.util.Log.d("ResultActivity", "No matches found for any words");
        showMessage("Word not found in dictionary.");
    }

        private void findSavedWord(String rawDefinition) {
        SQLiteDatabase db = database.openDatabase();
        
        try (Cursor cursor = db.rawQuery("SELECT * FROM Dict WHERE def = ?", new String[]{rawDefinition})) {
            if (cursor.moveToFirst()) {
                displayWordFromCursor(cursor);
            } else {
                                String mainMeaning = getIntent().getStringExtra("main_meaning");
                if (mainMeaning != null) {
                    searchForWords(new String[]{mainMeaning});
                } else {
                    showMessage("Saved word not found in dictionary.");
                }
            }
        }
        
        db.close();
    }

    
        private void displayWordFromCursor(Cursor cursor) {
        String kanji = cursor.getString(cursor.getColumnIndexOrThrow("kanji"));
        String kana = cursor.getString(cursor.getColumnIndexOrThrow("kana"));
        String rawDef = cursor.getString(cursor.getColumnIndexOrThrow("def"));
        
        currentWord.set(kanji, kana, rawDef);
        updateDisplay();
    }

        private void updateDisplay() {
                TextView kanjiText = findViewById(R.id.main_kanji);
        TextView kanaText = findViewById(R.id.main_kana);
        TextView englishTitle = findViewById(R.id.english_title);
        
        kanjiText.setText(currentWord.kanji);
        kanaText.setText(currentWord.kana);
        
                String cleanedDef = currentWord.rawDefinition.replaceAll("\\(.*?\\)", "").trim();
        String[] parts = cleanedDef.split("/");
        
                String mainMeaning = parts.length > 0 ? capitalizeFirstLetter(parts[0].trim()) : "No definition found.";
        englishTitle.setText("Definition: " + mainMeaning);
        
                StringBuilder others = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            String alt = parts[i].trim();
            if (!alt.isEmpty()) {
                others.append("• ").append(capitalizeFirstLetter(alt)).append("\n");
            }
        }
        
        meaningText.setText(others.toString().trim());
        
                updateFavoriteButton();
    }
    
        private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

        private void showMessage(String message) {
        meaningText.setText(message);
    }

    
        private void showKanaPopup(View anchor) {
        if (currentWord.kana.isEmpty()) return;
        
                TextView popup = new TextView(this);
        popup.setText(currentWord.kana.length() > 20 ? 
            currentWord.kana.substring(0, 20) + "..." : currentWord.kana);
        popup.setTextSize(18);
        popup.setPadding(20, 20, 20, 20);
        popup.setBackgroundColor(getColor(android.R.color.white));
        popup.setTextColor(getColor(android.R.color.black));
        
                PopupWindow popupWindow = new PopupWindow(popup, 
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.CENTER);
    }

        private void playPronunciation() {
        String textToSpeak = !currentWord.kanji.isEmpty() ? currentWord.kanji : currentWord.kana;
        
        if (textToSpeak.isEmpty()) {
            Toast.makeText(this, "No text to speak", Toast.LENGTH_SHORT).show();
        } else {
            ttsHelper.speak(textToSpeak);
        }
    }

        private void toggleFavorite() {
        SharedPreferences prefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);
        Set<String> savedWords = new HashSet<>(prefs.getStringSet("saved_words", new HashSet<>()));
        
                String cleanedDef = currentWord.rawDefinition.replaceAll("\\(.*?\\)", "").trim();
        String[] parts = cleanedDef.split("/");
        String mainDefinition = parts.length > 0 ? capitalizeFirstLetter(parts[0].trim()) : "No definition found.";
        
                String saveEntry = currentWord.kanji + "|" + currentWord.kana + "|" + mainDefinition + "|" + currentWord.rawDefinition;
        boolean wasSaved = savedWords.contains(saveEntry);
        
        if (wasSaved) {
            savedWords.remove(saveEntry);
            favoriteButton.setImageResource(R.drawable.card_add_fav_unchecked);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            savedWords.add(saveEntry);
            favoriteButton.setImageResource(R.drawable.card_add_fav_checked);
            Toast.makeText(this, "Saved to favorites ❤️", Toast.LENGTH_SHORT).show();
        }
        
        prefs.edit().putStringSet("saved_words", savedWords).apply();
    }

        private void updateFavoriteButton() {
        SharedPreferences prefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);
        Set<String> savedWords = prefs.getStringSet("saved_words", new HashSet<>());
        
                String cleanedDef = currentWord.rawDefinition.replaceAll("\\(.*?\\)", "").trim();
        String[] parts = cleanedDef.split("/");
        String mainDefinition = parts.length > 0 ? capitalizeFirstLetter(parts[0].trim()) : "No definition found.";
        
        String saveEntry = currentWord.kanji + "|" + currentWord.kana + "|" + mainDefinition + "|" + currentWord.rawDefinition;
        boolean isSaved = savedWords.contains(saveEntry);
        
        favoriteButton.setImageResource(isSaved ? 
            R.drawable.card_add_fav_checked : R.drawable.card_add_fav_unchecked);
    }

    
    @Override
    public void onTTSReady(boolean japaneseAvailable) {
        if (!japaneseAvailable) {
            android.util.Log.w("ResultActivity", "Japanese TTS not available");
        }
    }

    @Override
    public void onTTSError() {
        Toast.makeText(this, "Text-to-speech unavailable", Toast.LENGTH_SHORT).show();
    }

    
        private static class WordInfo {
        String kanji = "";
        String kana = "";
        String rawDefinition = "";

        void set(String k, String kn, String def) {
            kanji = k != null ? k : "";
            kana = kn != null ? kn : "";
            rawDefinition = def != null ? def : "";
        }
    }
}