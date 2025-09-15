package com.example.naraumi;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.*;
import androidx.core.content.ContextCompat;
import java.util.Map;
import java.util.Set;

public class CollectionsActivity extends BaseActivity {

    private static final int COLLECTION_BUTTON_BACKGROUND = R.drawable.rounded_square_purple;
    private static final int BUTTON_PADDING = 16;
    private static final int BUTTON_MARGIN = 24;
    private static final String COLLECTION_NAME_EXTRA = "collection_name";

    private LinearLayout collectionListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        initialiseUIComponents();
        setupAddCollectionButton();
        loadAndDisplayCollections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplayCollections();
    }

        private void initialiseUIComponents() {
        TextView headerTitle = findViewById(R.id.header_title);
        headerTitle.setText("Collections");

        collectionListContainer = findViewById(R.id.collection_list);
    }
    
        private void setupAddCollectionButton() {
        findViewById(R.id.btn_add_collection).setOnClickListener(v -> showCreateCollectionDialog());
    }

        private void loadAndDisplayCollections() {
        clearCollectionList();
        
        Map<String, Set<String>> allCollections = CollectionManager.getAllCollections(this);
        
        for (String collectionName : allCollections.keySet()) {
            Button collectionButton = createCollectionButton(collectionName);
            collectionListContainer.addView(collectionButton);
        }
    }
    
        private void clearCollectionList() {
        collectionListContainer.removeAllViews();
    }
    
        private Button createCollectionButton(String collectionName) {
        Button collectionButton = new Button(this);
        
        setupCollectionButtonAppearance(collectionButton, collectionName);
        setupCollectionButtonLayout(collectionButton);
        setupCollectionButtonHandlers(collectionButton, collectionName);
        
        return collectionButton;
    }
    
        private void setupCollectionButtonAppearance(Button button, String collectionName) {
        button.setText(collectionName);
        button.setAllCaps(false);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        button.setBackgroundResource(COLLECTION_BUTTON_BACKGROUND);
        button.setPadding(BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING);
    }
    
        private void setupCollectionButtonLayout(Button button) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, BUTTON_MARGIN);
        button.setLayoutParams(layoutParams);
    }
    
        private void setupCollectionButtonHandlers(Button button, String collectionName) {
        button.setOnClickListener(v -> openCollectionDetail(collectionName));
        
        button.setOnLongClickListener(v -> {
            showCollectionOptionsDialog(collectionName);
            return true;
        });
    }

        private void openCollectionDetail(String collectionName) {
        Intent detailIntent = new Intent(this, CollectionDetailActivity.class);
        detailIntent.putExtra(COLLECTION_NAME_EXTRA, collectionName);
        startActivity(detailIntent);
    }

        private void showCreateCollectionDialog() {
        EditText nameInput = createTextInputField();

        new AlertDialog.Builder(this)
                .setTitle("New Collection")
                .setMessage("Enter a name:")
                .setView(nameInput)
                .setPositiveButton("Create", (dialog, which) -> createNewCollection(nameInput))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
        private void createNewCollection(EditText nameInput) {
        String collectionName = nameInput.getText().toString().trim();
        
        if (!collectionName.isEmpty()) {
            CollectionManager.saveCollection(this, collectionName, new java.util.HashSet<>());
            loadAndDisplayCollections();
        }
    }

        private void showCollectionOptionsDialog(String collectionName) {
        String[] optionItems = {"Rename", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle(collectionName)
                .setItems(optionItems, (dialog, selectedIndex) -> {
                    if (selectedIndex == 0) {
                        showRenameCollectionDialog(collectionName);
                    } else if (selectedIndex == 1) {
                        deleteCollection(collectionName);
                    }
                }).show();
    }
    
        private void showRenameCollectionDialog(String currentName) {
        EditText nameInput = createTextInputField();
        nameInput.setText(currentName);

        new AlertDialog.Builder(this)
                .setTitle("Rename Collection")
                .setView(nameInput)
                .setPositiveButton("Rename", (dialog, which) -> renameCollection(currentName, nameInput))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
        private void renameCollection(String oldName, EditText nameInput) {
        String newName = nameInput.getText().toString().trim();
        
        if (!newName.isEmpty()) {
            CollectionManager.renameCollection(this, oldName, newName);
            loadAndDisplayCollections();
        }
    }
    
        private void deleteCollection(String collectionName) {
        CollectionManager.deleteCollection(this, collectionName);
        loadAndDisplayCollections();
    }

        private EditText createTextInputField() {
        EditText inputField = new EditText(this);
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        return inputField;
    }
}