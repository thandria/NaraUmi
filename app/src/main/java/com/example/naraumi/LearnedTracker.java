package com.example.naraumi;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class LearnedTracker {


        private static final String PROGRESS_PREFERENCES = "JapLearnProgress";
    
        private static final String LEARNED_KANA_KEY = "learned_kana";
    
        private static final String LEARNED_VOCAB_KEY = "learned_vocabulary";
    
        private static final String LEARNED_GRAMMAR_KEY = "learned_grammar";
    
        private static final String COMPLETED_LESSONS_KEY = "completed_lessons";
    
        private static final String MASTERY_LEVEL_KEY = "mastery_level_";


        public static void markKanaLearned(Context context, String kanaCharacter) {
        addToLearnedSet(context, LEARNED_KANA_KEY, kanaCharacter);
    }
    
        public static boolean isKanaLearned(Context context, String kanaCharacter) {
        return isInLearnedSet(context, LEARNED_KANA_KEY, kanaCharacter);
    }
    
        public static Set<String> getLearnedKana(Context context) {
        return getLearnedSet(context, LEARNED_KANA_KEY);
    }
    
        public static int getLearnedKanaCount(Context context) {
        return getLearnedKana(context).size();
    }


        public static void markVocabularyLearned(Context context, String vocabularyWord) {
        addToLearnedSet(context, LEARNED_VOCAB_KEY, vocabularyWord);
    }
    
        public static boolean isVocabularyLearned(Context context, String vocabularyWord) {
        return isInLearnedSet(context, LEARNED_VOCAB_KEY, vocabularyWord);
    }
    
        public static Set<String> getLearnedVocabulary(Context context) {
        return getLearnedSet(context, LEARNED_VOCAB_KEY);
    }
    
        public static int getLearnedVocabularyCount(Context context) {
        return getLearnedVocabulary(context).size();
    }


        public static void markGrammarLearned(Context context, String grammarPattern) {
        addToLearnedSet(context, LEARNED_GRAMMAR_KEY, grammarPattern);
    }
    
        public static boolean isGrammarLearned(Context context, String grammarPattern) {
        return isInLearnedSet(context, LEARNED_GRAMMAR_KEY, grammarPattern);
    }
    
        public static Set<String> getLearnedGrammar(Context context) {
        return getLearnedSet(context, LEARNED_GRAMMAR_KEY);
    }


        public static void markLessonCompleted(Context context, String lessonId) {
        addToLearnedSet(context, COMPLETED_LESSONS_KEY, lessonId);
    }
    
        public static boolean isLessonCompleted(Context context, String lessonId) {
        return isInLearnedSet(context, COMPLETED_LESSONS_KEY, lessonId);
    }
    
        public static Set<String> getCompletedLessons(Context context) {
        return getLearnedSet(context, COMPLETED_LESSONS_KEY);
    }
    
        public static int getCompletedLessonCount(Context context) {
        return getCompletedLessons(context).size();
    }


        public static void setMasteryLevel(Context context, String itemId, int masteryLevel) {
        SharedPreferences preferences = getProgressPreferences(context);
        preferences.edit()
                .putInt(MASTERY_LEVEL_KEY + itemId, masteryLevel)
                .apply();
    }
    
        public static int getMasteryLevel(Context context, String itemId) {
        SharedPreferences preferences = getProgressPreferences(context);
        return preferences.getInt(MASTERY_LEVEL_KEY + itemId, 0);
    }


        public static int getTotalLearnedItems(Context context) {
        return getLearnedKanaCount(context) + 
               getLearnedVocabularyCount(context) + 
               getLearnedGrammar(context).size() + 
               getCompletedLessonCount(context);
    }
    
        public static float getProgressPercentage(Context context, String category, int totalItemsInCategory) {
        if (totalItemsInCategory <= 0) {
            return 0.0f;
        }
        
        int learnedCount = 0;
        
        switch (category.toLowerCase()) {
            case "kana":
                learnedCount = getLearnedKanaCount(context);
                break;
            case "vocabulary":
                learnedCount = getLearnedVocabularyCount(context);
                break;
            case "grammar":
                learnedCount = getLearnedGrammar(context).size();
                break;
            case "lessons":
                learnedCount = getCompletedLessonCount(context);
                break;
        }
        
        return Math.min(1.0f, (float) learnedCount / totalItemsInCategory);
    }


        public static void clearAllProgress(Context context) {
        SharedPreferences preferences = getProgressPreferences(context);
        preferences.edit().clear().apply();
    }
    
        public static void unmarkItemLearned(Context context, String category, String item) {
        removeFromLearnedSet(context, category, item);
    }


        private static SharedPreferences getProgressPreferences(Context context) {
        return context.getSharedPreferences(PROGRESS_PREFERENCES, Context.MODE_PRIVATE);
    }
    
        private static void addToLearnedSet(Context context, String setKey, String item) {
        if (item == null || item.trim().isEmpty()) {
            return;
        }
        
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> learnedSet = new HashSet<>(preferences.getStringSet(setKey, new HashSet<>()));
        learnedSet.add(item.trim());
        
        preferences.edit()
                .putStringSet(setKey, learnedSet)
                .apply();
    }
    
        private static void removeFromLearnedSet(Context context, String setKey, String item) {
        if (item == null || item.trim().isEmpty()) {
            return;
        }
        
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> learnedSet = new HashSet<>(preferences.getStringSet(setKey, new HashSet<>()));
        learnedSet.remove(item.trim());
        
        preferences.edit()
                .putStringSet(setKey, learnedSet)
                .apply();
    }
    
        private static boolean isInLearnedSet(Context context, String setKey, String item) {
        if (item == null || item.trim().isEmpty()) {
            return false;
        }
        
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> learnedSet = preferences.getStringSet(setKey, new HashSet<>());
        return learnedSet.contains(item.trim());
    }
    
        private static Set<String> getLearnedSet(Context context, String setKey) {
        SharedPreferences preferences = getProgressPreferences(context);
        return new HashSet<>(preferences.getStringSet(setKey, new HashSet<>()));
    }
}
