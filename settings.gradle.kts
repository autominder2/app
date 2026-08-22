pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "AutoMinder"
include(":app")
// Producer module for the Baseline Profile. It is a com.android.test module:
// it never ships in the APK, it only runs on a device to generate the profile
// that :app then bundles. See baselineprofile/build.gradle.kts.
include(":baselineprofile")
