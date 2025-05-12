import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val localProps = Properties().apply {
    val propFile = rootProject.file("local.properties")
    if (propFile.exists()) {
        load(FileInputStream(propFile))
    }
}
val kakaoKey: String = localProps.getProperty("kakao.map.api.key") ?: ""

android {
    namespace = "com.a303.helpmet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.a303.helpmet"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "KAKAO_MAPS_API_KEY",
            "\"${kakaoKey}\""
        )
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // --- Core & UI ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.play.services.location)
    implementation(libs.firebase.crashlytics.buildtools)
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- DI ---
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("io.insert-koin:koin-android:4.0.2")
    implementation("io.insert-koin:koin-androidx-compose:4.0.2")

    // --- Networking (kotlinx-serialization 버전 채택 예) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // --- Local Data ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    // DataStore dependencies는 catalog(libs)에서 유지

    // --- Test ---
    testImplementation(libs.junit)

    // --- Kakao ---
    implementation("com.kakao.maps.open:android:2.12.8")
    implementation("com.kakao.sdk:v2-all:2.11.0")


    // --- tensorflow ---
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
}