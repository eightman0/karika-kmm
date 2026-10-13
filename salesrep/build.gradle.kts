import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.gms)
}


/** Domain prefix and guest JWT of each backend environment, see karikaEnv below. */
val KARIKA_ENVS = mapOf(
    "stage" to ("stage." to "09kqzjtmz5cf1klm9hjxw9yt3uaa63hk"),
    "demo" to ("demo." to "hgy5au3paxuijsiv52nyt9w47fcxprbz"),
    "prod" to ("" to "lbzgyy1qylr7unu707eblcphftb2fzha"),
)

android {
    namespace = "karika.distribucija.ba.salesrep"
    compileSdk = 37

    defaultConfig {
        applicationId = "karika.distribucija.ba.salesrep"
        minSdk = 30
        targetSdk = 37
        versionCode = 71
        versionName = "2.3"
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

    // The backend environment: -PkarikaEnv=stage|demo|prod (scripts/build-release-apks.sh passes
    // it), otherwise stage for debug and prod for release. The package stays the same, so every
    // Karika Ops (stage, demo, prod) publishes its own build of the same app.
    val karikaEnv = providers.gradleProperty("karikaEnv").orNull
    fun com.android.build.api.dsl.ApplicationBuildType.karikaEnv(default: String) {
        val env = karikaEnv ?: default
        val (prefix, jwt) = KARIKA_ENVS[env] ?: error("karikaEnv must be one of ${KARIKA_ENVS.keys}, not $env")
        buildConfigField("String", "ENV_PREFIX", "\"$prefix\"")
        buildConfigField("String", "ENV_JWT", "\"$jwt\"")
    }

    buildTypes {
        debug {
            karikaEnv("stage")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            karikaEnv("prod")
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
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.location)
    implementation(libs.vico.views)

    implementation(project(":core-logging"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)

    // In-app photo capture (see ui/camera/CameraCaptureActivity.kt) - not an external Camera app
    // intent, since a kiosk provisioned with PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED=false may
    // not have one installed at all.
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
}
