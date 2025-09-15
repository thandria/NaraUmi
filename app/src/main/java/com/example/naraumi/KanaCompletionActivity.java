package com.example.naraumi;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class KanaCompletionActivity extends BaseActivity {

    // what the user just completed
    private LessonData lessonData;
    private String completionSource;
    private int userScore;
    private int totalItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kana_complete);

        extractIntentData();
        setupCompletionDisplay();
        setupActivityButtons();
    }


        private void extractIntentData() {
        lessonData = new LessonData(getIntent());
        completionSource = getIntent().getStringExtra("SOURCE");
        userScore = getIntent().getIntExtra("LEARNED_COUNT", 0);
        totalItems = getIntent().getIntExtra("TOTAL_ITEMS", 0);

        if ("READ_POINT".equals(completionSource)) {
            totalItems = getIntent().getIntExtra("WRITE_TOTAL", 0);
        }
    }

        private void setupCompletionDisplay() {
        setupHeader();
        setupCompletionIcon();
        setupCompletionMessages();
        animateScoreAndUpdateStreak();
    }

        private void setupActivityButtons() {
        findViewById(R.id.btn_match).setOnClickListener(v -> launchActivity(MatchTilesActivity.class));
        findViewById(R.id.btn_flash).setOnClickListener(v -> launchFlashcards());
        findViewById(R.id.btn_write).setOnClickListener(v -> launchActivity(WritePracticeActivity.class));
        findViewById(R.id.btn_read_point).setOnClickListener(v -> launchReadAndPoint());
        findViewById(R.id.btn_back).setOnClickListener(v -> goBack());
        
        setupReadPointButtonVisibility();
        setupBackButtonText();
    }


    private void setupHeader() {
        TextView header = findViewById(R.id.header_title);
        String title = lessonData.hasGroup() ? "LEARNED: " + lessonData.group : "SESSION COMPLETE";
        header.setText(title);
    }

    private void setupCompletionIcon() {
        ImageView icon = findViewById(R.id.completion_icon);
        int iconResource;
        
        if (completionSource == null) {
            iconResource = R.drawable.review_learned_vocab;
        } else {
            switch (completionSource) {
                case "MATCH":
                    iconResource = R.drawable.review_match;
                    break;
                case "WRITE":
                    iconResource = R.drawable.review_write;
                    break;
                case "READ_POINT":
                    iconResource = R.drawable.review_learned_vocab;
                    break;
                default:
                    iconResource = R.drawable.review_learned_vocab;
                    break;
            }
        }
        icon.setImageResource(iconResource);
    }

    private void setupCompletionMessages() {
        TextView topMsg = findViewById(R.id.completion_message1);
        TextView bottomMsg = findViewById(R.id.completion_learned_words);
        
        if (completionSource == null) {
            topMsg.setText("You have learned");
            bottomMsg.setText(lessonData.isPhraseMode ? "New Phrases!" : "New Kana!");
        } else {
            switch (completionSource) {
                case "MATCH":
                    topMsg.setText("You matched");
                    bottomMsg.setText("pairs out of " + totalItems + "!");
                    break;
                case "WRITE":
                    topMsg.setText("You were correct");
                    bottomMsg.setText("out of " + totalItems + " times!");
                    break;
                case "FLASH":
                    topMsg.setText("You marked");
                    bottomMsg.setText("as learned out of " + totalItems + " cards!");
                    break;
                case "READ_POINT":
                    topMsg.setText("You found");
                    bottomMsg.setText("objects out of " + totalItems + "!");
                    break;
                default:
                    topMsg.setText("You have learned");
                    bottomMsg.setText(lessonData.isPhraseMode ? "New Phrases!" : "New Kana!");
                    break;
            }
        }
    }

    private void animateScoreAndUpdateStreak() {
        TextView scoreView = findViewById(R.id.completion_amount);
        
                ValueAnimator animator = ValueAnimator.ofInt(1, userScore);
        animator.setDuration(2000);
        animator.addUpdateListener(animation -> 
            scoreView.setText(String.valueOf((int) animation.getAnimatedValue())));
        animator.start();
        
                if (userScore > 0) {
            StreakManager.updateStreak(this);
        }

        TextView readPointButton = findViewById(R.id.btn_read_point);
        readPointButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }


    private void setupReadPointButtonVisibility() {
        findViewById(R.id.btn_read_point).setVisibility(
            canUseReadAndPoint() ? View.VISIBLE : View.GONE
        );
    }

    private void setupBackButtonText() {
        TextView backBtn = findViewById(R.id.btn_back);
        if ("FLASH".equals(completionSource) && lessonData.isCollectionMode()) {
            backBtn.setText("Back to Collection");
        }
    }


        private void launchActivity(Class<?> activityClass) {
        if (lessonData.isLessonMode()) {
            Intent intent = new Intent(this, activityClass);
            lessonData.addToIntent(intent);
            startActivity(intent);
        } else if (lessonData.isCollectionMode()) {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("COLLECTION_NAME", lessonData.collection);
            startActivity(intent);
        } else {
            showNoContextError(activityClass.getSimpleName());
        }
        finish();
    }

        private void launchFlashcards() {
        if (lessonData.isLessonMode()) {
            launchFlashcardsFromLesson();
        } else if (lessonData.isCollectionMode()) {
            launchFlashcardsFromCollection();
        } else {
            showNoContextError("Flashcards");
        }
        finish();
    }

    private void launchFlashcardsFromLesson() {
        String[][] rawData = KanaContentProvider.getKanaGroup(lessonData.type, lessonData.group);
        if (rawData == null) {
            Toast.makeText(this, "Failed to load group data", Toast.LENGTH_SHORT).show();
            return;
        }

                ArrayList<String> flashcardData = new ArrayList<>();
        for (String[] entry : rawData) {
            String japanese = entry[0].trim();
            String meaning = entry[1].trim();
            String romaji = (entry.length > 2) ? entry[2].trim() : "";
            flashcardData.add(japanese + "|" + meaning + "|" + romaji);
        }

        Intent intent = new Intent(this, FlashcardReviewActivity.class);
        intent.putStringArrayListExtra("collection_words", flashcardData);
        intent.putExtra("collection_name", lessonData.group);
        intent.putExtra("IS_LESSON_DATA", true);
        lessonData.addToIntent(intent);
        startActivity(intent);
    }

    private void launchFlashcardsFromCollection() {
        Intent intent = new Intent(this, FlashcardReviewActivity.class);
        intent.putExtra("collection_name", lessonData.collection);
        startActivity(intent);
    }

        private void launchReadAndPoint() {
        if (lessonData.isLessonMode()) {
            Intent intent = new Intent(this, ReadAndPointActivity.class);
            intent.putExtra("LESSON_TYPE", lessonData.type);
            intent.putExtra("BASICS_CIRCLE_INDEX", lessonData.basicsIndex);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Read & Point not available for collections", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void goBack() {
        if ("FLASH".equals(completionSource) && lessonData.isCollectionMode()) {
            finish(); //back to collection
        } else {
            //back to lesson selection
            Intent intent = new Intent(this, LessonActivity.class);
            lessonData.addToIntent(intent);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }


        private boolean canUseReadAndPoint() {
        // not available for collections
        if (lessonData.isCollectionMode()) return false;
        
                if ("HIRAGANA".equals(lessonData.type) || "KATAKANA".equals(lessonData.type)) return false;
        
                return lessonData.type.startsWith("BASICS") || lessonData.type.startsWith("ADVANCED");
    }

    private void showNoContextError(String activityName) {
        Toast.makeText(this, "No group selected for " + activityName + ".", Toast.LENGTH_SHORT).show();
    }


        private static class LessonData {
        final String type, group, collection;
        final boolean isPhraseMode;
        final int basicsIndex;

        LessonData(Intent intent) {
            this.type = intent.getStringExtra("KANA_TYPE");
            this.group = intent.getStringExtra("KANA_GROUP");
            this.collection = intent.getStringExtra("COLLECTION_NAME");
            this.isPhraseMode = intent.getBooleanExtra("PHRASE_MODE", false);
            this.basicsIndex = intent.getIntExtra("BASICS_CIRCLE_INDEX", 0);
        }

        boolean isLessonMode() { return type != null && !type.isEmpty(); }
        boolean isCollectionMode() { return collection != null && !collection.isEmpty(); }
        boolean hasGroup() { return group != null && !group.isEmpty(); }

        void addToIntent(Intent intent) {
            intent.putExtra("KANA_TYPE", type);
            intent.putExtra("KANA_GROUP", group);
            intent.putExtra("PHRASE_MODE", isPhraseMode);
            intent.putExtra("BASICS_CIRCLE_INDEX", basicsIndex);
        }
    }
}