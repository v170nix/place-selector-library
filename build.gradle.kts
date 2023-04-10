// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    id("com.android.application") version "7.4.2" apply false
//    id("com.android.library") version "7.4.2" apply false
//    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
//}

buildscript {
    val compose_version by extra("1.4.1")
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.45")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}