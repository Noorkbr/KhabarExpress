# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ==========================================
# Mapbox SDK ProGuard Rules (COMPLETE)
# ==========================================

# Keep all Mapbox classes and methods
-keep class com.mapbox.** { *; }
-keep interface com.mapbox.** { *; }
-dontwarn com.mapbox.**

# CRITICAL: Keep Mapbox Initialization classes (fixes ClassNotFoundException)
-keep class com.mapbox.common.** { *; }
-keep class com.mapbox.common.BaseMapboxInitializer { *; }
-keep class com.mapbox.navigation.core.internal.MapboxNavigationSDKInitializer { *; }
-keep class * extends com.mapbox.common.BaseMapboxInitializer { *; }

# Keep Mapbox ContentProvider and Startup Initializers
-keep class * extends androidx.startup.Initializer { *; }
-keep class androidx.startup.InitializationProvider { *; }

# ==========================================
# AndroidX Startup (CRITICAL for Mapbox)
# ==========================================
# Keep InitializationProvider and all Initializer implementations
-keep class androidx.startup.** { *; }
-keep interface androidx.startup.Initializer
-keep class * extends androidx.startup.Initializer {
    <init>();
    public java.util.List dependencies();
    public void create(android.content.Context);
}

# Keep all ContentProvider implementations
-keep class * extends android.content.ContentProvider {
    <init>();
}

# Keep androidx.startup.InitializationProvider specifically
-keepnames class androidx.startup.InitializationProvider

# Keep Mapbox Navigation lifecycle components
-keep class com.mapbox.navigation.** { *; }
-keep interface com.mapbox.navigation.** { *; }

# Keep GeoJSON and Turf
-keep class com.mapbox.geojson.** { *; }
-keep class com.mapbox.turf.** { *; }

# Keep Mapbox Native SDK
-keep class com.mapbox.bindgen.** { *; }

# Keep annotations used by Mapbox
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Mapbox services and listeners
-keepclassmembers class * {
    @com.mapbox.** <fields>;
    @com.mapbox.** <methods>;
}

# Keep serialization classes for Mapbox
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Retrofit and OkHttp (used by Mapbox)
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# Kotlin serialization
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Hilt
-dontwarn javax.annotation.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent