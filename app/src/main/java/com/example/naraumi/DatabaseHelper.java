package com.example.naraumi;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_FILENAME = "edict.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_DIRECTORY = "databases";
    
        private static final int FILE_COPY_BUFFER_SIZE = 1024;
    
        private static final String LOG_TAG = "DatabaseHelper";

        private final Context applicationContext;
    private final String databaseDirectoryPath;

        public DatabaseHelper(Context context) {
        super(context, DATABASE_FILENAME, null, DATABASE_VERSION);
        this.applicationContext = context;
        this.databaseDirectoryPath = getDatabaseDirectoryPath();
    }

        @Override
    public void onCreate(SQLiteDatabase db) {
            }

        @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }

        private boolean doesDatabaseExist() {
        File databaseFile = new File(databaseDirectoryPath + DATABASE_FILENAME);
        boolean exists = databaseFile.exists();
        Log.d(LOG_TAG, "Database exists: " + exists + " at path: " + databaseFile.getAbsolutePath());
        return exists;
    }

        private void copyDatabaseFromAssets() throws IOException {
        Log.d(LOG_TAG, "Starting database copy from assets");
        
        try (InputStream assetInputStream = applicationContext.getAssets().open(DATABASE_FILENAME);
             OutputStream internalOutputStream = new FileOutputStream(databaseDirectoryPath + DATABASE_FILENAME)) {
            
                        byte[] copyBuffer = new byte[FILE_COPY_BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = assetInputStream.read(copyBuffer)) > 0) {
                internalOutputStream.write(copyBuffer, 0, bytesRead);
            }
            
                        internalOutputStream.flush();
            Log.d(LOG_TAG, "Database copy completed successfully");
            
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to copy database from assets", e);
            throw e;
        }
    }

        public void createDatabase() throws IOException {
        boolean databaseExists = doesDatabaseExist();
        
        if (!databaseExists) {
            Log.d(LOG_TAG, "Database doesn't exist, creating it");
            
                        File databaseDir = new File(databaseDirectoryPath);
            if (!databaseDir.exists()) {
                boolean dirCreated = databaseDir.mkdirs();
                Log.d(LOG_TAG, "Database directory created: " + dirCreated);
            }
            
                        copyDatabaseFromAssets();
            
            Log.d(LOG_TAG, "Database creation completed");
        } else {
            Log.d(LOG_TAG, "Database already exists, skipping creation");
            
                        try (SQLiteDatabase db = openDatabase()) {
                try (Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Dict'", null)) {
                    if (!cursor.moveToFirst()) {
                        Log.w(LOG_TAG, "Database exists but Dict table is missing - forcing recreation");
                        forceRecreateDatabase();
                    } else {
                        Log.d(LOG_TAG, "Database validation passed - Dict table exists");
                    }
                }
            } catch (Exception e) {
                Log.w(LOG_TAG, "Database validation failed - forcing recreation", e);
                forceRecreateDatabase();
            }
        }
    }
    
        public void forceRecreateDatabase() throws IOException {
        Log.d(LOG_TAG, "Forcing database recreation");
        
                File databaseFile = new File(databaseDirectoryPath + DATABASE_FILENAME);
        if (databaseFile.exists()) {
            boolean deleted = databaseFile.delete();
            Log.d(LOG_TAG, "Existing database deleted: " + deleted);
        }
        
                File databaseDir = new File(databaseDirectoryPath);
        if (!databaseDir.exists()) {
            boolean dirCreated = databaseDir.mkdirs();
            Log.d(LOG_TAG, "Database directory created: " + dirCreated);
        }
        
                copyDatabaseFromAssets();
        
        Log.d(LOG_TAG, "Database recreation completed");
    }

        public SQLiteDatabase openDatabase() throws SQLException {
        String databasePath = databaseDirectoryPath + DATABASE_FILENAME;
        Log.d(LOG_TAG, "Opening database at: " + databasePath);
        
        return SQLiteDatabase.openDatabase(
            databasePath, 
            null, 
            SQLiteDatabase.OPEN_READONLY
        );
    }

        private String getDatabaseDirectoryPath() {
        File databaseDir = new File(applicationContext.getApplicationInfo().dataDir, DATABASE_DIRECTORY);
        String path = databaseDir.getAbsolutePath() + File.separator;
        Log.d(LOG_TAG, "Database directory path: " + path);
        return path;
    }

        public static class DictionarySearchHelper {
        private final DatabaseHelper databaseHelper;
        
        public DictionarySearchHelper(Context context) {
            this.databaseHelper = new DatabaseHelper(context);
                        try {
                this.databaseHelper.createDatabase();
            } catch (Exception e) {
                android.util.Log.e("DictionarySearchHelper", "Error initializing database", e);
            }
        }
        
                public SearchResult searchWord(String queryText) {
            if (queryText == null || queryText.trim().isEmpty()) {
                return new SearchResult(false, "", "Empty search query");
            }
            
            String cleanQuery = queryText.trim();
            android.util.Log.d("DictionarySearchHelper", "Searching for: " + cleanQuery);
            
                        SearchResult fallbackResult = searchFallbackDictionary(cleanQuery);
            if (fallbackResult.isFound()) {
                android.util.Log.d("DictionarySearchHelper", "Found in fallback dictionary: " + cleanQuery);
                return fallbackResult;
            }
            
            try (SQLiteDatabase db = databaseHelper.openDatabase()) {
                                android.util.Log.d("DictionarySearchHelper", "=== DATABASE DEBUG INFO ===");
                try (Cursor tablesCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)) {
                    android.util.Log.d("DictionarySearchHelper", "Tables in database:");
                    while (tablesCursor.moveToNext()) {
                        String tableName = tablesCursor.getString(0);
                        android.util.Log.d("DictionarySearchHelper", "  - " + tableName);
                        
                                                if (tableName.toLowerCase().contains("dict") || tableName.toLowerCase().contains("edict")) {
                            try (Cursor columnsCursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
                                android.util.Log.d("DictionarySearchHelper", "    Columns in " + tableName + ":");
                                while (columnsCursor.moveToNext()) {
                                    String columnName = columnsCursor.getString(1);
                                    String columnType = columnsCursor.getString(2);
                                    android.util.Log.d("DictionarySearchHelper", "      - " + columnName + " (" + columnType + ")");
                                }
                            }
                            
                                                        try (Cursor sampleCursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null)) {
                                if (sampleCursor.moveToFirst()) {
                                    android.util.Log.d("DictionarySearchHelper", "    Sample row from " + tableName + ":");
                                    for (int i = 0; i < sampleCursor.getColumnCount(); i++) {
                                        String columnName = sampleCursor.getColumnName(i);
                                        String value = sampleCursor.getString(i);
                                        android.util.Log.d("DictionarySearchHelper", "      " + columnName + " = " + value);
                                    }
                                }
                            }
                        }
                    }
                }
                android.util.Log.d("DictionarySearchHelper", "=== END DATABASE DEBUG ===");
                
                                if (!isEnglish(cleanQuery)) {
                    SearchResult japaneseResult = searchJapanese(db, cleanQuery);
                    if (japaneseResult.isFound()) {
                        return japaneseResult;
                    }
                }
                
                                return searchEnglish(db, cleanQuery);
                
            } catch (Exception e) {
                android.util.Log.e("DictionarySearchHelper", "Database search failed, using fallback only", e);
                return new SearchResult(false, "", "Database search failed, but fallback dictionary was checked: " + e.getMessage());
            }
        }
        
                private SearchResult searchFallbackDictionary(String query) {
                        java.util.Map<String, String[]> fallbackDict = new java.util.HashMap<>();
            
            // format query = [kanji, kana, definition]
            fallbackDict.put("可愛い", new String[]{"可愛い", "かわいい", "cute/pretty/lovely/charming/adorable/dear/precious/darling/pet"});
            fallbackDict.put("かわいい", new String[]{"可愛い", "かわいい", "cute/pretty/lovely/charming/adorable/dear/precious/darling/pet"});
            fallbackDict.put("kawaii", new String[]{"可愛い", "かわいい", "cute/pretty/lovely/charming/adorable/dear/precious/darling/pet"});
            fallbackDict.put("cute", new String[]{"可愛い", "かわいい", "cute/pretty/lovely/charming/adorable/dear/precious/darling/pet"});
            
            fallbackDict.put("猫", new String[]{"猫", "ねこ", "cat/(n) feline/domestic cat"});
            fallbackDict.put("ねこ", new String[]{"猫", "ねこ", "cat/(n) feline/domestic cat"});
            fallbackDict.put("neko", new String[]{"猫", "ねこ", "cat/(n) feline/domestic cat"});
            fallbackDict.put("cat", new String[]{"猫", "ねこ", "cat/(n) feline/domestic cat"});
            
            fallbackDict.put("こんにちは", new String[]{"今日は", "こんにちは", "hello/good day/(int) good afternoon"});
            fallbackDict.put("konnichiwa", new String[]{"今日は", "こんにちは", "hello/good day/(int) good afternoon"});
            fallbackDict.put("hello", new String[]{"今日は", "こんにちは", "hello/good day/(int) good afternoon"});
            
            fallbackDict.put("ありがとう", new String[]{"有難う", "ありがとう", "thank you/(int) thanks"});
            fallbackDict.put("arigatou", new String[]{"有難う", "ありがとう", "thank you/(int) thanks"});
            fallbackDict.put("thanks", new String[]{"有難う", "ありがとう", "thank you/(int) thanks"});
            
            fallbackDict.put("本", new String[]{"本", "ほん", "book/volume/script/(n) main/head/this/our/present/real"});
            fallbackDict.put("ほん", new String[]{"本", "ほん", "book/volume/script/(n) main/head/this/our/present/real"});
            fallbackDict.put("hon", new String[]{"本", "ほん", "book/volume/script/(n) main/head/this/our/present/real"});
            fallbackDict.put("book", new String[]{"本", "ほん", "book/volume/script/(n) main/head/this/our/present/real"});
            
            fallbackDict.put("水", new String[]{"水", "みず", "water/(n) cold water/fresh water"});
            fallbackDict.put("みず", new String[]{"水", "みず", "water/(n) cold water/fresh water"});
            fallbackDict.put("mizu", new String[]{"水", "みず", "water/(n) cold water/fresh water"});
            fallbackDict.put("water", new String[]{"水", "みず", "water/(n) cold water/fresh water"});
            
            // exact match
            String[] result = fallbackDict.get(query);
            if (result != null) {
                String wordData = result[0] + "|" + result[1] + "|" + result[2].split("/")[0] + "|" + result[2];
                return new SearchResult(true, wordData, "");
            }
            
            // not case sensitive search for English words
            for (java.util.Map.Entry<String, String[]> entry : fallbackDict.entrySet()) {
                if (entry.getKey().toLowerCase().equals(query.toLowerCase())) {
                    String[] entryResult = entry.getValue();
                    String wordData = entryResult[0] + "|" + entryResult[1] + "|" + entryResult[2].split("/")[0] + "|" + entryResult[2];
                    return new SearchResult(true, wordData, "");
                }
            }
            
            return new SearchResult(false, "", "Not found in fallback dictionary");
        }
        
                private boolean isEnglish(String text) {
            return text.matches("[a-zA-Z\\s]+");
        }
        
                private SearchResult searchJapanese(SQLiteDatabase db, String queryText) {
            android.util.Log.d("DictionarySearchHelper", "Trying Japanese search");
            try (Cursor cursor = db.rawQuery(
                "SELECT kanji, kana, def FROM Dict WHERE kanji = ? OR kana = ? LIMIT 1",
                new String[]{queryText, queryText})) {
                
                if (cursor.moveToFirst()) {
                    return createSearchResult(cursor);
                }
            }
            return new SearchResult(false, "", "Japanese word not found");
        }
        
                private SearchResult searchEnglish(SQLiteDatabase db, String word) {
            android.util.Log.d("DictionarySearchHelper", "Trying English search");
            
            //very short words (1 to 3 characts), avoid false matches
            if (word.length() <= 3) {
                return searchShortEnglishWord(db, word);
            }
            
            //  kanji entries first
            SearchResult kanjiResult = searchEnglishPrioritizeKanji(db, word);
            if (kanjiResult.isFound()) return kanjiResult;
            
            // fallback any result if no kanji found
            SearchResult anyResult = searchEnglishAnyScript(db, word);
            if (anyResult.isFound()) return anyResult;
            
            return new SearchResult(false, "", "English word not found");
        }
        
                private SearchResult searchEnglishPrioritizeKanji(SQLiteDatabase db, String word) {
            // trying exact patterns with kanji priority
            String[] exactPatterns = {
                "(n) " + word + "/%",     // noun first
                "(v) " + word + "/%",     // verb
                "(adj) " + word + "/%",   // adjective
                "(%) " + word + "/%"      // any part
            };
            
            SearchResult result = trySearchPatternsKanjiFirst(db, exactPatterns);
            if (result.isFound()) return result;
            
                        result = trySearchPatternsKanjiFirst(db, new String[]{word + "/%"});
            if (result.isFound()) return result;
            
                        result = trySearchPatternsKanjiFirst(db, new String[]{"%/" + word + "/%", "%/" + word});
            if (result.isFound()) return result;
            
            return new SearchResult(false, "", "No kanji matches");
        }
        
                private SearchResult searchEnglishAnyScript(SQLiteDatabase db, String word) {
                        String[] exactPatterns = {
                "(n) " + word + "/%",     // noun first
                "(v) " + word + "/%",     // verb
                "(adj) " + word + "/%",   // adjective
                "(%) " + word + "/%"      // any part
            };
            
            SearchResult result = trySearchPatterns(db, exactPatterns);
            if (result.isFound()) return result;
            
                        result = trySearchPatterns(db, new String[]{word + "/%"});
            if (result.isFound()) return result;
            
                        result = trySearchPatterns(db, new String[]{"%/" + word + "/%", "%/" + word});
            if (result.isFound()) return result;
            
                        result = tryBroadSearchWithValidation(db, word);
            if (result.isFound()) return result;
            
            return new SearchResult(false, "", "No matches");
        }
        
                private SearchResult trySearchPatternsKanjiFirst(SQLiteDatabase db, String[] patterns) {
            StringBuilder whereClause = new StringBuilder();
            for (int i = 0; i < patterns.length; i++) {
                if (i > 0) whereClause.append(" OR ");
                whereClause.append("def LIKE ?");
            }
            
                        String queryKanjiFirst = "SELECT kanji, kana, def FROM Dict WHERE (" + whereClause + ") AND kanji != kana ORDER BY LENGTH(kanji) DESC LIMIT 10";
            android.util.Log.d("DictionarySearchHelper", "Kanji-priority query: " + queryKanjiFirst + " Patterns: " + java.util.Arrays.toString(patterns));
            
            try (Cursor cursor = db.rawQuery(queryKanjiFirst, patterns)) {
                while (cursor.moveToNext()) {
                    String kanji = cursor.getString(0);
                                        if (containsKanji(kanji)) {
                        return createSearchResult(cursor);
                    }
                }
            }
            
            return new SearchResult(false, "", "No kanji matches");
        }
        
                private boolean containsKanji(String text) {
            if (text == null || text.isEmpty()) return false;
                        return text.matches(".*[\\u4E00-\\u9FAF].*");
        }
        
                private SearchResult searchShortEnglishWord(SQLiteDatabase db, String word) {
            android.util.Log.d("DictionarySearchHelper", "Using strict search for short word: " + word);
            

            String[] exactPOSPatterns = {
                "(n) " + word + "/%",
                "(v) " + word + "/%",
                "(adj) " + word + "/%",
            };
            
            SearchResult kanjiResult = trySearchPatternsKanjiFirst(db, exactPOSPatterns);
            if (kanjiResult.isFound()) return kanjiResult;
            
                        kanjiResult = trySearchPatternsKanjiFirst(db, new String[]{word + "/%"});
            if (kanjiResult.isFound()) return kanjiResult;
            
                        kanjiResult = trySearchPatternsKanjiFirst(db, new String[]{"%/" + word});
            if (kanjiResult.isFound()) return kanjiResult;
            
                        SearchResult anyResult = trySearchPatterns(db, exactPOSPatterns);
            if (anyResult.isFound()) return anyResult;
            
            anyResult = trySearchPatterns(db, new String[]{word + "/%"});
            if (anyResult.isFound()) return anyResult;
            
            anyResult = trySearchPatterns(db, new String[]{"%/" + word});
            if (anyResult.isFound()) return anyResult;
            
            return new SearchResult(false, "", "Short word not found");
        }
        
                private SearchResult tryBroadSearchWithValidation(SQLiteDatabase db, String word) {
            String query = "SELECT kanji, kana, def FROM Dict WHERE def LIKE ? LIMIT 10";
            String pattern = "%" + word + "%";
            
            try (Cursor cursor = db.rawQuery(query, new String[]{pattern})) {
                while (cursor.moveToNext()) {
                    String def = cursor.getString(2);
                    if (isValidWordMatch(def, word)) {
                        return createSearchResult(cursor);
                    }
                }
            }
            return new SearchResult(false, "", "No valid matches");
        }
        
                private boolean isValidWordMatch(String definition, String targetWord) {
            if (definition == null) return false;
            
                        String cleanedDef = definition.replaceAll("\\(.*?\\)", "").trim();
            String[] parts = cleanedDef.split("[/,;]");
            
            String cleanTarget = targetWord.toLowerCase();
            
                        for (String part : parts) {
                String cleanPart = part.trim().toLowerCase();
                if (cleanPart.equals(cleanTarget)) {
                    return true;
                }
            }
            
                        for (String part : parts) {
                String cleanPart = part.trim().toLowerCase();
                if (cleanPart.startsWith(cleanTarget + " ") || cleanPart.startsWith(cleanTarget + ",")) {
                    return true;
                }
            }
            
                        if (cleanTarget.length() > 3) {                  for (String part : parts) {
                    String cleanPart = part.trim().toLowerCase();
                    if (cleanPart.matches(".*\\b" + java.util.regex.Pattern.quote(cleanTarget) + "\\b.*")) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
                private SearchResult trySearchPatterns(SQLiteDatabase db, String[] patterns) {
            StringBuilder whereClause = new StringBuilder();
            for (int i = 0; i < patterns.length; i++) {
                if (i > 0) whereClause.append(" OR ");
                whereClause.append("def LIKE ?");
            }
            
            String query = "SELECT kanji, kana, def FROM Dict WHERE " + whereClause + " LIMIT 1";
            android.util.Log.d("DictionarySearchHelper", "Query: " + query + " Patterns: " + java.util.Arrays.toString(patterns));
            
            try (Cursor cursor = db.rawQuery(query, patterns)) {
                if (cursor.moveToFirst()) {
                    return createSearchResult(cursor);
                }
            }
            return new SearchResult(false, "", "No matches");
        }
        
                private SearchResult createSearchResult(Cursor cursor) {
            String kanji = cursor.getString(0);
            String kana = cursor.getString(1);
            String def = cursor.getString(2);
            
            android.util.Log.d("DictionarySearchHelper", "Found: " + kanji + " / " + kana + " - " + def);
            
            // main definition
            String cleanedDef = def.replaceAll("\\(.*?\\)", "").trim();
            String[] parts = cleanedDef.split("/");
            String mainMeaning = parts.length > 0 ? parts[0].trim() : "No definition";
            
            // kanji|kana|meaning|rawDefinition
            String wordData = kanji + "|" + kana + "|" + mainMeaning + "|" + def;
            return new SearchResult(true, wordData, "");
        }
        
                public static class SearchResult {
            private final boolean found;
            private final String wordData;
            private final String errorMessage;
            
            public SearchResult(boolean found, String wordData, String errorMessage) {
                this.found = found;
                this.wordData = wordData;
                this.errorMessage = errorMessage;
            }
            
            public boolean isFound() { return found; }
            public String getWordData() { return wordData; }
            public String getErrorMessage() { return errorMessage; }
            
            // kanji from word data
            public String getKanji() {
                if (!found) return "";
                String[] parts = wordData.split("\\|");
                return parts.length > 0 ? parts[0] : "";
            }
            
                        public String getKana() {
                if (!found) return "";
                String[] parts = wordData.split("\\|");
                return parts.length > 1 ? parts[1] : "";
            }
            
                        public String getMeaning() {
                if (!found) return "";
                String[] parts = wordData.split("\\|");
                return parts.length > 2 ? parts[2] : "";
            }
            
                        public String getRawDefinition() {
                if (!found) return "";
                String[] parts = wordData.split("\\|");
                return parts.length > 3 ? parts[3] : "";
            }
        }
    }
}