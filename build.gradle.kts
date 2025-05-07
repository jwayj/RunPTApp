// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
// RunPTApp/build.gradle.kts (프로젝트 루트)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Android Gradle Plugin
        classpath("com.android.tools.build:gradle:8.1.2")
        // Firebase용 Google Services 플러그인
        classpath("com.google.gms:google-services:4.3.15")
    }
}
