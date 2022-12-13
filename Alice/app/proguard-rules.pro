-assumenosideeffects class android.util.Log {

public static *** d(...);

public static *** v(...);

public static *** i(...);

public static *** e(...);

public static *** w(...);

}

# 针对于native方法不混淆
-keepclasseswithmembernames class * {
    native <methods>;

}
#对含有反射类的处理
-keep class com.club.mobile.main.MainActivity { public void call(java.lang.String); }

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void check*(...);
	public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}


# Strip Timber verbose and debug logging
-assumenosideeffects class timber.log.Timber$Tree {
  public void v(**);
  public void d(**);
}
