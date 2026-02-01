// Павлов Алексей https://github.com/AlexeyJarlax

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.pavlovalexey.pavlovAlexeySandbox"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pavlovalexey.pavlovAlexeySandbox"
        resourceConfigurations += setOf("ru", "en")
        minSdk = 24 //Android 7
        targetSdk = 36
        versionCode = 14
        versionName = "0.14"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    bundle {
        abi {
            enableSplit = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
        encoding = "UTF-8"
    }

    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "2.2"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
        compose = true
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.foundation.android)
    implementation(libs.material3)
    coreLibraryDesugaring (libs.desugar.jdk.libs)

    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.runtime.livedata)
    implementation(libs.androidx.foundation)
    implementation(libs.google.accompanist.flowlayout)
    implementation (libs.androidx.ui.tooling.preview)
    debugImplementation (libs.androidx.ui.tooling)

    // Compose навигация
    implementation (libs.androidx.navigation.compose)

    // Пикчи
    implementation(libs.coil.compose)


    // визуал material
    implementation (libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)

    // haze blur
    implementation(libs.haze) // блюр, эфект размытости для нижней навигации

    // корутин
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.peko)

    // запрос разрешений
    implementation (libs.accompanist.permissions)

    // Обфускатор R8
    implementation(libs.bcprov.jdk15on)
    implementation(libs.conscrypt.android)
}
