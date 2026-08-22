// Top-level build file. Dependency versions managed via gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application)  apply false
    // Must be declared here too: applying com.android.test in a subproject
    // without the root having it on the classpath fails with "already on the
    // classpath with an unknown version".
    alias(libs.plugins.android.test)         apply false
    alias(libs.plugins.kotlin.android)       apply false
    alias(libs.plugins.kotlin.compose)       apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android)         apply false
    alias(libs.plugins.ksp)                  apply false
    alias(libs.plugins.google.services)      apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf)        apply false
    alias(libs.plugins.baselineprofile)      apply false
}
