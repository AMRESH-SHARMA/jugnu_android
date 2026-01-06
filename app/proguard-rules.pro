# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#Prevent code obfuscation
# -----------------------------
# Project-specific ProGuard rules
# -----------------------------

# Preserve line numbers in crash reports (optional but useful)
-keepattributes SourceFile,LineNumberTable

# -----------------------------
# Avoid obfuscating core app classes
# -----------------------------

# Keep your Application class (update if name changes)
-keep class com.example.app.MyApp { *; }

# -----------------------------
# Agora SDK
# -----------------------------
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# -----------------------------
# Hilt / Dagger Dependency Injection
# -----------------------------
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class androidx.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class **_Hilt* { *; }

-dontwarn dagger.**
-dontwarn javax.inject.**
-dontwarn androidx.hilt.**

# ---- Hilt generated components & entrypoints ----
-keep class dagger.hilt.internal.** { *; }
-keep class dagger.hilt.android.internal.** { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedEntryPoint { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }
-keep class dagger.hilt.android.internal.managers.** { *; }

# Keep all _HiltModules and aggregated dependencies
-keep class **_HiltModules_* { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# -----------------------------
# Firebase (Analytics + Messaging)
# -----------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# -----------------------------
# Retrofit + Gson
# -----------------------------
-keepattributes Signature,RuntimeVisibleAnnotations

-keep class com.google.gson.stream.** { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }

# -----------------------------
# Kotlinx Serialization
# -----------------------------
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# -----------------------------
# Desugaring support (fix broken dontwarn)
# -----------------------------
-dontwarn com.google.devtools.build.android.desugar.runtime.ThrowableExtension
