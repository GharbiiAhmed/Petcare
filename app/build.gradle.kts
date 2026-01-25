plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") version "4.4.0" apply false
}

android {
    namespace = "tn.petcare_android"
    compileSdk = 36

    defaultConfig {
        applicationId = "tn.petcare_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read tokens from local.properties
        val properties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${properties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
        )

        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${properties.getProperty("MAPBOX_ACCESS_TOKEN", "")}\""
        )

        // Add Mapbox token to manifest
        manifestPlaceholders["MAPBOX_ACCESS_TOKEN"] = properties.getProperty("MAPBOX_ACCESS_TOKEN", "")
        
        ndk {
            // Specify ABIs to build for (arm64-v8a supports 16KB pages)
            abiFilters.clear()
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true  // Enable BuildConfig generation
    }
    
    packaging {
        jniLibs {
            // Set to true to match android:extractNativeLibs="true" in AndroidManifest.xml
            // This ensures proper 16 KB page size alignment for Android 15+ devices
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core library desugaring for java.time APIs on API < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Mapbox SDK - 11.0.0 with 16KB page size compatibility via useLegacyPackaging
    implementation("com.mapbox.maps:android:11.0.0")
    implementation("com.mapbox.extension:maps-compose:11.0.0")

    // Firebase - FCM Push Notifications (iOS Reference: FCMManager.swift)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Socket.IO - Real-time chat (iOS Reference: SocketManager.swift)
    implementation("io.socket:socket.io-client:2.1.0")
    
    // Stripe Android SDK for PaymentSheet
    // Official: https://github.com/stripe/stripe-android
    // Docs: https://docs.stripe.com/payments/accept-a-payment?platform=android&ui=payment-sheet
    implementation("com.stripe:stripe-android:21.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}