import java.util.Properties

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
    alias(libs.plugins.sentry)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

/**
 * versionCode is an explicit integer, checked in, bumped by hand per release.
 *
 * It used to be `LocalDateTime.now().format("yyMMddHH").toInt()`, whose comment
 * claimed it "ensures unique codes for every build". It did not, and all three
 * defects fire at submission time rather than during development:
 *
 *  1. Hour granularity — two builds in the same hour get the SAME code, so Play
 *     rejects the second as a duplicate. Shipping a fix 20 minutes later was
 *     blocked outright.
 *  2. Non-reproducible — the AAB tested was never the AAB uploaded, and no
 *     release could be rebuilt byte-identically afterwards.
 *  3. `LocalDateTime` is local time, not UTC — a timezone move or a DST
 *     rollback can emit a LOWER code than an earlier build, which Play refuses.
 *
 * The 27_000_000 floor is deliberate, not arbitrary. Play requires versionCode
 * to increase forever, and it is unknown whether a timestamp-era build was
 * already pushed to a track. The largest code the old yyMMddHH scheme could
 * emit in 2026 is 26123123 (yy=26, Dec 31, 23:00), so anything above 27_000_000
 * is guaranteed to outrank every build that scheme could have produced —
 * making this change safe without having to reconstruct the upload history.
 * Play's own ceiling is 2_100_000_000, so the headroom costs nothing.
 *
 * To bump: increment by 1 per uploaded build. Keep versionName semantic.
 *   1.0.0 -> 27_000_001   (first submission)
 *   1.0.1 -> 27_000_002
 *   1.1.0 -> 27_000_003
 */
val appVersionCode = 27_000_001

android {
    namespace  = "com.autominder.app"
    compileSdk = 36

    defaultConfig {
        applicationId          = "com.autominder.app"
        minSdk                 = 26
        targetSdk              = 36
        versionCode            = appVersionCode
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
            // Env vars first so CI supplies these from protected secrets and
            // never needs a local.properties on the runner.
            val keystorePath = System.getenv("KEYSTORE_PATH")
                ?: localProps.getProperty("KEYSTORE_PATH")
                ?: ""
            if (keystorePath.isNotEmpty()) {
                storeFile     = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_STORE_PASSWORD")
                    ?: localProps.getProperty("KEYSTORE_STORE_PASSWORD")
                keyAlias      = System.getenv("KEYSTORE_KEY_ALIAS")
                ?: localProps.getProperty("KEYSTORE_KEY_ALIAS")
                keyPassword   = System.getenv("KEYSTORE_KEY_PASSWORD")
                    ?: localProps.getProperty("KEYSTORE_KEY_PASSWORD")
            }
            // Deliberately NOT throwing here. This block is evaluated during
            // configuration for every build, debug included, so a throw would
            // make the project unbuildable on any machine without signing
            // material. The loud failure lives on the release tasks below,
            // where it is actually relevant. See gradle/RELEASE_INPUTS below.
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

    // ── Release input validation ───────────────────────────────────────────
    //
    // Both of the following used to degrade silently, which is the worst
    // possible behaviour for a release artifact:
    //
    //   * An absent KEYSTORE_PATH left `storeFile` unset while `signingConfig`
    //     was still assigned to the release build type, so `assembleRelease`
    //     emitted an UNSIGNED artifact rather than refusing.
    //   * Absent ad-unit ids fell through to the literal string "PLACEHOLDER",
    //     which compiles, ships, and then serves no ads in production.
    //
    // CLAUDE.md requires missing signing input to fail loudly. These checks run
    // in `doFirst`, so they fire only when a release artifact is actually being
    // produced — never during configuration, and never for a debug build.
    val requireReleaseInputs = {
        val keystorePath = System.getenv("KEYSTORE_PATH")
            ?: localProps.getProperty("KEYSTORE_PATH")
            ?: ""
        check(keystorePath.isNotEmpty()) {
            "Release signing is not configured. Set KEYSTORE_PATH (plus " +
                "KEYSTORE_STORE_PASSWORD, KEYSTORE_KEY_ALIAS, KEYSTORE_KEY_PASSWORD) " +
                "in an untracked local.properties, or as environment variables in CI. " +
                "Refusing to emit an unsigned release artifact."
        }
        check(file(keystorePath).exists()) {
            "KEYSTORE_PATH points at a file that does not exist. Refusing to " +
                "emit an unsigned release artifact. (Path not echoed: signing " +
                "material must never appear in build logs.)"
        }

        val adKeys = listOf(
            "RELEASE_ADMOB_ID" to "app id",
            "ADMOB_BANNER_ID" to "banner unit",
            "ADMOB_INTERSTITIAL_ID" to "interstitial unit",
            "ADMOB_REWARDED_ID" to "rewarded unit",
            "ADMOB_REWARDED_INTERSTITIAL_ID" to "rewarded interstitial unit"
        )
        val missingAds = adKeys.filter { (key, _) ->
            val v = System.getenv(key)
                ?: localProps.getProperty(key)
                ?: if (key == "RELEASE_ADMOB_ID") {
                    System.getenv("ADMOB_APP_ID") ?: localProps.getProperty("ADMOB_APP_ID")
                } else {
                    null
                }
            v.isNullOrBlank() || v.contains("PLACEHOLDER")
        }
        check(missingAds.isEmpty()) {
            "Release AdMob ids unresolved (would ship the literal PLACEHOLDER " +
                "and serve no ads): " + missingAds.joinToString { it.second } +
                ". Provide the corresponding keys via local.properties or CI env."
        }
    }

    tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }
        .configureEach { doFirst { requireReleaseInputs() } }

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
            "GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion",
            // ObsoleteSdkInt fires on mipmap-anydpi-v26/ but that qualifier is
            // structurally required by AAPT for adaptive-icon resolution — removing
            // it (or merging into mipmap-anydpi/) causes a BUILD FAILED because
            // AAPT can no longer locate mipmap/ic_launcher. False positive for this
            // specific resource folder type.
            "ObsoleteSdkInt"
        )
    }

    testOptions {
        unitTests {
            // Route-scoped ViewModels resolve their arguments through
            // SavedStateHandle.toRoute(), which is an inline reified function that
            // reaches android.os.Bundle. Without this, every such ViewModel is
            // untestable on the JVM and its save path can only be covered on a
            // device. Stubbed Android methods return defaults, so tests must assert
            // behaviour, never values that a real Bundle would have carried.
            isReturnDefaultValues = true
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

    // ─── Baseline Profile ───────────────────────────────────────────────────
    // Consumes the profile generated by :baselineprofile. Without this line the
    // baselineprofile plugin is applied but produces nothing — which is exactly
    // the state this project was in until 2026-08-16.
    baselineProfile(project(":baselineprofile"))

    // ─── Test ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.room.testing)
}

// Production Safety Gate — fail release builds if any real AdMob ID is missing.
// Covers the app ID AND all four ad unit IDs: the app ID alone passing this
// gate previously let a release ship with a PLACEHOLDER banner/interstitial/
// rewarded/rewarded-interstitial ID if just one CI secret or local.properties
// line was missing — a real ad slot silently requesting a malformed unit ID
// in production instead of failing the build loudly.
tasks.matching { it.name.contains("Release") && (it.name.startsWith("assemble") || it.name.startsWith("bundle")) }.configureEach {
    doFirst {
        val releaseId = System.getenv("RELEASE_ADMOB_ID")
            ?: localProps.getProperty("RELEASE_ADMOB_ID")
            ?: System.getenv("ADMOB_APP_ID")
            ?: localProps.getProperty("ADMOB_APP_ID")
        if (releaseId.isNullOrBlank() || releaseId == "PLACEHOLDER") {
            throw GradleException("FATAL: No AdMob app ID found. Set RELEASE_ADMOB_ID (or ADMOB_APP_ID) in local.properties or the CI environment before a production build.")
        }

        val requiredUnitIds = mapOf(
            "ADMOB_BANNER_ID" to (System.getenv("ADMOB_BANNER_ID") ?: localProps.getProperty("ADMOB_BANNER_ID")),
            "ADMOB_INTERSTITIAL_ID" to (System.getenv("ADMOB_INTERSTITIAL_ID") ?: localProps.getProperty("ADMOB_INTERSTITIAL_ID")),
            "ADMOB_REWARDED_ID" to (System.getenv("ADMOB_REWARDED_ID") ?: localProps.getProperty("ADMOB_REWARDED_ID")),
            "ADMOB_REWARDED_INTERSTITIAL_ID" to (System.getenv("ADMOB_REWARDED_INTERSTITIAL_ID") ?: localProps.getProperty("ADMOB_REWARDED_INTERSTITIAL_ID"))
        )
        val missingOrPlaceholder = requiredUnitIds.filter { (_, value) ->
            value.isNullOrBlank() || value.contains("PLACEHOLDER")
        }.keys
        if (missingOrPlaceholder.isNotEmpty()) {
            throw GradleException(
                "FATAL: Missing real AdMob ad unit ID(s) for a production build: " +
                    "${missingOrPlaceholder.joinToString(", ")}. Set them in local.properties " +
                    "or the CI environment before a production build."
            )
        }
    }
}


sentry {
    org.set("autominder-iz")
    projectName.set("android")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
}
