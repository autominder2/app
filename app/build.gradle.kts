import java.util.Properties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.baselineprofile)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

// Expertise: Auto-incrementing version code based on timestamp (YYMMDDHH)
// ensures unique codes for every build without manual intervention.
val timestampVersionCode: Int = LocalDateTime.now()
    .format(DateTimeFormatter.ofPattern("yyMMddHH")).toInt()

android {
    namespace  = "com.autominder.app"
    compileSdk = 36

    defaultConfig {
        applicationId          = "com.autominder.app"
        minSdk                 = 26
        targetSdk              = 36
        versionCode            = timestampVersionCode
        versionName            = "1.0.0"
        testInstrumentationRunner = "com.autominder.app.AutoMinderTestRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental",    "true")
        }
    }

    androidResources {
        localeFilters += listOf("en", "es", "pt-rBR")
    }

    // Room schema JSONs available to instrumented migration tests
    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
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
            // Expertise: Fallback to official Google Test IDs if local DEBUG_ADMOB_ID is missing
            val debugId = localProps.getProperty("DEBUG_ADMOB_ID") ?: "ca-app-pub-3940256099942544~3347511713"
            resValue("string", "admob_app_id", debugId)
            resValue("string", "admob_banner_id",                "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "admob_interstitial_id",          "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_rewarded_id",              "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "admob_rewarded_interstitial_id", "ca-app-pub-3940256099942544/5354046379")
            buildConfigField("Boolean", "ENABLE_ADS", "false")
            isDebuggable   = true
            isMinifyEnabled = false
        }
        release {
            // Accept either the RELEASE_ADMOB_ID convention or the legacy
            // ADMOB_APP_ID key so whichever local.properties/CI already provides works.
            val releaseAppId = System.getenv("RELEASE_ADMOB_ID")
                ?: localProps.getProperty("RELEASE_ADMOB_ID")
                ?: System.getenv("ADMOB_APP_ID")
                ?: localProps.getProperty("ADMOB_APP_ID")
                ?: "PLACEHOLDER"
            val admobBannerId           = System.getenv("ADMOB_BANNER_ID")           ?: localProps.getProperty("ADMOB_BANNER_ID")           ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"
            val admobInterstitialId     = System.getenv("ADMOB_INTERSTITIAL_ID")     ?: localProps.getProperty("ADMOB_INTERSTITIAL_ID")     ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"
            val admobRewardedId         = System.getenv("ADMOB_REWARDED_ID")         ?: localProps.getProperty("ADMOB_REWARDED_ID")         ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"
            val admobRewardedInterstitialId = System.getenv("ADMOB_REWARDED_INTERSTITIAL_ID") ?: localProps.getProperty("ADMOB_REWARDED_INTERSTITIAL_ID") ?: "ca-app-pub-PLACEHOLDER/PLACEHOLDER"

            resValue("string", "admob_app_id",                   releaseAppId)
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

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = true
        baseline = file("lint-baseline.xml")
        // GradleDependency / NewerVersionAvailable / AndroidGradlePluginVersion are
        // time-sensitive advisories that turn red the moment Google publishes a new
        // release — they'd break CI on days we changed no code. The stack is
        // intentionally pinned (see CLAUDE.md), so upgrades are a deliberate manual
        // decision, not a build gate. Real code issues still fail via warningsAsErrors.
        disable += listOf(
            "TypographyFractions", "TypographyQuotes",
            "GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion"
        )
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
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            keepDebugSymbols += listOf("*/armeabi-v7a/*.so", "*/arm64-v8a/*.so", "*/x86/*.so", "*/x86_64/*.so")
        }
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
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // ─── Room ───────────────────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ─── Navigation + Serialization ─────────────────────────────────────────
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // ─── Background + Storage ───────────────────────────────────────────────
    implementation(libs.workmanager)
    implementation(libs.datastore.preferences)

    // ─── Lifecycle ──────────────────────────────────────────────────────────
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.process)

    // ─── UI Utilities ───────────────────────────────────────────────────────
    implementation(libs.splashscreen.core)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.coil.compose)

    // ─── Ads + Billing ──────────────────────────────────────────────────────
    implementation(libs.admob)
    implementation(libs.ump)
    implementation(libs.billing)

    // ─── Firebase + Play Store ──────────────────────────────────────────────
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)
    implementation(libs.play.review)
    implementation(libs.play.app.update)

    // ─── Stability + Performance ────────────────────────────────────────────
    debugImplementation(libs.leakcanary)
    implementation(libs.profileinstaller)
    implementation(libs.timber)

    // ─── Test ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.room.testing)
}

// Production Safety Gate — fail release builds if no real AdMob app ID is available.
tasks.matching { it.name.contains("Release") && (it.name.startsWith("assemble") || it.name.startsWith("bundle")) }.configureEach {
    doFirst {
        val releaseId = System.getenv("RELEASE_ADMOB_ID")
            ?: localProps.getProperty("RELEASE_ADMOB_ID")
            ?: System.getenv("ADMOB_APP_ID")
            ?: localProps.getProperty("ADMOB_APP_ID")
        if (releaseId.isNullOrBlank() || releaseId == "PLACEHOLDER") {
            throw GradleException("FATAL: No AdMob app ID found. Set RELEASE_ADMOB_ID (or ADMOB_APP_ID) in local.properties or the CI environment before a production build.")
        }
    }
}
