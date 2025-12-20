# Keep model classes (for reflection)
-keep class com.symbianx.minimalistlauncher.domain.model.** { *; }

# Keep Compose @Stable/@Immutable classes
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
