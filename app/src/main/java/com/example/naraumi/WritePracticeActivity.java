package com.example.naraumi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class WritePracticeActivity extends BaseActivity {

    private String[][] currentGroup;
    private int currentIndex = 0;
    private boolean[] correctAnswers;

    private TextView charDisplay;
    private TextView hiraganaText;
    private TextView progressText;
    private EditText inputText;
    private TextView btnNext;
    private TextView btnPrev;

    private boolean isPhraseMode = false;
    private String kanaType;
    private String kanaGroup;
    private HiraganaLookupHelper hiraganaHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_practice);

        kanaType = getIntent().getStringExtra("KANA_TYPE");
        kanaGroup = getIntent().getStringExtra("KANA_GROUP");
        isPhraseMode = getIntent().getBooleanExtra("PHRASE_MODE", false);

        hiraganaHelper = new HiraganaLookupHelper(this);

        getViews();
        setHeaderText();
        if (!loadCharacterGroup()) {
            showEmptyState();
            return;
        }
        correctAnswers = new boolean[currentGroup.length];
        setupUI();
        currentWord();
    }

    private void getViews() {
        charDisplay = findViewById(R.id.writingWord);
        hiraganaText = findViewById(R.id.writingHiragana);
        progressText = findViewById(R.id.writingProgress);
        inputText = findViewById(R.id.writingInput);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setHeaderText() {
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            if (kanaType != null) {
                if (isPhraseMode && (kanaType.equals("BASICS") || kanaType.equals("BASICS 2") || kanaType.equals("ADVANCED 1") || kanaType.equals("ADVANCED 2"))) {
                    headerTitle.setText("WRITE: " + (kanaGroup != null ? kanaGroup : kanaType));
                } else {
                    headerTitle.setText("WRITE: " + kanaType);
                }
            } else {
                headerTitle.setText("WRITE");
            }
        }
    }

    private boolean loadCharacterGroup() {
        if (kanaType == null || kanaGroup == null) {
            Toast.makeText(this, "group info is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

                currentGroup = KanaContentProvider.getKanaGroup(kanaType, kanaGroup);
        return currentGroup != null && currentGroup.length > 0;
    }

    private void setupUI() {
        btnPrev.setOnClickListener(v -> navigate(-1));
        btnNext.setOnClickListener(v -> checkThenNavigate());
    }

    private void showEmptyState() {
        charDisplay.setText("No words found");
        inputText.setVisibility(View.GONE);
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
    }

    private void currentWord() {
        if (currentGroup == null || currentIndex >= currentGroup.length) {
            showCompletion();
            return;
        }

        String[] currentItem = currentGroup[currentIndex];
        if (currentItem.length < 2) {
            navigate(1);
            return;
        }

        charDisplay.setText(currentItem[0]);
        // fetch hiragana reading if word has any
        String hiragana = hiraganaHelper.getHiraganaReading(currentItem[0]);

        if (!hiragana.isEmpty()) {
            hiraganaText.setText(hiragana);
            hiraganaText.setVisibility(View.VISIBLE);
        } else {
            hiraganaText.setVisibility(View.GONE);
        }
        
        inputText.setText("");
        inputText.setEnabled(true);
        inputText.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_bg));
        updateProgress();
        
                inputText.requestFocus();
        showKeyboard(inputText);
    }

    private void checkThenNavigate() {
        String userInput = inputText.getText().toString().trim();
        String correctAnswer = currentGroup[currentIndex][1]; // Romaji

        if (TextUtils.isEmpty(userInput)) {
            Toast.makeText(this, "Please type your answer", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCorrect = userInput.equalsIgnoreCase(correctAnswer);
        correctAnswers[currentIndex] = isCorrect;

                if (isCorrect) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect. Correct answer: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }

        //moving forward right after
        navigate(1);
    }

    private void navigate(int direction) {
        int newIndex = currentIndex + direction;
        if (newIndex >= 0 && newIndex < currentGroup.length) {
            currentIndex = newIndex;
            currentWord();
        } else if (newIndex >= currentGroup.length) {
            //no more words complete and show completion
            showCompletion();
        }
    }

    private void updateProgress() {
        progressText.setText(String.format("%d/%d", currentIndex + 1, currentGroup.length));
        btnPrev.setEnabled(currentIndex > 0);
    }

    private void showCompletion() {
        LessonProgressManager.markLessonCompleted(this,
                LessonProgressManager.createLessonKey(kanaType, kanaGroup, "WRITE"));

        ActivityRefreshManager.setRefreshNeeded();
        StreakManager.updateStreak(this);
        int score = 0;
        for (boolean b : correctAnswers) {
            if (b) score++;
        }

        Intent intent = new Intent(this, KanaCompletionActivity.class);
        intent.putExtra("SOURCE", "WRITE");
        intent.putExtra("KANA_TYPE", kanaType);
        intent.putExtra("KANA_GROUP", kanaGroup);
        intent.putExtra("LEARNED_COUNT", score);
        intent.putExtra("TOTAL_ITEMS", currentGroup.length);
        intent.putExtra("PHRASE_MODE", isPhraseMode);
        startActivity(intent);
        finish();
    }


    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
