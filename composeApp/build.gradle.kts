import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
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
            implementation(compose.preview)
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
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
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
            implementation(libs.kotlinx.datetime)

            api(libs.decompose.decompose)
            api(libs.decompose.extensions.compose)
            api(libs.decompose.extensions.compose.experimental)
            api(libs.essenty.lifecycle)
            api(libs.essenty.backhandler)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
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
        versionCode = 224

        versionName = "2.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        create("prod") {
            applicationId = "karika.distribucija.ba"
            dimension = "karika"
        }
        create("kiosk") {
            applicationId = "karika.distribucija.ba.kiosk"
            dimension = "karika"
            versionCode = 220
            versionName = "2.0"
        }
        create("kioskTest") {
            applicationId = "karika.distribucija.ba.kiosk"
            dimension = "karika"
            versionCode = 220
            versionName = "2.0"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    debugImplementation(compose.uiTooling)
}

compose.resources {
    generateResClass = always
}

