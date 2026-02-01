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
        versionCode = 13
        versionName = "0.13"
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
    implementation(libs.material3)
    coreLibraryDesugaring (libs.desugar.jdk.libs)

    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
//    implementation(libs.android.maps.compose)
//    implementation(libs.maps.compose.v272)
    implementation(libs.androidx.foundation)
    implementation (libs.androidx.ui.tooling.preview)
    debugImplementation (libs.androidx.ui.tooling)

    // Compose навигация
    implementation (libs.androidx.navigation.compose)

    // визуал material
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
//    implementation ("androidx.compose.material:material-icons-extended:1.4.3")

    // haze blur
    implementation(libs.haze) // блюр, эфект размытости для нижней навигации

    // корутин
    implementation(libs.kotlinx.coroutines.android)

    //логи Тимбер
//    implementation(libs.timber)

    // тестирование
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)

    // поиск текущего расположения юзера
//    implementation (libs.play.services.location)
//    implementation (libs.kotlinx.coroutines.play.services)

    // Room
//    implementation (libs.androidx.room.runtime)
//    implementation (libs.androidx.room.ktx)

    // работа со временем
//    implementation (libs.androidx.datastore.preferences)

}
