# ONNX Runtime calls these Java classes from native code (JNI), so R8 must not rename or remove them
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# readable stack traces in Play crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Tink (via androidx.security-crypto) references compile-time-only annotations
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
