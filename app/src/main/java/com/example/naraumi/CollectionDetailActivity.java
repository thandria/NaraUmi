package com.example.naraumi;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.*;
import androidx.core.content.ContextCompat;

import java.util.Set;
import java.util.HashSet;


 // "kanji|kana|meaning|rawDefinition"

public class CollectionDetailActivity extends BaseActivity {

    private String collectionName;
    private LinearLayout wordListContainer;
    private DatabaseHelper.DictionarySearchHelper searchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        collectionName = getIntent().getStringExtra("collection_name");
        setupUI();
        setupSearchHelper();
        refreshWordList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWordList();
    }


    private void setupUI() {
        TextView header = findViewById(R.id.header_title);
        header.setText(collectionName != null ? collectionName : "Collection");

        wordListContainer = findViewById(R.id.collection_words_container);
        
        Button addWordBtn = findViewById(R.id.btn_add_word);
        addWordBtn.setOnClickListener(v -> showAddWordDialog());

        Button reviewBtn = findViewById(R.id.btn_review_collection);
        reviewBtn.setOnClickListener(v -> startFlashcardReview());
    }


    private void setupSearchHelper() {
        searchHelper = new DatabaseHelper.DictionarySearchHelper(this);
    }

    private void refreshWordList() {
        displayWordList();
        updateStatistics();
    }


    private void displayWordList() {
        wordListContainer.removeAllViews();
        Set<String> words = CollectionManager.getWordsInCollection(this, collectionName);
        SharedPreferences prefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);

        for (String word : words) {
            int learningScore = prefs.getInt("retention_" + word, 0);
            Button wordButton = createWordButton(word, learningScore);
            wordListContainer.addView(wordButton);
        }
    }


    private Button createWordButton(String wordData, int learningScore) {
        WordInfo word = new WordInfo(wordData);
        
        Button button = new Button(this);
        button.setText(word.getDisplayText() + "  ⭐" + learningScore);
        button.setAllCaps(false);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        button.setBackgroundResource(R.drawable.rounded_square_purple);
        button.setPadding(16, 16, 16, 16);
        button.setOnClickListener(v -> showRemoveWordDialog(wordData));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        button.setLayoutParams(params);

        return button;
    }

    private void showAddWordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Word");

        EditText input = new EditText(this);
        input.setHint("Enter English, Kana, or Kanji");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String queryText = input.getText().toString().trim();
            if (!queryText.isEmpty()) {
                searchAndAddWord(queryText);
            } else {
                Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void searchAndAddWord(String queryText) {
        if (queryText.trim().isEmpty()) {
            android.widget.Toast.makeText(this, "Please enter a word to search", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper.DictionarySearchHelper.SearchResult result = searchHelper.searchWord(queryText);
        
        if (result.isFound()) {

            SharedPreferences prefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);
            Set<String> savedWords = new HashSet<>(prefs.getStringSet("collection_" + collectionName, new HashSet<>()));
            
            savedWords.add(result.getWordData());
            prefs.edit().putStringSet("collection_" + collectionName, savedWords).apply();
            
            refreshWordList();
            android.widget.Toast.makeText(this, "Added: " + result.getKanji() + " (" + result.getMeaning() + ")", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            android.widget.Toast.makeText(this, "Word not found in dictionary", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showRemoveWordDialog(String wordData) {
        new AlertDialog.Builder(this)
            .setTitle("Remove Word")
            .setMessage("Are you sure you want to remove this word from this collection?")
            .setPositiveButton("Yes", (dialog, which) -> {
                CollectionManager.removeWordFromCollection(this, collectionName, wordData);
                Toast.makeText(this, "Word has been removed", Toast.LENGTH_SHORT).show();
                refreshWordList();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void startFlashcardReview() {
        Intent intent = new Intent(this, FlashcardReviewActivity.class);
        intent.putExtra("collection_name", collectionName);
        startActivity(intent);
    }


    private void updateStatistics() {
        Set<String> words = CollectionManager.getWordsInCollection(this, collectionName);
        if (words == null || words.isEmpty()) {
            showEmptyCollectionStats();
            return;
        }

        LearningStats stats = calculateLearningStats(words);
        displayLearningStats(stats);
    }


    private void showEmptyCollectionStats() {
        findViewById(R.id.retention_text).setVisibility(android.view.View.GONE);
        TextView statsView = findViewById(R.id.collection_stats);
        statsView.setText("You've learned 0 out of 0 words");
    }

    private LearningStats calculateLearningStats(Set<String> words) {
        SharedPreferences prefs = getSharedPreferences("JapLearnPrefs", MODE_PRIVATE);
        float totalRetention = 0;
        int learnedWords = 0;
        int totalWords = words.size();

        for (String word : words) {
            int score = prefs.getInt("retention_" + word, 0);
            totalRetention += score;
            if (score > 0) learnedWords++;
        }

        float avgRetention = totalWords > 0 ? totalRetention / totalWords : 0;
        return new LearningStats(learnedWords, totalWords, avgRetention);
    }


    private void displayLearningStats(LearningStats stats) {
        TextView retentionView = findViewById(R.id.retention_text);
        retentionView.setText("Avg Retention: " + String.format("%.1f", stats.avgRetention));
        retentionView.setVisibility(android.view.View.VISIBLE);

        TextView statsView = findViewById(R.id.collection_stats);
        statsView.setText("You've learned " + stats.learnedWords + " out of " + stats.totalWords + " words");
    }


    private static class WordInfo {
        final String kanji, kana, meaning;

        WordInfo(String wordData) {
            String[] parts = wordData.split("\\|");
            this.kanji = parts.length > 0 ? parts[0].trim() : "";
            this.kana = parts.length > 1 ? parts[1].trim() : "";
            this.meaning = parts.length > 2 ? parts[2].trim() : "";
        }

        String getDisplayText() {
            if (kanji.isEmpty() || kanji.equals(kana)) {
                return kana.isEmpty() ? meaning : kana;
            }
            return kanji + " / " + kana;
        }
    }

    private static class LearningStats {
        final int learnedWords, totalWords;
        final float avgRetention;

        LearningStats(int learned, int total, float avgRetention) {
            this.learnedWords = learned;
            this.totalWords = total;
            this.avgRetention = avgRetention;
        }
    }
}