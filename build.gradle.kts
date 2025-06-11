// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        // Add the Google Services plugin
//        classpath("com.google.gms:google-services:4.4.0")
//        // Other classpaths you might have
//        // classpath("com.android.tools.build:gradle:7.4.2")
//        // classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
//    }
//}
//
//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//    }
//}
