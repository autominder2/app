# ─── AutoMinder Production ProGuard Configuration ───────────────────────────

# 1. Hilt / Dagger
-keepattributes *Annotation*
-keepattributes Signature
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }

# 2. Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao
-dontwarn androidx.room.paging.**

# 3. Kotlinx Serialization
# Ensure @Serializable classes aren't stripped as they are used by Navigation Compose
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class ** {
    *** Companion;
    *** $serializer;
}

# 4. AdMob / UMP
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }

# 5. Billing
-keep class com.android.billingclient.api.** { *; }

# 6. Timber Strip (Expertise)
# This physically removes the calls to Timber.d, Timber.v, and Timber.i from the release binary.
# Note: R8 is more efficient if we target the static methods directly.
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 7. Compose Performance
# Optimizes Compose runtime by stripping out source information used by the debugger
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable *;
}

# 8. General Metadata Preservation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
