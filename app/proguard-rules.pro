# Add project specific ProGuard rules here.

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Navigation
-keep class androidx.navigation.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# Keep data classes
-keep class com.temporary.memo.data.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Widget support
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class com.temporary.memo.widget.** { *; }
-keepclassmembers class com.temporary.memo.widget.MemoWidgetReceiver {
    public <init>();
    public void onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[]);
}

# RemoteViews support
-keep class android.widget.RemoteViews { *; }
-keepclassmembers class * {
    public <init>(android.content.Context, int);
}
