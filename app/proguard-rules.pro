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
# Preserve Firebase and Google Play Services classes
#-keep class com.google.firebase.** { *; }
#-keep class com.google.android.gms.** { *; }
#
## Keep all Parcelable objects to prevent crashes
#-keep class * implements android.os.Parcelable {
#    public static final android.os.Parcelable$Creator *;
#}
#
## Keep classes that are used for serialization/deserialization
## Keep Gson SerializedName fields properly
#-keepattributes *Annotation*
#-keep class com.google.gson.** { *; }
#-keep class * {
#    @com.google.gson.annotations.SerializedName *;
#}
#
#
## Keep all classes that are annotated with @Keep
#-keep @androidx.annotation.Keep class * { *; }
#-keep @com.google.firebase.database.IgnoreExtraProperties class * { *; }
#
## Do not strip metadata used by Firebase
#-keepattributes Signature
#-keepattributes *Annotation*
#
## Keep classes required for Compose
#-keep class androidx.compose.runtime.** { *; }
#-keep class androidx.compose.ui.** { *; }
#-keep class androidx.lifecycle.** { *; }
#-keep class androidx.navigation.** { *; }
#
## Keep code required for Firebase Auth
#-keep class com.google.firebase.auth.** { *; }
#-keep class com.google.firebase.database.** { *; }
#-keep class com.google.firebase.firestore.** { *; }
#
## Keep code required for Firebase Performance
#-keep class com.google.firebase.perf.** { *; }
#
## Avoid issues with Reflection (such as Dagger, Hilt, etc.)
#-dontwarn javax.annotation.**
#-dontwarn dagger.**
#-dontwarn com.google.errorprone.annotations.**
#-dontwarn androidx.compose.animation.**
#
## Recommended rules for Kotlin reflection
#-keep class kotlin.Metadata { *; }
#-keep class kotlin.jvm.internal.** { *; }
#-keep class kotlin.reflect.** { *; }
#
## Keep Material Design and Jetpack Compose-related classes
#-keep class com.google.android.material.** { *; }
#-keep class androidx.compose.material3.** { *; }
#-keep class androidx.activity.compose.** { *; }
#
## Avoid code shrinking in specific Compose and Material APIs
#-keep class androidx.compose.ui.platform.** { *; }
#-keep class androidx.compose.foundation.** { *; }
#-keep class androidx.compose.material.** { *; }
#
## Prevent removing debug information in release builds
#-keepattributes InnerClasses
#-keepattributes EnclosingMethod
#
## General ProGuard optimizations
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-keepattributes Exceptions
#-keepclasseswithmembernames class * {
#    native <methods>;
# MPAndroidChart Proguard rules
-keep class com.github.mikephil.charting.** { *; }

# Apache POI Proguard rules
-keep class org.apache.poi.** { *; }

