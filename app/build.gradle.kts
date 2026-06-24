plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.gfxtool.roblox"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gfxtool.roblox"
        minSdk = 29   // Android 10
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }

    // ── Đặt tên file APK output: RBLX-GFX-Tool-v<version>-<buildType>.apk ──
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val buildType = variant.buildType.name   // "release" | "debug"
                val version   = variant.versionName      // "1.0.0"
                output.outputFileName = "RBLX-GFX-Tool-v${version}-${buildType}.apk"
            }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.datastore)
    implementation(libs.gson)

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.code.gson:gson:2.11.0")
}
