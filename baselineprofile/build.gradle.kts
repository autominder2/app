plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace  = "com.autominder.baselineprofile"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    defaultConfig {
        // Macrobenchmark drives a real app process, so it cannot run below 24.
        // The app's own minSdk is 26, which is already above that floor.
        minSdk    = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // The module under measurement.
    targetProjectPath = ":app"
}

// Generation must run on a rooted emulator or a userdebug build — a plain
// physical device cannot capture the profile. AGP enforces this; the property
// below documents the expectation rather than silently failing later.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.uiautomator)
    implementation(libs.junit)
}
