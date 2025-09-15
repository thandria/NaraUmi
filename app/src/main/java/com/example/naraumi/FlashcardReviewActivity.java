package com.example.naraumi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FlashcardReviewActivity extends BaseActivity {

    // session state
    private List<String> flashcards;
    private int currentCardIndex = 0;
    private int learnedCount = 0;
    private CardDisplayState displayState = new CardDisplayState();
    
    // Ui
    private TextView frontText, backText, hiraganaText, romajiText;
    private TextView revealBtn, learnedBtn, unknownBtn, prevBtn, hiraganabtn;
    
    // preferences
    private String collectionName;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_review);

        initialiseComponents();
        loadFlashcardData();
        setupClickHandlers();
        displayCurrentCard();
    }


        private void initialiseComponents() {
        prefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);
        
        // text displays
        frontText = findViewById(R.id.flashcard_front);
        backText = findViewById(R.id.flashcard_back);
        hiraganaText = findViewById(R.id.flashcard_hiragana);
        romajiText = findViewById(R.id.flashcard_romaji);
        
        // buttons
        revealBtn = findViewById(R.id.btn_reveal);
        learnedBtn = findViewById(R.id.btn_mark_learned);
        unknownBtn = findViewById(R.id.btn_dont_know);
        prevBtn = findViewById(R.id.btn_prev);
        hiraganabtn = findViewById(R.id.btn_toggle_romaji);
        
        setupHeader();
    }

        private void setupHeader() {
        TextView header = findViewById(R.id.header_title);
        String kanaType = getIntent().getStringExtra("KANA_TYPE");
        boolean isPhraseMode = getIntent().getBooleanExtra("PHRASE_MODE", false);
        
        if (kanaType != null) {
            if (isPhraseMode && (kanaType.startsWith("BASICS") || kanaType.startsWith("ADVANCED"))) {
                String kanaGroup = getIntent().getStringExtra("KANA_GROUP");
                header.setText("FLASHCARDS: " + (kanaGroup != null ? kanaGroup : kanaType));
            } else {
                header.setText("FLASHCARDS: " + kanaType);
            }
        } else if (collectionName != null) {
            header.setText("FLASHCARDS: " + collectionName);
        } else {
            header.setText("FLASHCARDS");
        }
    }

        private void loadFlashcardData() {
        collectionName = getIntent().getStringExtra("collection_name");
        
                ArrayList<String> passedWords = getIntent().getStringArrayListExtra("collection_words");
        if (passedWords != null) {
            flashcards = passedWords;
        } 
                else if (collectionName != null) {
            Set<String> wordSet = CollectionManager.getWordsInCollection(this, collectionName);
            flashcards = new ArrayList<>(wordSet);
        }
        else {
            flashcards = new ArrayList<>();
        }

        Collections.shuffle(flashcards);
    }

        private void setupClickHandlers() {
        revealBtn.setOnClickListener(v -> toggleReveal());
        hiraganabtn.setOnClickListener(v -> toggleRomaji());
        learnedBtn.setOnClickListener(v -> markCurrentCardAndAdvance(true));
        unknownBtn.setOnClickListener(v -> markCurrentCardAndAdvance(false));
        prevBtn.setOnClickListener(v -> goToPreviousCard());
    }


        private void displayCurrentCard() {
        if (currentCardIndex >= flashcards.size()) {
            goToCompletionScreen();
            return;
        }

        String cardData = flashcards.get(currentCardIndex);
                boolean isFromCollection = !getIntent().getBooleanExtra("IS_LESSON_DATA", false) && (collectionName != null);
        FlashcardInfo card = new FlashcardInfo(cardData, isFromCollection, this);
        
                frontText.setText(card.japanese);
        backText.setText(card.meaning);
        
                updateRevealDisplay();
        
                updateHiraganaDisplay(card);
        updateRomajiDisplay(card);
    }

        private void updateRevealDisplay() {
        backText.setVisibility(displayState.isRevealed ? View.VISIBLE : View.INVISIBLE);
        revealBtn.setText(displayState.isRevealed ? "Hide" : "Reveal");
    }

        private void updateHiraganaDisplay(FlashcardInfo card) {
                if (!card.hiragana.isEmpty() && !card.hiragana.equals(card.japanese)) {
            hiraganaText.setText(card.hiragana);
            hiraganaText.setVisibility(displayState.isRomajiVisible ? View.VISIBLE : View.GONE);
            hiraganaText.setTextColor(android.graphics.Color.parseColor("#C8B5FF"));
            hiraganaText.setTextSize(24f);
        } else {
            hiraganaText.setVisibility(View.GONE);
        }
    }

        private void updateRomajiDisplay(FlashcardInfo card) {
                romajiText.setVisibility(View.GONE);
        
                boolean hasHiragana = !card.hiragana.isEmpty() && !card.hiragana.equals(card.japanese);
        
        if (hasHiragana) {
                        hiraganabtn.setText(displayState.isRomajiVisible ? "Hide Hiragana" : "Show Hiragana");
            hiraganabtn.setEnabled(true);
        } else {
                        hiraganabtn.setText("No Reading");
            hiraganabtn.setEnabled(false);
        }
    }

        private boolean isJapaneseText(String text) {
        return text != null && text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF].*");
    }


        private void toggleReveal() {
        displayState.isRevealed = !displayState.isRevealed;
        updateRevealDisplay();
    }

        private void toggleRomaji() {
        displayState.isRomajiVisible = !displayState.isRomajiVisible;
                boolean isFromCollection = !getIntent().getBooleanExtra("IS_LESSON_DATA", false) && (collectionName != null);
        FlashcardInfo card = new FlashcardInfo(flashcards.get(currentCardIndex), isFromCollection, this);
        
                updateHiraganaDisplay(card);
        updateRomajiDisplay(card);
    }

        private void markCurrentCardAndAdvance(boolean wasLearned) {
        String cardData = flashcards.get(currentCardIndex);
        
        if (wasLearned) {
            updateLearningScore(cardData, 1);
            learnedCount++;
        } else {
            updateLearningScore(cardData, -1);
        }
        
        goToNextCard();
    }

        private void goToNextCard() {
        currentCardIndex++;
        resetDisplayState();
        displayCurrentCard();
    }

        private void goToPreviousCard() {
        if (currentCardIndex > 0) {
            currentCardIndex--;
            resetDisplayState();
            displayCurrentCard();
        }
    }

        private void resetDisplayState() {
        displayState.isRevealed = false;
        displayState.isRomajiVisible = false;
    }


        private void updateLearningScore(String cardData, int delta) {
        String key = "retention_" + cardData;
        int currentScore = prefs.getInt(key, 0);
        int newScore = Math.max(0, currentScore + delta);
        prefs.edit().putInt(key, newScore).apply();
    }

        private void goToCompletionScreen() {
        Intent intent = new Intent(this, KanaCompletionActivity.class);
        intent.putExtra("SOURCE", "FLASH");
        intent.putExtra("LEARNED_COUNT", learnedCount);
        intent.putExtra("TOTAL_ITEMS", flashcards.size());

                boolean isFromCollection = !getIntent().getBooleanExtra("IS_LESSON_DATA", false) && (collectionName != null);
        
        if (isFromCollection) {
                        intent.putExtra("COLLECTION_NAME", collectionName);
        } else {
                        intent.putExtra("PHRASE_MODE", getIntent().getBooleanExtra("PHRASE_MODE", false));
            intent.putExtra("KANA_TYPE", getIntent().getStringExtra("KANA_TYPE"));
            intent.putExtra("KANA_GROUP", getIntent().getStringExtra("KANA_GROUP"));
            intent.putExtra("BASICS_CIRCLE_INDEX", getIntent().getIntExtra("BASICS_CIRCLE_INDEX", 0));
        }

        startActivity(intent);
        finish();
    }


        private static class CardDisplayState {
        boolean isRevealed = false;
        boolean isRomajiVisible = false;
    }

        private static class FlashcardInfo {
        final String japanese, meaning, hiragana, romaji;

        FlashcardInfo(String cardData, boolean isFromCollection, Context context) {
            String[] parts = cardData.split("\\|");
            
                        this.japanese = parts.length > 0 ? parts[0].trim() : "";
            
                        if (isFromCollection && parts.length > 2) {
                this.meaning = formatMeaning(parts[2]);             } else if (!isFromCollection && parts.length > 1) {
                this.meaning = formatMeaning(parts[1]);             } else {
                this.meaning = "Meaning: [Not available]";
            }
            
                        if (isFromCollection && parts.length > 1) {
                                String kana = parts[1].trim();
                this.hiragana = isJapaneseText(kana) ? kana : "";
            } else if (!isFromCollection && isKanjiText(this.japanese)) {
                                this.hiragana = fetchHiraganaFromDatabase(this.japanese, context);
            } else {
                                this.hiragana = "";
            }
            
                        if (!isFromCollection && parts.length > 2) {
                String romaji = parts[2].trim();
                this.romaji = !isJapaneseText(romaji) ? romaji : "";
            } else {
                this.romaji = "";
            }
        }

                private String formatMeaning(String rawMeaning) {
            if (rawMeaning.isEmpty()) return "Meaning: [Not available]";
            
            return rawMeaning.toLowerCase().startsWith("meaning:") ? 
                rawMeaning : "Meaning: " + rawMeaning;
        }

                private boolean isJapaneseText(String text) {
            return text != null && text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF].*");
        }
        
                private boolean isKanjiText(String text) {
            if (text == null || text.isEmpty()) return false;
            return text.matches(".*[\\u4E00-\\u9FAF].*");
        }
        
                private String fetchHiraganaFromDatabase(String kanjiWord, Context context) {
            try {
                DatabaseHelper.DictionarySearchHelper searchHelper = new DatabaseHelper.DictionarySearchHelper(context);
                DatabaseHelper.DictionarySearchHelper.SearchResult result = searchHelper.searchWord(kanjiWord);
                
                if (result.isFound()) {
                    String kana = result.getKana();

                    return (kana != null && isJapaneseText(kana)) ? kana : "";
                }
            } catch (Exception e) {
                android.util.Log.w("FlashcardReviewActivity", "Failed to fetch hiragana for: " + kanjiWord, e);
            }
            
            return "";
        }
    }
}
