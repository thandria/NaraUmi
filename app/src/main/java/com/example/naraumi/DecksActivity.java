package com.example.naraumi;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DecksActivity extends BaseActivity {


        private static final String APP_PREFERENCES = "JapLearnPrefs";
    
        private static final String SAVED_WORDS_KEY = "saved_words";
    
        private static final String TUTORIAL_EXTRA_KEY = "start_decks_tutorial";


        private TextView savedWordsButton;
    
        private TextView collectionsButton;
    
        private TextView lookupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decks);

        initialiseUIComponents();
        setupClickHandlers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        updateButtonCounters();
        handleTutorialIfNeeded();
    }


        private void initialiseUIComponents() {
                TextView headerTitle = findViewById(R.id.header_title);
        headerTitle.setText("Decks");

        //references to main buttons
        savedWordsButton = findViewById(R.id.saved_words_btn);
        collectionsButton = findViewById(R.id.collections_btn);
        lookupButton = findViewById(R.id.lookup_btn);
    }
    
        private void setupClickHandlers() {
        setupSavedWordsButton();
        setupCollectionsButton(); 
        setupLookupButton();
    }
    
        private void setupSavedWordsButton() {
        savedWordsButton.setOnClickListener(v -> {
            Intent savedWordsIntent = new Intent(this, SavedWordsActivity.class);
            startActivity(savedWordsIntent);
        });
    }
    
        private void setupCollectionsButton() {
        collectionsButton.setOnClickListener(v -> {
            Intent collectionsIntent = new Intent(this, CollectionsActivity.class);
            startActivity(collectionsIntent);
        });
    }
    
        private void setupLookupButton() {
        lookupButton.setOnClickListener(v -> showWordLookupDialog());
    }


        private void updateButtonCounters() {
        updateSavedWordsCounter();
        updateCollectionsCounter();
    }
    
        private void updateSavedWordsCounter() {
        int savedWordsCount = getSavedWordsCount();
        savedWordsButton.setText("Saved Words (" + savedWordsCount + ")");
    }
    
        private void updateCollectionsCounter() {
        int collectionsCount = getCollectionsCount();
        collectionsButton.setText("Collections (" + collectionsCount + ")");
    }
    
        private int getSavedWordsCount() {
        SharedPreferences appPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        Set<String> savedWords = appPreferences.getStringSet(SAVED_WORDS_KEY, new HashSet<>());
        return savedWords.size();
    }
    
        private int getCollectionsCount() {
        Map<String, Set<String>> userCollections = CollectionManager.getAllCollections(this);
        return userCollections.size();
    }


        private void showWordLookupDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Look up a word");

        // user input
        EditText wordInputField = createWordInputField();
        dialogBuilder.setView(wordInputField);

        //dialog buttons
        setupLookupDialogButtons(dialogBuilder, wordInputField);

        dialogBuilder.show();
    }
    
        private EditText createWordInputField() {
        EditText inputField = new EditText(this);
        inputField.setHint("Enter Kanji, Kana, or English");
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        return inputField;
    }
    
        private void setupLookupDialogButtons(AlertDialog.Builder dialogBuilder, EditText inputField) {
        // look up button (word search)
        dialogBuilder.setPositiveButton("Look up", (dialog, which) -> {
            String enteredWord = inputField.getText().toString().trim();
            
            if (!enteredWord.isEmpty()) {
                performWordLookup(enteredWord);
            } else {
                Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show();
            }
        });


        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
    }
    
        private void performWordLookup(String searchWord) {
        //check if the word exists using our search helper
        DatabaseHelper.DictionarySearchHelper searchHelper = new DatabaseHelper.DictionarySearchHelper(this);
        DatabaseHelper.DictionarySearchHelper.SearchResult result = searchHelper.searchWord(searchWord);
        
        if (result.isFound()) {
            // word found proceed to ResultActivity
            Intent lookupIntent = new Intent(this, ResultActivity.class);
            lookupIntent.putExtra("detected_words", new String[]{searchWord});
            startActivity(lookupIntent);
        } else {
            // not found toast message instead
            Toast.makeText(this, "Word not found in dictionary", Toast.LENGTH_SHORT).show();
        }
    }


        private void handleTutorialIfNeeded() {
        if (shouldStartTutorial()) {
            startDecksTutorial();
            clearTutorialFlag();
        }
    }
    
        private boolean shouldStartTutorial() {
        return getIntent().getBooleanExtra(TUTORIAL_EXTRA_KEY, false);
    }
    
        private void startDecksTutorial() {
        savedWordsButton.post(() -> {
            TutorialSequenceManager tutorialManager = new TutorialSequenceManager(this);
            tutorialManager.continueInDecksScreen();
        });
    }
    
        private void clearTutorialFlag() {
        getIntent().removeExtra(TUTORIAL_EXTRA_KEY);
    }
}