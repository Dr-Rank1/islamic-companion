# Islamic Companion — R8 / ProGuard rules (v3)
# Code shrinking + obfuscation enabled for release.
# Add project specific ProGuard rules here.

# Adhan library — keep all public API
-keep class com.batoulapps.adhan.** { *; }

# Gson — keep model classes used for JSON parsing
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# App model classes parsed from JSON assets
-keep class com.islamiccompanion.app.model.** { *; }

# Room — keep entity and DAO classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker

# Google Play Services Ads
-keep class com.google.android.gms.ads.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep custom Views
-keep class com.islamiccompanion.app.ui.qibla.QiblaCompassView { *; }
-keep class com.islamiccompanion.app.ui.tasbih.BeadView { *; }
-keep class com.islamiccompanion.app.ui.stats.PrayerBarChartView { *; }

# EncryptedSharedPreferences / security-crypto
-keep class androidx.security.crypto.** { *; }
-keepclassmembers class androidx.security.crypto.** { *; }

# OkHttp (Mosque finder)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson — keep @SerializedName fields on all classes (not just model package)
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Prevent stripping of security utilities used reflectively
-keep class com.islamiccompanion.app.util.SecurityHelper { *; }
-keep class com.islamiccompanion.app.util.EncryptedPrefsHelper { *; }

# ViewBinding — keep all generated binding classes
-keep class com.islamiccompanion.app.databinding.** { *; }

# Lifecycle ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Suppress notes for libraries that use reflection internally
-dontnote com.google.android.gms.**
-dontnote androidx.security.**
