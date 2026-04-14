import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)    // REQUIRED for NavRoutes @Serializable
    alias(libs.plugins.ksp)                     // NOT kapt — Room 2.7+ requires KSP
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)         // Firebase / AdMob
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace  = "com.autominder.app"
    compileSdk = 36

    defaultConfig {
        applicationId          = "com.autominder.app"
        minSdk                 = 26
        targetSdk              = 36
        versionCode            = 1
        versionName            = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental",    "true")
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = localProps.getProperty("KEYSTORE_PATH") ?: ""
            if (keystorePath.isNotEmpty()) {
                storeFile     = file(keystorePath)
                storePassword = localProps.getProperty("KEYSTORE_STORE_PASSWORD")
                keyAlias      = localProps.getProperty("KEYSTORE_KEY_ALIAS")
                keyPassword   = localProps.getProperty("KEYSTORE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // Google's official test IDs — safe to hardcode in debug only
            resValue("string", "admob_app_id",                   "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_banner_id",                "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "admob_interstitial_id",          "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_rewarded_id",              "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "admob_rewarded_interstitial_id", "ca-app-pub-3940256099942544/5354046379")
            buildConfigField("Boolean", "ENABLE_ADS", "false")
            isDebuggable   = true
            isMinifyEnabled = false
        }
        release {
            val admobAppId              = System.getenv("ADMOB_APP_ID")              ?: localProps.getProperty("ADMOB_APP_ID")              ?: "ca-app-pub-PLACEHOLDER~PLACEHOLDER"
            val admobBannerId           = System.getenv("ADMOB_BANNER_ID")           ?: localProps.getProperty("ADMOB_BANNER_ID")           ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"
            val admobInterstitialId     = System.getenv("ADMOB_INTERSTITIAL_ID")     ?: localProps.getProperty("ADMOB_INTERSTITIAL_ID")     ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"
            val admobRewardedId         = System.getenv("ADMOB_REWARDED_ID")         ?: localProps.getProperty("ADMOB_REWARDED_ID")         ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"
            val admobRewardedInterstitialId = System.getenv("ADMOB_REWARDED_INTERSTITIAL_ID") ?: localProps.getProperty("ADMOB_REWARDED_INTERSTITIAL_ID") ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"
            resValue("string", "admob_app_id",                   admobAppId)
            resValue("string", "admob_banner_id",                admobBannerId)
            resValue("string", "admob_interstitial_id",          admobInterstitialId)
            resValue("string", "admob_rewarded_id",              admobRewardedId)
            resValue("string", "admob_rewarded_interstitial_id", admobRewardedInterstitialId)
            buildConfigField("Boolean", "ENABLE_ADS", "true")
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose     = true
        buildConfig = true
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // ─── Compose ────────────────────────────────────────────────────────────
    implementation(libs.compose.activity)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // ─── Hilt DI ────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)                    // KSP — never kapt
    implementation(libs.hilt.navigation.compose) // Required for hiltViewModel()
    implementation(libs.hilt.work)               // Required for @HiltWorker
    ksp(libs.androidx.hilt.compiler)             // Required for @HiltWorker KSP

    // ─── Room ───────────────────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)                    // KSP — never kapt

    // ─── Navigation ─────────────────────────────────────────────────────────
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json) // Required for type-safe routes

    // ─── Background + Storage ───────────────────────────────────────────────
    implementation(libs.workmanager)
    implementation(libs.datastore.preferences)

    // ─── Lifecycle ──────────────────────────────────────────────────────────
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)

    // ─── Splash Screen ──────────────────────────────────────────────────────
    implementation(libs.splashscreen.core)

    // ─── Glance (home screen widget) ────────────────────────────────────────────
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // ─── Image loading (Coil 2.x) ──────────────────────────────────────────
    implementation(libs.coil.compose)

    // ─── Ads + Billing ──────────────────────────────────────────────────────
    implementation(libs.admob)
    implementation(libs.billing)

    // ─── Utilities ──────────────────────────────────────────────────────────
    implementation(libs.timber)

    // ─── Test ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
}

// Fail the release build (not debug/compile) if AdMob IDs are missing
tasks.matching { it.name.contains("Release") && (it.name.startsWith("assemble") || it.name.startsWith("bundle")) }.configureEach {
    doFirst {
        val appId = System.getenv("ADMOB_APP_ID") ?: localProps.getProperty("ADMOB_APP_ID")
        val bannerId = System.getenv("ADMOB_BANNER_ID") ?: localProps.getProperty("ADMOB_BANNER_ID")
        if (appId == null) throw GradleException("ADMOB_APP_ID not set — add it to local.properties or CI environment")
        if (bannerId == null) throw GradleException("ADMOB_BANNER_ID not set — add it to local.properties or CI environment")
    }
}
