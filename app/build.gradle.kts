plugins {
    id("com.android.application") version "8.2.0"
    id("org.jetbrains.kotlin.android") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
}

android {
    namespace = "com.skybox.tv"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildToolsVersion = "34.0.0"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("SKYBOX_KEYSTORE_PATH") ?: "android/keystore.jks"
            val keystorePassword = System.getenv("SKYBOX_KEYSTORE_PASSWORD") ?: "android"
            val keyAlias = "skybox-tv"
            val keyPassword = "skybox"

            storeFile = file(keystorePath)
            storePassword = keystorePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug builds without signing config
        }
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
    // Kotlin coroutines for Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // TDLib Android core library
    implementation("io.github.tdlib-android:core:0.1.1")
    // TDLib Android KTX extensions
    implementation("io.github.tdlib-android:ktx:0.1.1")

    // Room - local database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Media3 ExoPlayer - main playback library
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    // Media3 UI - pre-built player UI components
    implementation("androidx.media3:media3-ui:1.3.1")
    // Media3 DataSource - custom data source interface
    implementation("androidx.media3:media3-datasource:1.3.1")
    // Media3 Common - shared utilities
    implementation("androidx.media3:media3-common:1.3.1")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
}