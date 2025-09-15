package com.example.naraumi;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;

public class TutorialSequenceManager {


        private static final String TUTORIAL_PREFS = "JapLearnPrefs";
    
        private static final String TUTORIAL_COMPLETED_KEY = "tutorial_completed";
    
        private static final String POPUP_SPACING = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n";


        private final Activity currentActivity;


        public TutorialSequenceManager(Activity activity) {
        this.currentActivity = activity;
    }


        public void startTutorial() {
                View streakBadge = currentActivity.findViewById(R.id.streak_badge);
        View roadmapHeader = currentActivity.findViewById(R.id.rm_header_section);
        View lessonContainer = currentActivity.findViewById(R.id.lesson_container);
        View scanNavButton = currentActivity.findViewById(R.id.nav_app_scan);
        View roadmapNavButton = currentActivity.findViewById(R.id.nav_app_rmap);
        View decksNavButton = currentActivity.findViewById(R.id.nav_app_decks);

                View secondLessonSection = getSecondLessonSection(lessonContainer);

                FancyShowCaseView streakPrompt = createTutorialPrompt(streakBadge,
                "Track your consistency via a daily streak!\nSkipping a day resets your streak.");

        FancyShowCaseView roadmapPrompt = createTutorialPrompt(roadmapHeader,
                "Welcome to your Roadmap!\nThis is the homepage, and most importantly tracks your learning progress along the way.");

        FancyShowCaseView lessonPrompt = createTutorialPrompt(secondLessonSection,
                "Pick what to learn at your own pace!\nContent that ranges from helping you learn the writing system, to being able to converse at a beginner's level.");

        FancyShowCaseView scanPrompt = createTutorialPrompt(scanNavButton,
                "Scan Text (Japanese to English).\nYour device is now equipped to help with comprehension of Japanese text offline!");

        FancyShowCaseView roadmapNavPrompt = createTutorialPrompt(roadmapNavButton,
                "Roadmap\nTap here anytime you need to come back to this screen.");

        FancyShowCaseView decksPrompt = createTutorialPrompt(decksNavButton,
                "Collections\nEver wanted to review and look back at saved words for practice?\n You now can via your Decks screen.");

                FancyShowCaseQueue tutorialQueue = buildMainTutorialQueue(
            streakPrompt, roadmapPrompt, lessonPrompt, 
            scanPrompt, roadmapNavPrompt, decksPrompt);

                tutorialQueue.setCompleteListener(this::transitionToDecksActivity);
        tutorialQueue.show();
    }

        public void continueInDecksScreen() {
                View savedWordsButton = currentActivity.findViewById(R.id.saved_words_btn);
        View collectionsButton = currentActivity.findViewById(R.id.collections_btn);
        View lookupButton = currentActivity.findViewById(R.id.lookup_btn);

                FancyShowCaseView savedWordsPrompt = createTutorialPrompt(savedWordsButton,
                "Saved Words\n Words that you have saved earlier in your studies, via look up/ reviews will appear in here.");

        FancyShowCaseView collectionsPrompt = createTutorialPrompt(collectionsButton,
                "Collections\nMaintain comprehension through retention!\nGroup words into custom lists to review at a later time.");

        FancyShowCaseView lookupPrompt = createTutorialPrompt(lookupButton,
                "Look up \nQuickly search for any word in the dictionary.");

                FancyShowCaseQueue decksQueue = new FancyShowCaseQueue()
                .add(savedWordsPrompt)
                .add(collectionsPrompt)
                .add(lookupPrompt);

                decksQueue.setCompleteListener(this::completeTutorial);
        decksQueue.show();
    }


        private View getSecondLessonSection(View lessonContainer) {
        if (lessonContainer instanceof LinearLayout) {
            LinearLayout container = (LinearLayout) lessonContainer;
            if (container.getChildCount() > 1) {
                return container.getChildAt(1);
            }
        }
        return null;
    }
    
        private FancyShowCaseQueue buildMainTutorialQueue(FancyShowCaseView... prompts) {
        FancyShowCaseQueue tutorialQueue = new FancyShowCaseQueue();
        
        for (FancyShowCaseView prompt : prompts) {
            if (prompt != null) {
                tutorialQueue.add(prompt);
            }
        }
        
        return tutorialQueue;
    }
    
        private void transitionToDecksActivity() {
        Intent decksIntent = new Intent(currentActivity, DecksActivity.class);
        decksIntent.putExtra("start_decks_tutorial", true);
        currentActivity.startActivity(decksIntent);
    }
    
        private void completeTutorial() {
        Toast.makeText(currentActivity, "Enjoy your learning journey!", Toast.LENGTH_SHORT).show();
        markTutorialAsCompleted();
                Intent mainIntent = new Intent(currentActivity, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        currentActivity.startActivity(mainIntent);
        currentActivity.finish();
    }


        private FancyShowCaseView createTutorialPrompt(View targetView, String messageText) {
        if (targetView == null) return null;

                String finalMessage = shouldApplyExtraSpacing(targetView, messageText) ?
            POPUP_SPACING + messageText : messageText;

        return new FancyShowCaseView.Builder(currentActivity)
                .focusOn(targetView)
                .fitSystemWindows(true)                  .title(finalMessage)
                .closeOnTouch(true)
                .build();
    }
    
        private boolean shouldApplyExtraSpacing(View targetView, String messageText) {
        int viewId = targetView.getId();
        
                return viewId == R.id.lesson_container ||
               viewId == R.id.saved_words_btn || 
               viewId == R.id.collections_btn || 
               viewId == R.id.lookup_btn || 
               messageText.contains("Pick what to learn");
    }


        private void markTutorialAsCompleted() {
        SharedPreferences tutorialPrefs = currentActivity.getSharedPreferences(TUTORIAL_PREFS, MODE_PRIVATE);
        tutorialPrefs.edit().putBoolean(TUTORIAL_COMPLETED_KEY, true).apply();
    }
    
        public static boolean isTutorialCompleted(Activity activity) {
        SharedPreferences tutorialPrefs = activity.getSharedPreferences(TUTORIAL_PREFS, MODE_PRIVATE);
        return tutorialPrefs.getBoolean(TUTORIAL_COMPLETED_KEY, false);
    }
    
        public static void resetTutorial(Activity activity) {
        SharedPreferences tutorialPrefs = activity.getSharedPreferences(TUTORIAL_PREFS, MODE_PRIVATE);
        tutorialPrefs.edit().putBoolean(TUTORIAL_COMPLETED_KEY, false).apply();
    }
}