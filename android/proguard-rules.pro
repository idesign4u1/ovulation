# ProGuard rules for Ovulation Health App

# Keep all model classes
-keepclassmembers class com.ovulation.health.data.model.** {
    <init>;
    <fields>;
}

# Keep Room database and DAOs
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep interface androidx.room.** { *; }
-keepclassmembers interface com.ovulation.health.data.db.** { *; }

# Keep TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }

# Keep Retrofit interfaces
-keep interface com.ovulation.health.network.** { *; }
-keepclassmembers interface com.ovulation.health.network.** { *; }

# Keep JSON serialization classes for Retrofit
-keepclassmembers class com.ovulation.health.network.** {
    <init>;
    <fields>;
    <methods>;
}

# Keep all enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Timber
-keepclassmembers class timber.log.Timber { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }
-keepclassmembers class androidx.work.** { *; }

# Keep ML Kit Vision
-keep class com.google.mlkit.vision.** { *; }
-keepclassmembers class com.google.mlkit.vision.** { *; }

# Keep data classes
-keep class com.ovulation.health.data.model.** { *; }
-keepclassmembers class com.ovulation.health.data.model.** { *; }

# Keep services
-keep class com.ovulation.health.service.** { *; }
-keepclassmembers class com.ovulation.health.service.** { *; }

# Remove logging in release builds
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove API calls in debugging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Keep exceptions
-keep public class * extends java.lang.Exception { *; }

# Gson configuration
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Android
-keep class android.** { *; }
-keepclassmembers class android.** { *; }
