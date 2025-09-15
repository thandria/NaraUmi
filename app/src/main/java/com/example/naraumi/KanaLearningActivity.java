package com.example.naraumi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class KanaLearningActivity extends BaseActivity implements TTSHelper.TTSReadyListener {

        private int currentCardIndex = 0;
    
    // lesson content arrays
    private String[] japaneseCharacters;    // Japanese text (kana/kanji)
    private String[] englishMeanings;       // English
    private String[] romajiPronunciations;  // Romanji
    
    // UI
    private TextView japaneseTextDisplay;
    private TextView englishMeaningDisplay;
    private TextView romajiDisplay;
    private TextView lessonHeaderTitle;
    private ImageButton previousButton;
    private ImageButton nextButton;
    
    // tts helper for pronunciation
    private TTSHelper speechHelper;
    
    // lesson config
    private boolean isPhraseLessonMode = false;  // true for phrases, false for kana
    private String lessonType;                   // "HIRAGANA", "KATAKANA", "BASICS", etc.
    private String lessonGroup;                  // specific group within the lesson type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kana_learning);
        
                initialiseUIComponents();
        
                extractLessonParameters();
        
                setLessonHeaderTitle();
        
                loadLessonContent();
        
                displayCurrentCard();
        updateNavigationButtons();
        
                setupNavigationListeners();
        setupAudioListener();
        
                speechHelper = new TTSHelper(this, this);
    }

        private void initialiseUIComponents() {
        japaneseTextDisplay = findViewById(R.id.kana_display);
        englishMeaningDisplay = findViewById(R.id.kana_meaning);
        romajiDisplay = findViewById(R.id.kana_romaji);
        lessonHeaderTitle = findViewById(R.id.header_title);
        previousButton = findViewById(R.id.btn_prev);
        nextButton = findViewById(R.id.btn_next);
    }

        private void extractLessonParameters() {
        lessonType = getIntent().getStringExtra("KANA_TYPE");
        lessonGroup = getIntent().getStringExtra("KANA_GROUP");
        isPhraseLessonMode = getIntent().getBooleanExtra("PHRASE_MODE", false);
    }

        private void setLessonHeaderTitle() {
        if (isPhraseLessonMode) {
            lessonHeaderTitle.setText("LESSON: " + lessonGroup);
        } else {
            lessonHeaderTitle.setText("LESSON: KANA");
        }
    }

        private void loadLessonContent() {
        if (isPhraseLessonMode && isAdvancedLessonType(lessonType)) {
            loadPhraseContent(lessonGroup);
        } else {
            loadKanaContent(lessonType, lessonGroup);
        }
    }

        private boolean isAdvancedLessonType(String type) {
        return "BASICS".equals(type) || "BASICS 2".equals(type) || 
               "ADVANCED 1".equals(type) || "ADVANCED 2".equals(type);
    }

        private void setupNavigationListeners() {
        nextButton.setOnClickListener(v -> handleNextButtonClick());
        previousButton.setOnClickListener(v -> handlePreviousButtonClick());
    }

        private void setupAudioListener() {
        findViewById(R.id.btn_sound).setOnClickListener(v -> playCurrentPronunciation());
        findViewById(R.id.btn_sound).setOnLongClickListener(v -> {
            // Long press on audio button shows TTS setup
            speechHelper.showTTSSetupSteps();
            Toast.makeText(this, "Long press detected - showing TTS setup", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

        private void handleNextButtonClick() {
        if (currentCardIndex < japaneseCharacters.length - 1) {
            currentCardIndex++;
            displayCurrentCard();
            updateNavigationButtons();
        } else {
            showLessonCompletionScreen();
        }
    }

        private void handlePreviousButtonClick() {
        if (currentCardIndex > 0) {
            currentCardIndex--;
            displayCurrentCard();
            updateNavigationButtons();
        } else {
            Toast.makeText(this, "You're at the first character", Toast.LENGTH_SHORT).show();
        }
    }

        private void playCurrentPronunciation() {
        if (speechHelper != null && !speechHelper.isSpeaking()) {
            String textToSpeak = japaneseCharacters[currentCardIndex];
            speechHelper.speak(textToSpeak);
        }
    }

        private void displayCurrentCard() {
                currentCardIndex = Math.max(0, Math.min(currentCardIndex, japaneseCharacters.length - 1));
        
                japaneseTextDisplay.setText(japaneseCharacters[currentCardIndex]);
        englishMeaningDisplay.setText("Meaning: " + englishMeanings[currentCardIndex]);
        
        // show romaji if available
        if (romajiPronunciations != null && currentCardIndex < romajiPronunciations.length) {
            romajiDisplay.setText(romajiPronunciations[currentCardIndex]);
        }
    }

        private void updateNavigationButtons() {
        // hide previous button on first card else enable
        if (currentCardIndex == 0) {
            previousButton.setVisibility(View.GONE);
        } else {
            previousButton.setVisibility(View.VISIBLE);
            previousButton.setEnabled(true);
            previousButton.setColorFilter(null);
        }
        
                nextButton.setEnabled(true);
        nextButton.setColorFilter(null);
    }

    /**
     * Loads phrase content for advanced lessons (Basics, Advanced levels)
     */
    private void loadPhraseContent(String phraseGroup) {
        switch (phraseGroup) {
            case "Greetings 1":
                japaneseCharacters = new String[]{"こんにちは", "お元気ですか", "元気です"};
                englishMeanings = new String[]{"Hello", "How are you?", "I'm fine"};
                romajiPronunciations = new String[]{"konnichiwa", "ogenki desu ka", "genki desu"};
                break;
                
            case "Greetings 2":
                japaneseCharacters = new String[]{"ありがとう", "はじめまして", "じゃあね"};
                englishMeanings = new String[]{"Thank you", "Nice to meet you", "See you later"};
                romajiPronunciations = new String[]{"arigatou", "hajimemashite", "jaa ne"};
                break;
                
            case "Office Items":
                japaneseCharacters = new String[]{"マウス", "キーボード", "テレビ", "本"};
                englishMeanings = new String[]{"Mouse", "Keyboard", "TV", "Book"};
                romajiPronunciations = new String[]{"mausu", "kiiboodo", "terebi", "hon"};
                break;
                
            case "Classroom 1":
                japaneseCharacters = new String[]{"教室", "先生", "学生", "宿題"};
                englishMeanings = new String[]{"Classroom", "Teacher", "Student", "Homework"};
                romajiPronunciations = new String[]{"kyoushitsu", "sensei", "gakusei", "shukudai"};
                break;
                
            case "Classroom 2":
                japaneseCharacters = new String[]{"試験", "授業", "質問", "答え"};
                englishMeanings = new String[]{"Exam", "Class/Lesson", "Question", "Answer"};
                romajiPronunciations = new String[]{"shiken", "jugyou", "shitsumon", "kotae"};
                break;
                
            case "Class Items":
                japaneseCharacters = new String[]{"ペン", "鉛筆", "ノート", "消しゴム", "定規"};
                englishMeanings = new String[]{"Pen", "Pencil", "Notebook", "Eraser", "Ruler"};
                romajiPronunciations = new String[]{"pen", "enpitsu", "nooto", "keshigomu", "jougi"};
                break;
                
                        case "Travel 1":
                japaneseCharacters = new String[]{"空港", "駅", "ホテル", "切符", "荷物"};
                englishMeanings = new String[]{"Airport", "Station", "Hotel", "Ticket", "Luggage"};
                romajiPronunciations = new String[]{"kuukou", "eki", "hoteru", "kippu", "nimotsu"};
                break;
                
            case "Travel 2":
                japaneseCharacters = new String[]{"地図", "道", "方向", "右", "左"};
                englishMeanings = new String[]{"Map", "Road/Way", "Direction", "Right", "Left"};
                romajiPronunciations = new String[]{"chizu", "michi", "houkou", "migi", "hidari"};
                break;
                
            case "Food Items":
                japaneseCharacters = new String[]{"寿司", "ラーメン", "お米", "野菜", "肉"};
                englishMeanings = new String[]{"Sushi", "Ramen", "Rice", "Vegetables", "Meat"};
                romajiPronunciations = new String[]{"sushi", "raamen", "okome", "yasai", "niku"};
                break;
                
            case "Restaurant":
                japaneseCharacters = new String[]{"メニュー", "注文", "お会計", "美味しい", "飲み物"};
                englishMeanings = new String[]{"Menu", "Order", "Bill/Check", "Delicious", "Drinks"};
                romajiPronunciations = new String[]{"menyuu", "chuumon", "okaikei", "oishii", "nomimono"};
                break;
                
            // Advanced Level 2 Content
            case "Business 1":
                japaneseCharacters = new String[]{"会社", "会議", "仕事", "プロジェクト", "報告"};
                englishMeanings = new String[]{"Company", "Meeting", "Work/Job", "Project", "Report"};
                romajiPronunciations = new String[]{"kaisha", "kaigi", "shigoto", "purojekuto", "houkoku"};
                break;
                
            case "Business 2":
                japaneseCharacters = new String[]{"契約", "交渉", "提案", "締切", "成功"};
                englishMeanings = new String[]{"Contract", "Negotiation", "Proposal", "Deadline", "Success"};
                romajiPronunciations = new String[]{"keiyaku", "koushou", "teian", "shimekiri", "seikou"};
                break;
                
            case "Formal Greetings":
                japaneseCharacters = new String[]{"お疲れ様です", "申し訳ございません", "恐れ入ります", "失礼いたします", "よろしくお願いします"};
                englishMeanings = new String[]{"Thank you for your hard work", "I sincerely apologize", "Excuse me (formal)", "Excuse me (leaving)", "Please treat me favorably"};
                romajiPronunciations = new String[]{"otsukaresama desu", "moushiwake gozaimasen", "osore irimasu", "shitsurei itashimasu", "yoroshiku onegaishimasu"};
                break;
                
            case "Polite Speech":
                japaneseCharacters = new String[]{"いらっしゃいませ", "恐縮です", "お忙しい", "ご確認", "お手伝い"};
                englishMeanings = new String[]{"Welcome (to customer)", "I'm sorry/grateful", "Busy (respectful)", "Confirmation (respectful)", "Assistance (respectful)"};
                romajiPronunciations = new String[]{"irasshaimase", "kyoushuku desu", "oisogashii", "gokakunin", "otetsudai"};
                break;
                
            default:
                Toast.makeText(this, "Group " + phraseGroup + " not available", Toast.LENGTH_SHORT).show();
                finish();
        }
    }

        private void loadKanaContent(String kanaType, String kanaGroup) {
        String[][] kanaData = KanaContentProvider.getKanaGroup(kanaType, kanaGroup);
        
        if (kanaData != null && kanaData.length > 0) {
                        japaneseCharacters = new String[kanaData.length];
            englishMeanings = new String[kanaData.length];
            romajiPronunciations = new String[kanaData.length];
            
                        for (int i = 0; i < kanaData.length; i++) {
                japaneseCharacters[i] = kanaData[i][0];  // Kana character
                englishMeanings[i] = kanaData[i][1];     // Romaji reading
                
                                if (kanaData[i].length > 2) {
                    romajiPronunciations[i] = kanaData[i][2];
                } else {
                    romajiPronunciations[i] = kanaData[i][1];
                }
            }
        } else {
                        japaneseCharacters = new String[]{"N/A"};
            englishMeanings = new String[]{"Not available"};
            romajiPronunciations = new String[]{"N/A"};
        }
    }

        private void showLessonCompletionScreen() {
        // creates lesson completion key for progress tracking
        String completionKey = LessonProgressManager.createLessonKey(
            lessonType,    // "HIRAGANA", "BASICS"
            lessonGroup,   // "A-KA", "Greetings 1"
            "COMPLETED"
        );
        
                StreakManager.updateStreak(this);
        
                LessonProgressManager.markLessonCompleted(this, completionKey);
        
                ActivityRefreshManager.setRefreshNeeded();
        
                Intent completionIntent = new Intent(this, KanaCompletionActivity.class);
        completionIntent.putExtra("LEARNED_COUNT", japaneseCharacters.length);
        completionIntent.putExtra("KANA_TYPE", lessonType);
        completionIntent.putExtra("KANA_GROUP", lessonGroup);
        completionIntent.putExtra("PHRASE_MODE", isPhraseLessonMode);
        
        startActivity(completionIntent);
    }


    @Override
    public void onTTSReady(boolean japaneseAvailable) {
        if (japaneseAvailable) {
            android.util.Log.d("KanaLearningActivity", "Japanese TTS is ready");
        } else {
            android.util.Log.w("KanaLearningActivity", "Japanese TTS is not available");
                        findViewById(R.id.btn_sound).setAlpha(0.5f);
        }
    }

    @Override
    public void onTTSError() {
        android.util.Log.e("KanaLearningActivity", "TTS initialization failed");
        findViewById(R.id.btn_sound).setAlpha(0.5f);
        Toast.makeText(this, "Audio features unavailable", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        // cleans up tts
        if (speechHelper != null) {
            speechHelper.shutdown();
        }
        super.onDestroy();
    }
}