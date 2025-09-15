package com.example.naraumi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.*;

public class MatchTilesActivity extends BaseActivity implements TTSHelper.TTSReadyListener {

        private MatchingGameSession gameSession;
    private TileSelection currentSelection = new TileSelection();
    
        private LinearLayout leftColumn, rightColumn;
    private TTSHelper ttsHelper;
    private HiraganaLookupHelper hiraganaHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_tiles);

        gameSession = new MatchingGameSession(getIntent());
        setupUI();
        
        if (gameSession.hasContent()) {
            setupTTS();
            startMatchingGame();
        } else {
            Toast.makeText(this, "No content available for matching", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsHelper != null) ttsHelper.shutdown();
    }

    
        private void setupUI() {
        leftColumn = findViewById(R.id.kana_column);
        rightColumn = findViewById(R.id.romaji_column);
        hiraganaHelper = new HiraganaLookupHelper(this);
        
        TextView header = findViewById(R.id.header_title);
        header.setText("MATCH: " + gameSession.getDisplayName());
    }

        private void setupTTS() {
        ttsHelper = new TTSHelper(this, this);
    }

        private void startMatchingGame() {
        clearTiles();
        showNextBatch();
    }

    
        private void showNextBatch() {
        List<MatchPair> batch = gameSession.getNextBatch();
        if (batch.isEmpty()) return;
        
                List<String> leftTiles = new ArrayList<>();
        List<String> rightTiles = new ArrayList<>();
        
        for (MatchPair pair : batch) {
            leftTiles.add(pair.japanese);
            rightTiles.add(pair.english);
        }
        
        Collections.shuffle(leftTiles);
        Collections.shuffle(rightTiles);
        
                for (String text : leftTiles) {
            leftColumn.addView(createTile(text, true));
        }
        for (String text : rightTiles) {
            rightColumn.addView(createTile(text, false));
        }
    }

        private LinearLayout createTile(String text, boolean isJapanese) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout tileLayout = (LinearLayout) inflater.inflate(R.layout.match_tile, null);
        TextView mainText = tileLayout.findViewById(R.id.match_tile);
        TextView hintText = tileLayout.findViewById(R.id.match_tile_romaji);

        mainText.setText(text);
        setupTileHint(hintText, text, isJapanese);
        setupTileSize(mainText, hintText);
        setupTileClick(tileLayout, text, isJapanese);
        setupTileMargins(mainText);

        return tileLayout;
    }

        private void setupTileHint(TextView hintText, String text, boolean isJapanese) {
        if (!isJapanese || gameSession.isKanaLearningLesson()) {
                        hintText.setVisibility(View.GONE);
            return;
        }
        
                String reading = hiraganaHelper.getHiraganaReading(text);
        if (!reading.isEmpty()) {
            hintText.setText(reading);
            hintText.setVisibility(View.VISIBLE);
        } else {
            hintText.setVisibility(View.GONE);
        }
    }

        private void setupTileSize(TextView mainText, TextView hintText) {
        if (gameSession.getTotalPairs() > 6) {
            mainText.setTextSize(18);
            hintText.setTextSize(12);
        }
    }

        private void setupTileClick(LinearLayout tileLayout, String text, boolean isJapanese) {
        tileLayout.setOnClickListener(v -> handleTileClick(text, tileLayout, isJapanese));
        
                if (isJapanese) {
            tileLayout.setOnLongClickListener(v -> {
                                ttsHelper.showTTSSetupSteps();
                Toast.makeText(this, "Long press detected - showing TTS setup", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

        private void setupTileMargins(TextView mainText) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 12, 0, 12);
        mainText.setLayoutParams(params);
    }

    
        private void handleTileClick(String text, LinearLayout tile, boolean isJapanese) {
        if (isJapanese) {
            handleJapaneseTileClick(text, tile);
        } else {
            handleEnglishTileClick(text, tile);
        }
        
                if (currentSelection.hasBothSelected()) {
            checkForMatch();
        }
    }

        private void handleJapaneseTileClick(String text, LinearLayout tile) {
        if (currentSelection.isJapaneseTileSelected(tile)) {
            currentSelection.clearJapanese();
            return;
        }
        
        currentSelection.selectJapanese(text, tile);
        updateTileVisualState(tile, true);
        
                if (ttsHelper != null) {
            ttsHelper.speak(text);
        }
    }

        private void handleEnglishTileClick(String text, LinearLayout tile) {
        if (currentSelection.isEnglishTileSelected(tile)) {
            currentSelection.clearEnglish();
            return;
        }
        
        currentSelection.selectEnglish(text, tile);
        updateTileVisualState(tile, true);
    }

        private void updateTileVisualState(LinearLayout tile, boolean selected) {
        int backgroundResource = selected ? 
            R.drawable.match_tile_selected : 
            R.drawable.match_tile_default;
        tile.setBackgroundResource(backgroundResource);
    }

        private void checkForMatch() {
        boolean isCorrectMatch = gameSession.isCorrectMatch(
            currentSelection.japaneseText, 
            currentSelection.englishText
        );
        
        if (isCorrectMatch) {
            handleCorrectMatch();
        } else {
            handleIncorrectMatch();
        }
        
        currentSelection.clearAll();
    }

        private void handleCorrectMatch() {
        Toast.makeText(this, "Match!", Toast.LENGTH_SHORT).show();
        
                removeTileFromColumn(leftColumn, currentSelection.japaneseText);
        removeTileFromColumn(rightColumn, currentSelection.englishText);
        
        gameSession.recordMatch();
        
                if (gameSession.shouldShowNextBatch()) {
            showNextBatch();
        }
        
                if (gameSession.isGameComplete()) {
            showCompletionScreen();
        }
    }

        private void handleIncorrectMatch() {
        Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
        
                if (currentSelection.japaneseTile != null) {
            updateTileVisualState(currentSelection.japaneseTile, false);
        }
        if (currentSelection.englishTile != null) {
            updateTileVisualState(currentSelection.englishTile, false);
        }
    }

    
        private void removeTileFromColumn(LinearLayout column, String text) {
        for (int i = 0; i < column.getChildCount(); i++) {
            LinearLayout tileLayout = (LinearLayout) column.getChildAt(i);
            TextView textView = tileLayout.findViewById(R.id.match_tile);
            if (textView.getText().toString().equals(text)) {
                column.removeView(tileLayout);
                break;
            }
        }
    }

        private void clearTiles() {
        leftColumn.removeAllViews();
        rightColumn.removeAllViews();
    }

        private void showCompletionScreen() {
        Intent intent = new Intent(this, KanaCompletionActivity.class);
        intent.putExtra("LEARNED_COUNT", gameSession.getMatchCount());
        intent.putExtra("TOTAL_ITEMS", gameSession.getTotalPairs());
        intent.putExtra("KANA_TYPE", gameSession.kanaType);
        intent.putExtra("KANA_GROUP", gameSession.kanaGroup);
        intent.putExtra("PHRASE_MODE", gameSession.isPhraseMode);
        intent.putExtra("SOURCE", "MATCH");
        
        String collectionName = getIntent().getStringExtra("COLLECTION_NAME");
        if (collectionName != null) {
            intent.putExtra("COLLECTION_NAME", collectionName);
        }

        startActivity(intent);
        finish();
    }

    
    @Override
    public void onTTSReady(boolean japaneseAvailable) {
        if (!japaneseAvailable) {
            android.util.Log.w("MatchTilesActivity", "Japanese TTS not available");
        }
    }

    @Override
    public void onTTSError() {
        Toast.makeText(this, "Audio features unavailable", Toast.LENGTH_SHORT).show();
    }

    
        private static class MatchingGameSession {
        final String kanaType, kanaGroup;
        final boolean isPhraseMode;
        private final List<MatchPair> allPairs;
        private final Queue<MatchPair> remainingPairs;
        private int matchesInCurrentBatch = 0;
        private int totalMatches = 0;
        
        private static final int TILES_PER_BATCH = 6;
        private static final int MATCHES_BEFORE_NEW_BATCH = 3;

        MatchingGameSession(Intent intent) {
            this.kanaType = intent.getStringExtra("KANA_TYPE");
            this.kanaGroup = intent.getStringExtra("KANA_GROUP");
            this.isPhraseMode = intent.getBooleanExtra("PHRASE_MODE", false);
            this.allPairs = loadContentPairs();
            this.remainingPairs = new LinkedList<>(allPairs);
            Collections.shuffle(allPairs);
        }

        boolean hasContent() { return !allPairs.isEmpty(); }
        int getTotalPairs() { return allPairs.size(); }
        int getMatchCount() { return totalMatches; }
        boolean isGameComplete() { return totalMatches == allPairs.size(); }
        
        String getDisplayName() {
            if (kanaType != null) {
                if (isPhraseMode && (kanaType.startsWith("BASICS") || kanaType.startsWith("ADVANCED"))) {
                    return kanaGroup != null ? kanaGroup : kanaType;
                }
                return kanaType;
            }
            return "MATCHING";
        }

        boolean isKanaLearningLesson() {
            return kanaType != null && (kanaType.contains("HIRAGANA") || kanaType.contains("KATAKANA"));
        }

        List<MatchPair> getNextBatch() {
            List<MatchPair> batch = new ArrayList<>();
            for (int i = 0; i < TILES_PER_BATCH && !remainingPairs.isEmpty(); i++) {
                batch.add(remainingPairs.poll());
            }
            return batch;
        }

        boolean isCorrectMatch(String japanese, String english) {
            return allPairs.stream()
                .anyMatch(pair -> pair.japanese.equals(japanese) && pair.english.equals(english));
        }

        void recordMatch() {
            totalMatches++;
            matchesInCurrentBatch++;
        }

        boolean shouldShowNextBatch() {
            if (matchesInCurrentBatch >= MATCHES_BEFORE_NEW_BATCH) {
                matchesInCurrentBatch = 0;
                return !remainingPairs.isEmpty();
            }
            return false;
        }

                private List<MatchPair> loadContentPairs() {
            String[][] content = KanaContentProvider.getKanaGroup(kanaType, kanaGroup);
            List<MatchPair> pairs = new ArrayList<>();
            
            if (content != null && content.length > 0) {
                for (String[] row : content) {
                    if (row.length >= 2) {
                        pairs.add(new MatchPair(row[0], row[1]));
                    }
                }
            } else {
                                pairs.add(new MatchPair("こんにちは", "Hello"));
                pairs.add(new MatchPair("ありがとう", "Thank you"));
                pairs.add(new MatchPair("さようなら", "Goodbye"));
                pairs.add(new MatchPair("おはよう", "Good morning"));
            }
            
            return pairs;
        }
    }

        private static class MatchPair {
        final String japanese, english;
        
        MatchPair(String japanese, String english) {
            this.japanese = japanese;
            this.english = english;
        }
    }

        private class TileSelection {
        String japaneseText, englishText;
        LinearLayout japaneseTile, englishTile;

        boolean hasBothSelected() {
            return japaneseText != null && englishText != null;
        }

        boolean isJapaneseTileSelected(LinearLayout tile) {
            return japaneseTile == tile;
        }

        boolean isEnglishTileSelected(LinearLayout tile) {
            return englishTile == tile;
        }

        void selectJapanese(String text, LinearLayout tile) {
            clearJapanese();
            japaneseText = text;
            japaneseTile = tile;
        }

        void selectEnglish(String text, LinearLayout tile) {
            clearEnglish();
            englishText = text;
            englishTile = tile;
        }

        void clearJapanese() {
            if (japaneseTile != null) {
                updateTileVisualState(japaneseTile, false);
            }
            japaneseText = null;
            japaneseTile = null;
        }

        void clearEnglish() {
            if (englishTile != null) {
                updateTileVisualState(englishTile, false);
            }
            englishText = null;
            englishTile = null;
        }

        void clearAll() {
            clearJapanese();
            clearEnglish();
        }
    }
}