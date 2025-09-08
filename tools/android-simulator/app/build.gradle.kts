plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.dexmon.simulator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dexmon.simulator"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
