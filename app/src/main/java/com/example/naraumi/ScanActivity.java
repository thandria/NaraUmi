package com.example.naraumi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;

import java.io.IOException;
import java.util.Arrays;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private boolean hasLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
                     }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasLaunched) {
            Log.d("ScanActivity", "onResume called again, finishing to avoid ghost screen.");
            finish();
            return;
        }

        hasLaunched = true;
        checkCameraPermissionAndOpen();
    }

    private void checkCameraPermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openRearCamera();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        } else {
            openRearCamera();         }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openRearCamera();
            } else {
                Toast.makeText(this, "Camera permission is required for scanning.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void openRearCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_BACK);

        if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            Toast.makeText(this, "Please make sure you're using your rear camera.", Toast.LENGTH_LONG).show();
        }

        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                processOCR(imageBitmap);
            } else {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        processOCR(bitmap);
                    } catch (IOException e) {
                        Log.e("OCR", "Error loading image", e);
                        finish();
                    }
                }
            }
        } else {
            Toast.makeText(this, "Camera operation canceled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processOCR(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());

        recognizer.process(image)
                .addOnSuccessListener(result -> {
                    String detectedText = result.getText();
                    Log.d("OCR", "Raw actual detected text: " + detectedText);

                    String filteredText = detectedText.replaceAll("[^\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}]", " ").trim();
                    String[] words = filteredText.split("\\s+");

                    Log.d("OCR", "Filtered words: " + Arrays.toString(words));

                    if (words.length > 0 && !words[0].isEmpty()) {
                        Intent intent = new Intent(this, ResultActivity.class);
                        intent.putExtra("detected_words", words);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "No valid Japanese text detected.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("OCR", "Error: " + e.getMessage());
                    Toast.makeText(this, "OCR failed. Retry.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

        private void testOCRProcessing() {
        Log.d("ScanActivity", "Testing OCR processing...");
        
        String[] testTexts = {
            "可愛い",                  "猫",                      "こんにちは",              "ありがとう",              "本",                      "水"                   };
        
        for (String testText : testTexts) {
            Log.d("OCR_TEST", "Testing with: '" + testText + "'");
            
                        DatabaseHelper.DictionarySearchHelper searchHelper = new DatabaseHelper.DictionarySearchHelper(this);
            DatabaseHelper.DictionarySearchHelper.SearchResult result = searchHelper.searchWord(testText);
            
            if (result.isFound()) {
                Log.d("OCR_TEST", "SUCCESS: Found " + testText + " -> " + result.getKanji() + "/" + result.getKana());
            } else {
                Log.d("OCR_TEST", "FAILED: " + testText + " not found in dictionary");
            }
            
                        if (testText.equals("可愛い")) {
                String filteredText = testText.replaceAll("[^\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}]", " ").trim();
                String[] words = filteredText.split("\\s+");

                Log.d("OCR", "Filtered words: " + Arrays.toString(words));

                if (words.length > 0 && !words[0].isEmpty()) {
                    Intent intent = new Intent(this, ResultActivity.class);
                    intent.putExtra("detected_words", words);
                    startActivity(intent);
                }
                finish();
                return;
            }
        }
    }
}