# Fake Call ProGuard rules.
# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mgneg.fakecall.**$$serializer { *; }
-keepclassmembers class com.mgneg.fakecall.** {
    *** Companion;
}
-keepclasseswithmembers class com.mgneg.fakecall.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Room generated code
-keep class * extends androidx.room.RoomDatabase { <init>(); }
