// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add ObjectBox plugin to the classpath so the app module can find it
        classpath("io.objectbox:objectbox-gradle-plugin:4.0.3")
    }
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains:annotations:13.0")
        }
    }
}

plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
    //id("org.jetbrains.kotlin.android") version "2.4.10" apply false
}