plugins {
    id("com.android.application")
}

android {
    namespace = "com.zevclip.sender"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zevclip.sender"
        minSdk = 26
        targetSdk = 36
        versionCode = 32
        versionName = "3.1.1"
    }

    val localKeystore = file(System.getProperty("user.home") + "/.zevlink-signing/zevlink.jks")
    if (localKeystore.exists()) {
        signingConfigs {
            create("local") {
                storeFile = localKeystore
                storePassword = "zevlink-local"
                keyAlias = "zevlink"
                keyPassword = "zevlink-local"
            }
        }
    }

    buildTypes {
        release {
            if (localKeystore.exists()) {
                signingConfig = signingConfigs.getByName("local")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.google.zxing:core:3.5.3")
    implementation("org.bouncycastle:bcprov-jdk18on:1.81")
    testImplementation("junit:junit:4.13.2")
}
