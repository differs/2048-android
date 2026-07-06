# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.differs.game2048.** {
    *** Companion;
}
-keepclasseswithmembers class com.differs.game2048.** {
    kotlinx.serialization.KSerializer serializer(...);
}
