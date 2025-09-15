plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.naraumi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.naraumi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.litert.api)
    implementation(libs.litert)
    implementation(libs.litert.support.api)
    implementation(libs.androidx.animation.graphics.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.text.recognition) // ML Kit Text Recognition
    implementation(libs.text.recognition.japanese)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.androidx.cardview)
    //implementation(libs.flexbox)
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.github.faruktoptas:FancyShowCaseView:1.4.0")

    // coroutines for async operations
    implementation(libs.kotlinx.coroutines.android)

   
}