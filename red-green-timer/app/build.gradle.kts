plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.redgreen.timer"
    compileSdk = 35

    defaultConfig {
        // Change this to something unique to you before publishing (e.g. com.yourname.redgreentimer).
        applicationId = "com.redgreen.timer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

android {
    packaging {
        resources {
            excludes += setOf("kotlin/**", "META-INF/**", "DebugProbesKt.bin", "kotlin-tooling-metadata.json")
        }
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}
