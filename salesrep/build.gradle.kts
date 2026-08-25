import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinxSerialization)
}

android {
    namespace = "karika.distribucija.ba.salesrep"
    compileSdk = 37

    defaultConfig {
        applicationId = "karika.distribucija.ba.salesrep"
        minSdk = 30
        targetSdk = 37
        versionCode = 41
        versionName = "2"
        // Shared with launcher (see its build.gradle.kts) - checked on both ends of the
        // launcher<->salesrep broadcasts instead of a signature-level permission, since the two
        // apps are signed with different keys.
        buildConfigField("String", "KIOSK_IPC_TOKEN", "\"a746b793be90d5dba0895fdfbcce98e8b43f00e994b551e9\"")
    }

    buildFeatures {
        viewBinding = true
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
        debug {
            buildConfigField("String", "ENV_PREFIX", "\"stage.\"")
            buildConfigField("String", "ENV_JWT", "\"09kqzjtmz5cf1klm9hjxw9yt3uaa63hk\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField("String", "ENV_PREFIX", "\"\"")
            buildConfigField("String", "ENV_JWT", "\"lbzgyy1qylr7unu707eblcphftb2fzha\"")
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    implementation(project(":core-logging"))
}
