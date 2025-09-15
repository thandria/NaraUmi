package com.example.naraumi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class TTSHelper {
    private static final String TAG = "TTSHelper";
    private TextToSpeech textToSpeech;
    private Context context;
    private boolean isJapaneseAvailable = false;
    private TTSReadyListener readyListener;

    public interface TTSReadyListener {
        void onTTSReady(boolean japaneseAvailable);
        void onTTSError();
    }

    public TTSHelper(Context context, TTSReadyListener listener) {
        this.context = context;
        this.readyListener = listener;
        initialiseTTS();
    }

    private void initialiseTTS() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                checkJapaneseSupport();
            } else {
                Log.e(TAG, "TTS initialization failed");
                showTTSInstallDialog();
                if (readyListener != null) {
                    readyListener.onTTSError();
                }
            }
        });
    }

    private void checkJapaneseSupport() {
        int result = textToSpeech.setLanguage(Locale.JAPANESE);

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Japanese language not supported or missing data");
            isJapaneseAvailable = false;
            showJapaneseInstallDialog();
            if (readyListener != null) {
                readyListener.onTTSReady(false);
            }
        } else {
            Log.i(TAG, "Japanese TTS is available");
            isJapaneseAvailable = true;


            if (result == TextToSpeech.LANG_AVAILABLE) {
                Log.i(TAG, "Found Japanese locale: " + textToSpeech.getLanguage());
            }

            if (readyListener != null) {
                readyListener.onTTSReady(true);
            }
        }
    }

    private void showTTSInstallDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Text to Speech is needed!")
                .setMessage("This app needs Text to Speech installed Japanese word pronunciation. Would you like to install it?")
                .setPositiveButton("Install TTS", (dialog, which) -> installTTS())
                .setNegativeButton("No, thank you", (dialog, which) -> {
                    Toast.makeText(context, "Text to speech is unavailable.", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    private void showJapaneseInstallDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Japanese Voice Required")
                .setMessage("Japanese text-to-speech voice is not installed. This is needed to hear Japanese pronunciation.\n\nWould you like to install it?")
                .setPositiveButton("Install Japanese Voice", (dialog, which) -> installJapaneseVoice())
                .setNeutralButton("Open TTS Settings", (dialog, which) -> openTTSSettings())
                .setNegativeButton("Continue Without Audio", (dialog, which) -> {
                    Toast.makeText(context, "Japanese audio disabled - install Japanese TTS voice to enable", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    private void installTTS() {
        try {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            context.startActivity(installIntent);
        } catch (Exception e) {
            Log.w(TAG, "Could not launch TTS installer", e);
            openPlayStoreForTTS();
        }
    }

    private void installJapaneseVoice() {

                try {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            installIntent.putExtra("language", "ja");
            installIntent.putExtra("country", "JP");
            context.startActivity(installIntent);

            Toast.makeText(context, "Scroll down and select Japanese from the list", Toast.LENGTH_LONG).show();
            return;
        } catch (Exception e) {
            Log.w(TAG, "Direct Japanese TTS install failed, trying alternatives");
        }

                openTTSSettings();
    }

    private void openTTSSettings() {
        try {
                        Intent settingsIntent = new Intent();
            settingsIntent.setAction("com.android.settings.TTS_SETTINGS");
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);

                        showTTSInstallSteps();
        } catch (Exception e) {
            Log.w(TAG, "Could not open TTS settings, trying Google TTS settings");
            try {
                                Intent googleTTSIntent = new Intent();
                googleTTSIntent.setAction("android.intent.action.MAIN");
                googleTTSIntent.setClassName("com.google.android.tts", "com.google.android.apps.speech.tts.googletts.settings.GoogleTTSSettingsActivity");
                context.startActivity(googleTTSIntent);

                showTTSInstallSteps();
            } catch (Exception ex) {
                Log.w(TAG, "Could not open Google TTS settings, opening Play Store");
                openPlayStoreForTTS();
            }
        }
    }

    private void showTTSInstallSteps() {
        new AlertDialog.Builder(context)
                //popup title and instructions
                .setTitle("Steps for installing the Japanese voice")
                .setMessage(
                        "\n\nIn TTS settings:\n" +
                        "1) Tap on Google Text-to-Speech Engine\n" +
                        "2) Then tap on the Settings icon ️\n" +
                        "3) Tap on Install voice data'\n" +
                        "4) Scroll down and select Japanese'\n" +
                        "5) Pick a voice, wait for it to download\n" +
                        "6) Return back to the app\n\n" +
                        "Otherwise follow this navigation order:\n" +
                        "System Settings -> Language and Input -> Text-to-Speech → Add Japanese")
                .setPositiveButton("Understood!", null)
                .setNeutralButton("Download from Play Store instead", (dialog, which) -> openPlayStoreForTTS())
                .show();
    }

    private void openPlayStoreForTTS() {
        try {
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
            // Open playstore to google tts app.
            playStoreIntent.setData(Uri.parse("market://details?id=com.google.android.tts"));
            playStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(playStoreIntent);

            Toast.makeText(context, "Download Google TTS, then add Japanese voice", Toast.LENGTH_LONG).show();
        } catch (Exception e) {

            try {
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                // If it cant open google tts playstore page.
                webIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts"));
                context.startActivity(webIntent);

                Toast.makeText(context, "Install Google TTS from the browser", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                // If that fails
                Toast.makeText(context, "Please manually install Japanese TTS from device settings", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void speak(String text) {
        if (textToSpeech != null && isJapaneseAvailable) {
            textToSpeech.stop();
            int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            if (result == TextToSpeech.ERROR) {
                Log.e(TAG, "TTS speak failed");
                Toast.makeText(context, "Speech is not working. Check TTS settings.", Toast.LENGTH_SHORT).show();
                                showTTSSetupSteps();
            }
        } else {
            Log.w(TAG, "Japanese TTS not found");
            Toast.makeText(context, "A Japanese voice could not be found.", Toast.LENGTH_SHORT).show();

            // Ask if theyd like to install again.
            showJapaneseInstallDialog();
        }
    }

        public void showTTSSetupSteps() {
        if (!isJapaneseAvailable) {
            showJapaneseInstallDialog();
        } else {
                        showTTSInstallSteps();
        }
    }

    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
} 