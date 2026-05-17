# ============================================================
# AutoMinder — ProGuard Rules (baseline)
# ============================================================

# ─── Room ────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# ─── Hilt / Dagger ──────────────────────────────────────────
-dontwarn com.google.dagger.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.InstallIn class *

# ─── Kotlin Serialization ───────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.autominder.app.**$$serializer { *; }
-keepclassmembers class com.autominder.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.autominder.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Coroutines ──────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ─── AdMob / Google Mobile Ads ───────────────────────────────
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.**
-dontwarn com.google.android.gms.ads.**

# ─── Play Billing ────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ─── Coil 3 ──────────────────────────────────────────────────
-keep class coil3.** { *; }
-dontwarn coil3.**
-dontwarn coil.**

# ─── Glance AppWidget ────────────────────────────────────────
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver

# ─── Timber ──────────────────────────────────────────────────
-dontwarn org.jetbrains.annotations.**

# ─── DataStore Preferences ──────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ─── Navigation Compose — type-safe @Serializable routes ────
-keep class com.autominder.app.ui.navigation.NavRoutes { *; }
-keep class com.autominder.app.ui.navigation.NavRoutes$* { *; }

# ─── Enum Classes (ServiceType and others) ──────────────────
-keepclassmembers enum com.autominder.app.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ─── General Android ─────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
