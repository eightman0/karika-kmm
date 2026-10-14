import java.security.MessageDigest
import java.util.Properties

/** Same salt as ServiceUnlockActivity.hash(). */
fun servicePinSha256(): String {
    val propsFile = file("keystore.properties")
    if (!propsFile.exists()) return ""
    val pin = Properties().apply { load(propsFile.inputStream()) }.getProperty("kioskServicePin")
        ?.trim().orEmpty()
    if (pin.isEmpty()) return ""
    return MessageDigest.getInstance("SHA-256")
        .digest("karika-kiosk-service:$pin".toByteArray())
        .joinToString("") { byte -> "%02x".format(byte) }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.gms)
}

android {
    namespace = "karika.distribucija.ba.launcher"
    compileSdk = 37

    defaultConfig {
        applicationId = "karika.distribucija.ba.launcher"
        minSdk = 30
        targetSdk = 37
        // 3.x talks only to the Laravel device API (enroll + bearer token) - 2.x and older can't.
        versionCode = 28
        versionName = "3.5"
        // Shared with salesrep (see its build.gradle.kts) - launcher and salesrep are signed with
        // different keys, so a signature-level permission can't gate the broadcasts between them.
        // This token is checked in code instead, the same way LogProvider now checks the calling
        // package directly rather than trusting a manifest permission grant.
        buildConfigField("String", "KIOSK_IPC_TOKEN", "\"a746b793be90d5dba0895fdfbcce98e8b43f00e994b551e9\"")
        // Service PIN for the on-device kiosk unlock (ServiceUnlockActivity) - only its salted
        // SHA-256 goes into the APK. Set `kioskServicePin` in launcher/keystore.properties (not in
        // git); without it the service unlock is disabled.
        buildConfigField("String", "SERVICE_PIN_SHA256", "\"${servicePinSha256()}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    val keystorePropsFile = file("keystore.properties")
    signingConfigs {
        if (keystorePropsFile.exists()) {
            val keystoreProps = Properties().apply { load(keystorePropsFile.inputStream()) }
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(project(":core-logging"))
}
