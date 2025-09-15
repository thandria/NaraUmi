package com.example.naraumi;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionManager {


        private static final String PREFERENCES_NAME = "JapLearnPrefs";


    private static final String COLLECTION_KEY_PREFIX = "collection_";


    private static final String RETENTION_KEY_PREFIX = "retention_";


    private static final String CURRENT_COLLECTION_KEY = "current_collection";


    private static final int DEFAULT_RETENTION_SCORE = 0;


        public static Map<String, Set<String>> getAllCollections(Context context) {
        SharedPreferences preferences = getPreferences(context);
        Map<String, Set<String>> collectionsMap = new HashMap<>();

                for (Map.Entry<String, ?> preferenceEntry : preferences.getAll().entrySet()) {
            String preferenceKey = preferenceEntry.getKey();
            
            if (isCollectionKey(preferenceKey)) {
                String collectionName = extractCollectionName(preferenceKey);
                Set<String> collectionWords = preferences.getStringSet(preferenceKey, new HashSet<>());
                collectionsMap.put(collectionName, collectionWords);
            }
        }

        return collectionsMap;
    }
    

    public static void saveCollection(Context context, String collectionName, Set<String> wordSet) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        
        String collectionKey = buildCollectionKey(collectionName);
        getPreferences(context).edit()
                .putStringSet(collectionKey, wordSet)
                .apply();
    }
    

    public static void deleteCollection(Context context, String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            return;
        }
        
        String collectionKey = buildCollectionKey(collectionName);
        getPreferences(context).edit()
                .remove(collectionKey)
                .apply();
    }
    

    public static void renameCollection(Context context, String oldCollectionName, String newCollectionName) {
        if (oldCollectionName == null || newCollectionName == null || 
            oldCollectionName.trim().isEmpty() || newCollectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection names cannot be null or empty");
        }
        
        if (oldCollectionName.equals(newCollectionName)) {
            return;         }
        
                Set<String> collectionWords = getWordsInCollection(context, oldCollectionName);
        
                SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putStringSet(buildCollectionKey(newCollectionName), collectionWords);
        editor.remove(buildCollectionKey(oldCollectionName));
        editor.apply();
    }
    

    public static boolean collectionExists(Context context, String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            return false;
        }
        
        String collectionKey = buildCollectionKey(collectionName);
        return getPreferences(context).contains(collectionKey);
    }



    public static boolean addWordToCollection(Context context, String collectionName, String wordEntry) {
        if (collectionName == null || wordEntry == null || 
            collectionName.trim().isEmpty() || wordEntry.trim().isEmpty()) {
            return false;
        }
        
        Set<String> collectionWords = new HashSet<>(getWordsInCollection(context, collectionName));
        
        // checks for duplicates
        if (collectionWords.contains(wordEntry)) {
            return false;
        }
        
        // add word and save collection
        collectionWords.add(wordEntry);
        saveCollection(context, collectionName, collectionWords);
        return true;
    }
    

    public static void removeWordFromCollection(Context context, String collectionName, String wordEntry) {
        if (collectionName == null || wordEntry == null) {
            return;
        }
        
        Set<String> collectionWords = new HashSet<>(getWordsInCollection(context, collectionName));
        collectionWords.remove(wordEntry);
        saveCollection(context, collectionName, collectionWords);
    }
    

    public static Set<String> getWordsInCollection(Context context, String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        String collectionKey = buildCollectionKey(collectionName);
        return getPreferences(context).getStringSet(collectionKey, new HashSet<>());
    }
    

    public static int getCollectionWordCount(Context context, String collectionName) {
        return getWordsInCollection(context, collectionName).size();
    }



    public static float calculateAverageRetention(Context context, Set<String> wordSet) {
        if (wordSet == null || wordSet.isEmpty()) {
            return 0.0f;
        }
        
        SharedPreferences preferences = getPreferences(context);
        int totalRetention = 0;
        int wordsWithRetentionData = 0;
        
        for (String wordEntry : wordSet) {
            String retentionKey = RETENTION_KEY_PREFIX + wordEntry;
            int wordRetention = preferences.getInt(retentionKey, DEFAULT_RETENTION_SCORE);
            totalRetention += wordRetention;
            wordsWithRetentionData++;
        }
        
        return wordsWithRetentionData > 0 ? (float) totalRetention / wordsWithRetentionData : 0.0f;
    }
    

    public static int getWordRetention(Context context, String wordEntry) {
        String retentionKey = RETENTION_KEY_PREFIX + wordEntry;
        return getPreferences(context).getInt(retentionKey, DEFAULT_RETENTION_SCORE);
    }
    

    public static void updateWordRetention(Context context, String wordEntry, int retentionScore) {
        String retentionKey = RETENTION_KEY_PREFIX + wordEntry;
        getPreferences(context).edit()
                .putInt(retentionKey, retentionScore)
                .apply();
    }



    public static List<String> getWordsForReview(Context context) {
        String currentCollectionName = getCurrentCollectionName(context);
        
        if (currentCollectionName == null) {
            return new ArrayList<>();
        }
        
        Set<String> collectionWords = getWordsInCollection(context, currentCollectionName);
        return new ArrayList<>(collectionWords);
    }
    

    public static void setCurrentCollection(Context context, String collectionName) {
        getPreferences(context).edit()
                .putString(CURRENT_COLLECTION_KEY, collectionName)
                .apply();
    }
    

    public static String getCurrentCollectionName(Context context) {
        return getPreferences(context).getString(CURRENT_COLLECTION_KEY, null);
    }



    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    

    private static String buildCollectionKey(String collectionName) {
        return COLLECTION_KEY_PREFIX + collectionName;
    }
    

    private static String extractCollectionName(String collectionKey) {
        return collectionKey.replace(COLLECTION_KEY_PREFIX, "");
    }
    

    private static boolean isCollectionKey(String preferenceKey) {
        return preferenceKey != null && preferenceKey.startsWith(COLLECTION_KEY_PREFIX);
    }
    

    public static List<String> getAllCollectionNames(Context context) {
        return new ArrayList<>(getAllCollections(context).keySet());
    }



    @Deprecated
    public static Map<String, Set<String>> getCollections(Context context) {
        return getAllCollections(context);
    }
    

    @Deprecated
    public static void addWord(Context context, String collection, String word) {
        addWordToCollection(context, collection, word);
    }
    

    @Deprecated
    public static void removeWord(Context context, String collection, String word) {
        removeWordFromCollection(context, collection, word);
    }
    

    @Deprecated
    public static float getAverageRetention(Context context, Set<String> words) {
        return calculateAverageRetention(context, words);
    }
    

    @Deprecated
    public static List<String> getReviewWords(Context context) {
        return getWordsForReview(context);
    }
}