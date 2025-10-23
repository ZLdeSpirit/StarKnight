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

-keep class com.reyun.** {*; }
-keep class route.**{*;}
-keep interface com.reyun.** {*; }
-keep interface route.**{*;}
-dontwarn com.reyun.**
-dontwarn org.json.**
-keep class org.json.**{*;}
# Google lib库
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
# 如果使用到了获取oaid插件，请添加以下混淆策略
-keep class com.huawei.hms.**{*;}
-keep class com.hihonor.**{*;}

# ========== SingBoxOptions混淆==========

# 保护整个包结构
-keep class moe.matsuri.nb4a.SingBoxOptions { *; }
-keep class moe.matsuri.nb4a.SingBoxOptions$** { *; }

# 保护所有内部类和成员
-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$* {
    *;
}

# Gson 必需属性
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*

# 保护所有字段（由于使用反射）
-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$** {
    public protected private <fields>;
}

# 保护所有方法
-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$** {
    public protected private <methods>;
}

# 保护构造函数
-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$** {
    public <init>(...);
}

# 特殊保护 final_ 字段（因为有 @SerializedName("final")）
-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$DNSOptions {
    public java.lang.String final_;
}

-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$RouteOptions {
    public java.lang.String final_;
}

-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$Outbound_SelectorOptions {
    public java.lang.String default_;
}

# 保护 transient 字段（虽然 transient 字段默认不被序列化，但代码中使用了）
-keepclassmembers class moe.matsuri.nb4a.SingBoxOptions$** {
    public transient *;
}

# 保护工具类
-keep class moe.matsuri.nb4a.utils.Util { *; }

# 防止内联
-dontoptimize