package com.example.naraumi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.*;

public class ReadAndPointActivity extends BaseActivity {
    private static final String TAG = "ReadAndPointActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private PreviewView previewView;
    private Interpreter tflite;
    private TextView targetWordView, tickView, progressView, hiraganaView;
    private TextView nextButton, skipButton;

    private Map<Integer, String> labelMap;
    private List<TargetObject> targetObjects;
    private String currentTargetWord;
    private boolean isDetectionEnabled = true;

    private int currentIndex = 0;
    private int foundCount = 0;
    private String lessonType;
    private int basicsCircleIndex;

    private long lastInferenceTime = 0;
    private static final long INFERENCE_INTERVAL_MS = 500; // run it every 500ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_point);

                lessonType = getIntent().getStringExtra("LESSON_TYPE");
        if (lessonType == null || lessonType.isEmpty()) {
            lessonType = "BASICS";         }
        basicsCircleIndex = getIntent().getIntExtra("BASICS_CIRCLE_INDEX", 1);

                TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText("READ & POINT");
        }

        initializeViews();
        loadModel();
        loadLabelMapAndTargets();
        setupClickListeners();
        setTargetWord();

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        targetWordView = findViewById(R.id.targetWordView);
        hiraganaView = findViewById(R.id.hiraganaView);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);
        tickView = findViewById(R.id.tickView);
        progressView = findViewById(R.id.read_point_progress);

                if (previewView == null || targetWordView == null || nextButton == null ||
                skipButton == null || tickView == null || progressView == null || hiraganaView == null) {
            Log.e(TAG, "One or more views not found in layout");
            Toast.makeText(this, "Layout error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tickView.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        nextButton.setOnClickListener(v -> {
            if (tickView.getVisibility() == View.VISIBLE) {
                foundCount++;
                Log.d(TAG, "Correct object found. Found count: " + foundCount);
            }
            advanceToNextTarget();
        });

        skipButton.setOnClickListener(v -> {
            Log.d(TAG, "Skipping object. Found count remains: " + foundCount);
            advanceToNextTarget();
        });
    }

    private void advanceToNextTarget() {
        currentIndex++;
        if (currentIndex >= targetObjects.size()) {
            showCompletionScreen();
            return;
        }
        isDetectionEnabled = true;
        tickView.setVisibility(View.GONE);
        setTargetWord();
    }

    private void showCompletionScreen() {
        Log.d(TAG, "Showing completion screen with " + foundCount + "/" + targetObjects.size() + " found");

        String lessonKey = LessonProgressManager.createLessonKey(lessonType, "", "READ_POINT");
        LessonProgressManager.markLessonCompleted(this, lessonKey);
        ActivityRefreshManager.setRefreshNeeded();
        StreakManager.updateStreak(this);

        Intent intent = new Intent(this, KanaCompletionActivity.class);
        intent.putExtra("SOURCE", "READ_POINT");
        intent.putExtra("KANA_TYPE", lessonType);
        intent.putExtra("KANA_GROUP", "");
        intent.putExtra("LEARNED_COUNT", foundCount);
        intent.putExtra("WRITE_TOTAL", targetObjects.size());
        intent.putExtra("PHRASE_MODE", true);
        intent.putExtra("BASICS_CIRCLE_INDEX", basicsCircleIndex);

        startActivity(intent);
        finish();
    }


    private void loadModel() {
        try {
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, "YOLOv8-Detection.tflite");
            tflite = new Interpreter(tfliteModel);
            Log.d(TAG, "Model loaded successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error loading model: " + e.getMessage());
            Toast.makeText(this, "Model load error - some features may not work", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLabelMapAndTargets() {
        labelMap = new HashMap<>();
        targetObjects = new ArrayList<>();

        try {
            String[] labels = getResources().getStringArray(R.array.coco_labels_ja);
            for (int i = 0; i < labels.length; i++) {
                labelMap.put(i, labels[i]);
            }
            Log.d(TAG, "Loaded " + labels.length + " labels from resources.");
        } catch (Exception e) {
            Log.e(TAG, "Error loading labels from resources: " + e.getMessage());
        }
        
        targetObjects = createTargetsForLesson(lessonType);
        if (targetObjects.isEmpty()) {
            Log.e(TAG, "No targets created for lesson type: " + lessonType);
            Toast.makeText(this, "Error: No targets for this lesson.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Log.d(TAG, "Created " + targetObjects.size() + " targets for lesson: " + lessonType);
        }
    }

    private List<TargetObject> createTargetsForLesson(String type) {
        List<TargetObject> objects = new ArrayList<>();
        switch (type) {
            case "BASICS":
                objects.add(new TargetObject("マウス", "まうす"));
                objects.add(new TargetObject("キーボード", "きーぼーど"));
                objects.add(new TargetObject("テレビ", "てれび"));
                break;
            case "BASICS 2":
                objects.add(new TargetObject("鋏", "はさみ"));
                objects.add(new TargetObject("時計", "とけい"));
                objects.add(new TargetObject("ボトル", "ぼとる"));
                break;
            case "ADVANCED 1":
                objects.add(new TargetObject("バナナ", "ばなな"));
                objects.add(new TargetObject("りんご", "りんご"));
                objects.add(new TargetObject("カップ", "かっぷ"));
                objects.add(new TargetObject("フォーク", "ふぉーく"));
                break;
            case "ADVANCED 2":
                objects.add(new TargetObject("椅子", "いす"));
                objects.add(new TargetObject("ソファ", "そふぁ"));
                objects.add(new TargetObject("花瓶", "かびん"));
                objects.add(new TargetObject("時計", "とけい"));
                break;
            default:
                Log.w(TAG, "Unknown lesson type '" + type + "', defaulting to BASICS.");
                objects.add(new TargetObject("マウス", "まうす"));
                objects.add(new TargetObject("キーボード", "きーぼーど"));
                objects.add(new TargetObject("テレビ", "てれび"));
                break;
        }
        return objects;
    }

    private void setTargetWord() {
        if (targetObjects.isEmpty() || currentIndex >= targetObjects.size()) {
            Log.w(TAG, "No target words available or index out of bounds.");
            return;
        }

        TargetObject currentTarget = targetObjects.get(currentIndex);
        currentTargetWord = currentTarget.japanese;
        runOnUiThread(() -> {
            targetWordView.setText(currentTarget.japanese);
            hiraganaView.setText(currentTarget.hiragana);
            hiraganaView.setVisibility(currentTarget.hiragana.isEmpty() ? View.GONE : View.VISIBLE);
            tickView.setVisibility(View.GONE);
            progressView.setText("Object " + (currentIndex + 1) + " of " + targetObjects.size());
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Camera start failed: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image == null) return null;

        try {
            int width = image.getWidth();
            int height = image.getHeight();

            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            buffer.rewind();

            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            int adjustedWidth = width + rowPadding / pixelStride;

            Bitmap bitmap = Bitmap.createBitmap(adjustedWidth, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            Matrix matrix = new Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }

            return rotatedBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Bitmap conversion failed: " + e.getMessage());
            return null;
        }
    }

    private void detectObjects(Bitmap bitmap) {
        if (bitmap == null || tflite == null || !isDetectionEnabled) return;

        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);
            ByteBuffer rgbBuffer = ByteBuffer.allocateDirect(640 * 640 * 3 * 4).order(ByteOrder.nativeOrder());

            int[] pixels = new int[640 * 640];
            resizedBitmap.getPixels(pixels, 0, 640, 0, 0, 640, 640);
            for (int pixel : pixels) {
                rgbBuffer.putFloat(((pixel >> 16) & 0xFF) / 255f);
                rgbBuffer.putFloat(((pixel >> 8) & 0xFF) / 255f);
                rgbBuffer.putFloat((pixel & 0xFF) / 255f);
            }

            float[][][] outputBoxes = new float[1][8400][4];
            float[][] outputScores = new float[1][8400];
            float[][] outputClasses = new float[1][8400];

            Object[] inputs = {rgbBuffer};
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, outputBoxes);
            outputs.put(1, outputScores);
            outputs.put(2, outputClasses);

                        long startTime = System.currentTimeMillis();

            tflite.runForMultipleInputsOutputs(inputs, outputs);

                        long endTime = System.currentTimeMillis();
            long inferenceTime = endTime - startTime;
            float fps = 1000f / inferenceTime;
            Log.d(TAG, "YOLOv8 Inference Time: " + inferenceTime + "ms | FPS: " + fps);

            for (int i = 0; i < 8400; i++) {
                if (outputScores[0][i] > 0.8) {
                    int classId = (int) outputClasses[0][i];
                    String label = labelMap.get(classId);
                    if (label != null && label.equals(currentTargetWord)) {
                        isDetectionEnabled = false;
                        updateUI(true);
                        return;
                    }
                }
            }

            updateUI(false);
        } catch (Exception e) {
            Log.e(TAG, "Error during object detection: " + e.getMessage());
                    }
    }


    private void updateUI(boolean isCorrect) {
        runOnUiThread(() -> {
            if (tickView != null) {
                tickView.setVisibility(isCorrect ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        try {
            cameraProvider.unbindAll();

            if (previewView == null) {
                Log.e(TAG, "PreviewView is null, cannot bind camera");
                return;
            }

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build();

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
                try {
                    long currentTime = System.currentTimeMillis();

                    if (isDetectionEnabled && (currentTime - lastInferenceTime >= INFERENCE_INTERVAL_MS)) {
                        lastInferenceTime = currentTime;

                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
                        if (bitmap != null) {
                            detectObjects(bitmap);
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error in image analysis: " + e.getMessage());
                } finally {
                    imageProxy.close();
                }
            });

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            Log.d(TAG, "Camera bound successfully");
        } catch (Exception e) {
            Log.e(TAG, "Camera binding failed: " + e.getMessage());
            Toast.makeText(this, "Camera setup failed", Toast.LENGTH_SHORT).show();
        }
    }
    
        private static class TargetObject {
        final String japanese, hiragana;

        TargetObject(String japanese, String hiragana) {
            this.japanese = japanese;
            this.hiragana = hiragana;
        }
    }
}