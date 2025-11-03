plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
    id("stringfog")
    id("kotlin-parcelize")
}

apply(plugin = "stringfog")
configure<com.github.megatronking.stringfog.plugin.StringFogExtension> {
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    packageName = "com.github.megatronking.stringfog.app"
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}

android {
    namespace = "com.s.k.starknight"
    compileSdk = 36
    //TODO 打正式包移除
//    resourcePrefix = "sk"

    ksp {
        arg("room.incremental", "true")
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    defaultConfig {
        applicationId = "com.starvpn.proxy.nightproxy.knightproxy"
        minSdk = 24
        targetSdk = 36
        versionCode = 100
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "${rootProject.name}-v${versionName}-${versionCode}")
    }

    buildTypes {
//        debug {
//            isMinifyEnabled = true
//            isShrinkResources = true
//            isDebuggable = true
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        aidl = true
    }
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(fileTree("libs"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.0")

    api("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    implementation("com.tencent:mmkv:1.3.14")
    implementation("com.airbnb.android:lottie:6.6.2")

    //google
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-config")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // admob
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    // 买量用户确认
    implementation("com.reyun.solar.engine.oversea:solar-engine-core:1.2.9.7")
    implementation("com.android.installreferrer:installreferrer:2.2")

    implementation("com.facebook.android:facebook-android-sdk:latest.release")

    implementation("com.google.android.gms:play-services-ads:24.6.0")
    implementation("com.google.ads.mediation:applovin:13.4.0.0")
    implementation("com.google.ads.mediation:ironsource:8.11.1.0")
    implementation("com.google.ads.mediation:mintegral:16.9.91.1")
    implementation("com.google.ads.mediation:pangle:7.6.0.2.0")
    implementation("com.unity3d.ads:unity-ads:4.16.1")
    implementation("com.google.ads.mediation:unity:4.16.1.0")

    implementation("com.github.megatronking.stringfog:xor:5.0.0")

    implementation("com.blankj:utilcodex:1.31.1")

    //room
//    implementation("androidx.room:room-runtime:2.8.0")
//    ksp("androidx.room:room-compiler:2.8.0")
//    implementation("androidx.room:room-ktx:2.8.0")



    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.work:work-multiprocess:2.8.1")
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("org.yaml:snakeyaml:1.30")
    implementation("com.jakewharton:process-phoenix:2.1.2")
    implementation("com.esotericsoftware:kryo:5.2.1")
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.ini4j:ini4j:0.5.4")

    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("com.github.MatrixDev.Roomigrant:RoomigrantLib:0.3.4")
    ksp("com.github.MatrixDev.Roomigrant:RoomigrantCompiler:0.3.4")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
}