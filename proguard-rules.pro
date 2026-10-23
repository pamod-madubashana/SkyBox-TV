# ProGuard rules for SkyBox TV - Release build configuration
# https://developer.android.com/studio/build/shrink-code/proguard

# ============================================================
# TDLib (org.drinkless.tdlib.**)
# ==========================================================-
-keep class org.drinkless.tdlib.** { *; }
-keepclassmembers class org.drinkless.tdlib.** {
    *;
}
-keepclassmembers class org.telegram.mtproto.** {
    *;
}

# TDLib specific keep rules - prevent obfuscation of critical classes
-keep class org.drinkless.tdlib.TdService { *; }
-keep class org.drinkless.tdlib.TdClient { *; }
-keep class org.drinkless.tdlib.api.** { *; }
-keep class org.drinkless.tdlib.core.** { *; }

# ============================================================
# Media3 (androidx.media3.**)
# ============================================================
-keep class androidx.media3.** { *; }
-keepclassmembers class androidx.media3.** {
    *;
}
-keep enum androidx.media3.** { *; }

# Media3 ExoPlayer specific rules
-keep class androidx.media3.exoplayer.** { *; }
-keepclassmembers class androidx.media3.exoplayer.** {
    *;
}
-keep enum androidx.media3.exoplayer.** { *; }

# Media3 DataSource specific rules
-keep class androidx.media3.datasource.** { *; }
-keepclassmembers class androidx.media3.datasource.** {
    *;
}

# Media3 UI specific rules
-keep class androidx.media3.ui.** { *; }
-keepclassmembers class androidx.media3.ui.** {
    *;
}

# ============================================================
# Room (androidx.room.**)
# ============================================================
-keepclassmembers class *.annotation.RoomDatabase {
    *;
}
-keep class *.*Dao { *; }
-keep class androidx.room.Room { *; }

# Keep entity classes
-keep class *.*Entity { *; }
-keep class *.*Dao { *; }

# Keep Room compensation for Kotlin features
-keep @androidx.room.Query *.* { *; }
-keep @androidx.room.Insert *.* { *; }
-keep @androidx.room.Update *.* { *; }
-keep @androidx.room.Delete *.* { *; }

# Kotlin serialization support for Room
-keep class androidx.room.db.sqlite.SupportSQLiteOpenHelper {}
-keep class androidx.room.db.sqlite.SupportSQLiteClosable {}

# ============================================================
# Compose (androidx.compose.**)
# ============================================================
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.ui.node.ComposeNode {
    *;
}
-keepclassmembers class androidx.compose.runtime.snapshots.SnapshotStateList {
    *;
}
-keepclassmembers class androidx.compose.runtime.snapshots.SnapshotStateMap {
    *;
}
-keepclassmembers class androidx.compose.runtime.*
# Keep Compose runtime identifiers
-keepclassmembers class androidx.compose.ui.Modifier {
    *;
}
-keepattributes *Annotation*

# Keep @Composable functions and parameters
-keepannotatedclass class androidx.compose.ui.platform.ComposeView { *; }
-keep @compose.compiler.api.Stable class * { *; }

# Keep DrawableImporter and image loading
-keep class androidx.compose.ui.platform.* { *; }

# ============================================================
# Kotlin coroutines
# ============================================================
-keepclassmembers class kotlin.coroutines.*;
-keepclassmembers class kotlinx.coroutines.*;
-keepclassmembers class kotlinx.coroutines.android.*;

# Keep suspend function metadata
-keepattributes *Annotation*, OriginalName
-keep @interface kotlin.coroutines.Resume

# Keep coroutine context and dispatchers
-keep class kotlinx.coroutines.Dispatchers {
    *;
}
-keep class kotlinx.coroutines.CoroutineStart {
    *;
}

# ============================================================
# AndroidX core and support
# ============================================================
-keep class androidx.core.** { *; }
-keepclassmembers class androidx.core.** {
    *;
}

# Keep Android TV Foundation classes
-keep class androidx.tv.** { *; }
-keepclassmembers class androidx.tv.** {
    *;
}

# Keep Material Components
-keep class androidx.appcompat.** { *; }
-keepclassmembers class androidx.appcompat.** {
    *;
}
-keep class androidx.material.** { *; }
-keepclassmembers class androidx.material.** {
    *;
}

# Keep Activity and Fragment
-keep class androidx.activity.** { *; }
-keepclassmembers class androidx.activity.** {
    *;
}
-keep class androidx.fragment.** { *; }
-keepclassmembers class androidx.fragment.** {
    *;
}

# ============================================================
# SkyBox TV specific classes
# ============================================================
# Keep main application class
-keep class com.skybox.tv.SkyBoxApp { *; }
-keep class com.skybox.tv.presentation.* { *; }
-keep class com.skybox.tv.domain.* { *; }
-keep class com.skybox.tv.data.* { *; }
-keep class com.skybox.tv.ui.* { *; }
-keep class com.skybox.tv.playback.* { *; }
-keep class com.skybox.tv.telegram.* { *; }
-keep class com.skybox.tv.data.streaming.* { *; }

# Keep ExoPlayer classes
-keep class com.skybox.tv.playback.ExoPlayerPlaybackController { *; }

# ============================================================
# Generic keep rules (use sparingly)
# ============================================================
# Method names - keep for runtime reflection
-keeppublic class * {
    public *;
}

# Prevent resource shrinking from removing kept classes
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>();
}

# Keep Parcelable implementors
-keep @androidx.annotation.ParcelClass * implements android.os.Parcelable {
    *;
}