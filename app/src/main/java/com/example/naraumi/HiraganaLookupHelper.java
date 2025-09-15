package com.example.naraumi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HiraganaLookupHelper {
    

        private static final String LOG_TAG = "HiraganaLookupHelper";
    
        private static final String KANJI_REGEX = ".*[\\u4E00-\\u9FAF].*";
    
        private static final String JAPANESE_TEXT_REGEX = ".*[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF].*";
    

        private final DatabaseHelper databaseHelper;
    
        private final Context applicationContext;
    

        public HiraganaLookupHelper(Context context) {
        this.applicationContext = context;
        this.databaseHelper = new DatabaseHelper(context);
        
        try {
            databaseHelper.createDatabase();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Database initialisation failed", e);
        }
    }
    

        public String getHiraganaReading(String japaneseWord) {
        if (!isValidInputForReading(japaneseWord)) {
            return "";
        }
        
                if (!containsKanji(japaneseWord)) {
            return "";
        }

        try {
            return performDatabaseLookup(japaneseWord);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Database query failed for text: " + japaneseWord, e);
            return "";
        }
    }
    
        private boolean isValidInputForReading(String input) {
        return input != null && !input.trim().isEmpty();
    }
    
        private String performDatabaseLookup(String japaneseWord) {
        SQLiteDatabase database = databaseHelper.openDatabase();
        
        try {
                        Cursor cursor = database.rawQuery(
                "SELECT kana FROM Dict WHERE kanji = ?", 
                new String[]{japaneseWord});
            
            if (cursor.moveToFirst()) {
                String kanaReading = cursor.getString(cursor.getColumnIndexOrThrow("kana"));
                cursor.close();
                
                                return (kanaReading != null && !kanaReading.equals(japaneseWord)) ? kanaReading : "";
            }
            
            cursor.close();
            return "";
            
        } finally {
            database.close();
        }
    }
    

        public boolean containsKanji(String text) {
        if (text == null) {
            return false;
        }
        return text.matches(KANJI_REGEX);
    }
    
        public boolean isJapaneseText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches(JAPANESE_TEXT_REGEX);
    }
    
        public boolean isHiraganaOnly(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches("^[\\u3040-\\u309F]+$");
    }
    
        public boolean isKatakanaOnly(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches("^[\\u30A0-\\u30FF]+$");
    }
    

        @Deprecated
    public boolean wordHasKanji(String text) {
        return containsKanji(text);
    }
    
        public String getCharacterTypeDescription(String text) {
        if (text == null || text.isEmpty()) {
            return "Empty text";
        }
        
        boolean hasKanji = containsKanji(text);
        boolean hasHiragana = text.matches(".*[\\u3040-\\u309F].*");
        boolean hasKatakana = text.matches(".*[\\u30A0-\\u30FF].*");
        
        if (hasKanji && hasHiragana && hasKatakana) {
            return "Mixed (Kanji + Hiragana + Katakana)";
        } else if (hasKanji && hasHiragana) {
            return "Kanji + Hiragana";
        } else if (hasKanji && hasKatakana) {
            return "Kanji + Katakana";
        } else if (hasKanji) {
            return "Kanji only";
        } else if (hasHiragana) {
            return "Hiragana only";
        } else if (hasKatakana) {
            return "Katakana only";
        } else if (isJapaneseText(text)) {
            return "Other Japanese characters";
        } else {
            return "Non-Japanese text";
        }
    }
} 