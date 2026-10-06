import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.gms)
    alias(libs.plugins.crashlytics)
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            linkerOpts("-Xlinker", "-parallel_link_jobs=4")
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Karika"
            isStatic = true

            export(libs.decompose.decompose)
            export(libs.essenty.lifecycle)
            export(libs.essenty.backhandler)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            api(libs.koin.android)
            api(libs.koin.compose.multiplatform)
            api(libs.koin.workmanager)

            implementation(libs.accompanist)

            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)

            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.messaging.ktx)

            implementation(libs.app.update)
            implementation(libs.app.update.ktx)

            implementation(libs.play.services.location)
            implementation(libs.maps.compose)
            implementation(libs.kotlinx.coroutines.play.services)
        }

        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ui.backhandler)

            api(libs.koin.core)
            implementation(libs.koin.compose.multiplatform)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)

            api(libs.decompose.decompose)
            api(libs.decompose.extensions.compose)
            api(libs.decompose.extensions.compose.experimental)
            api(libs.essenty.lifecycle)
            api(libs.essenty.backhandler)
            api(libs.essenty.lifecycle.coroutines)

            implementation(libs.richeditor.compose)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.material.icons.extended)
        }
    }
}

android {
    namespace = "karika.distribucija.ba"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "karika.distribucija.ba"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 270

        versionName = "2.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Stage test accounts for the login E2E tests, from the environment or ~/.gradle/gradle.properties
        listOf(
            "KARIKA_STAGE_SHOP_EMAIL",
            "KARIKA_STAGE_SHOP_PASSWORD",
            "KARIKA_STAGE_DISTRIBUTER_EMAIL",
            "KARIKA_STAGE_DISTRIBUTER_PASSWORD",
            "KARIKA_STAGE_SALESREP_EMAIL",
            "KARIKA_STAGE_SALESREP_PASSWORD",
        ).forEach { key ->
            providers.environmentVariable(key)
                .orElse(providers.gradleProperty(key))
                .orNull
                ?.let { testInstrumentationRunnerArguments[key] = it }
        }
        // All E2E tests in the order a user goes through the app, unless a run picks its own tests
        val picksTests = listOf("class", "package", "notClass", "notPackage", "tests_regex")
            .any { providers.gradleProperty("android.testInstrumentationRunnerArguments.$it").isPresent }
        if (!picksTests) testInstrumentationRunnerArguments["class"] = "karika.distribucija.ba.e2e.AllE2ETests"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Optional, gitignored - lets scripts/build-release-apks.sh build a signed kiosk (shop) APK
    // headlessly, same as launcher/salesrep. Without it release builds stay unsigned as before.
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
        getByName("release") {
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    flavorDimensions += "karika"
    productFlavors {
        create("demo") {
            applicationId = "karika.distribucija.ba.demo"
            dimension = "karika"
        }
        create("uat") {
            applicationId = "karika.distribucija.ba.uat"
            dimension = "karika"
        }
        create("stage") {
            applicationId = "karika.distribucija.ba.stage"
            dimension = "karika"
        }
        create("prod") {
            applicationId = "karika.distribucija.ba"
            dimension = "karika"
        }
        create("kiosk") {
            applicationId = "karika.distribucija.ba.kiosk"
            dimension = "karika"
        }
        create("kioskTest") {
            applicationId = "karika.distribucija.ba.kiosk"
            dimension = "karika"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    lint {
        disable.add("NullSafeMutableLiveData")
    }
    sourceSets {
        getByName("kioskTest") {
            setRoot("src/kiosk")
        }
        getByName("kiosk") {
            setRoot("src/kiosk")
        }
    }
}

dependencies {
    // End-to-end tests that launch the real app on a device, see src/androidInstrumentedTest
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

compose.resources {
    generateResClass = always
}

