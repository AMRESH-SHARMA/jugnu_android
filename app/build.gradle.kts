import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val agoraAppId: String =
    (project.findProperty("AGORA_APP_ID") as String?)
        ?: localProperties.getProperty("AGORA_APP_ID")
        ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinAndroidKsp)
    alias(libs.plugins.hiltAndroid)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.amresh.helium"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "11.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "AGORA_APP_ID",
            "\"$agoraAppId\""
        )
    }

    buildTypes {
        release {
            //TODO
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.volley)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.foundation)
    implementation(libs.ui)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.navigation:navigation-compose:2.9.6")
    // Jetpack Compose Pager (official)
    implementation("androidx.compose.foundation:foundation:1.9.5")
    // Coil for images
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Material icons
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Hilt navigation compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // OkHttp latest
//    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // Agora SDK
    // RTM (signaling)
    implementation("io.agora:agora-rtm-lite:2.2.2")
    implementation("io.agora.rtc:full-sdk:4.6.1")

    //    implementation("io.agora.rtc:full-sdk:4.2.6")
//    implementation("io.agora.rtm:rtm-sdk:2.2.6")
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    //cryptic user pref
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}