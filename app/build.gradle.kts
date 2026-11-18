import java.text.SimpleDateFormat
import java.util.Date

// AGP 9 ma wbudowaną obsługę Kotlina, ale w wersji 2.2.0, za starej dla GeckoView 160
// (biblioteki Kotlin 2.4). Dlatego wskazujemy nowszy Kotlin 2.4.20.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
        // Kompilator Compose (ekran aplikacji na telefonie) w tej samej wersji co Kotlin
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.4.20")
    }
}

plugins {
    id("com.android.application") version "9.3.1"
}
apply(plugin = "org.jetbrains.kotlin.plugin.compose")

android {
    namespace = "app.youtubeauto"
    // GeckoView 160 wymaga kompilacji pod Android 37.2
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "app.youtubeauto"
        minSdk = 26
        targetSdk = 36
        versionCode = 26
        versionName = "0.2.6"
        // Data budowania do ekranu "O aplikacji"
        buildConfigField("String", "BUILD_STAMP", "\"${SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())}\"")

        // Tylko procesor 64-bit ARM (Samsung S24 Ultra), żeby APK był mniejszy
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.car.app:app:1.7.0")
    implementation("org.mozilla.geckoview:geckoview:160.0.20261104181552")

    // Ekran aplikacji na telefonie (menu jak w OnlyMusic)
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.10.1")
}

// Po każdym budowaniu kopia APK trafia do folderu głównego projektu
// jako TubeAUTO-<wersja>.apk (np. C:\TEMP\TubeAUTO\TubeAUTO-0.2.3.apk).
val copyApkToMainFolder by tasks.registering {
    val version = android.defaultConfig.versionName
    val src = layout.buildDirectory.file("outputs/apk/debug/TubeAUTO-debug.apk")
    val dst = rootDir.parentFile.resolve("TubeAUTO-$version.apk")
    doLast {
        src.get().asFile.copyTo(dst, overwrite = true)
        println("APK skopiowany: $dst")
    }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    finalizedBy(copyApkToMainFolder)
}
