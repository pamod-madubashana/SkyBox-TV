plugins {
    alias("com.android.application") apply true
    alias("org.jetbrains.kotlin.android") apply true
}

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildToolsVersion = "34.0.0"

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    defaultConfig {
        manifestPlaceholders = [leanbackLauncher: "@string/leanback_launcher"]
    }
}

dependencies {
    // AndroidX Bill (BOM) - manages compose versions
    implementation(platform("androidx.compose:compose-bom:2024.07.00"))
    // Android TV Foundation - core TV UI components
    implementation("androidx.tv:tv-foundation:1.3.0")
    // Android TV Material - Material Design for TV
    implementation("androidx.tv:tv-material:1.3.0")
    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    // Core KTX
    implementation("androidx.core:core-ktx:1.13.0")
    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
}