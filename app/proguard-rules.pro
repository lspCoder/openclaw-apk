# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep application classes
-keep class com.openclaw.apk.** { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# AndroidX
-dontwarn androidx.**
-keep class androidx.** { *; }

# Material Design
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }
