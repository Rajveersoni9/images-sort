# MediaFlow AI Proguard Rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.mediaflow.ai.data.local.entity.** { *; }
-keep class com.mediaflow.ai.domain.model.** { *; }
