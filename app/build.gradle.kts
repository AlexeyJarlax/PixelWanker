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
        resourceConfigurations += setOf("ru", "en", "es", "hi", "fr", "pt", "ja")
        minSdk = 24 //Android 7
        targetSdk = 36
        versionCode = 24
        versionName = "0.24"
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


    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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

    // визуал material3
    implementation (libs.androidx.material3)
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

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

}


tasks.register("qaTestSummary") {
    group = "verification"
    description = "Runs debug unit tests and prints QA summary line."
    dependsOn("testDebugUnitTest")

    doLast {
        val reportDir = file("$buildDir/test-results/testDebugUnitTest")
        val reportFiles = reportDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "xml" }
            .toList()

        var total = 0
        var failures = 0
        var errors = 0
        var skipped = 0

        reportFiles.forEach { reportFile ->
            val text = reportFile.readText()
            val tests = Regex("tests=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val fileFailures = Regex("failures=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val fileErrors = Regex("errors=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val fileSkipped = Regex("skipped=\"(\\d+)\"").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            total += tests
            failures += fileFailures
            errors += fileErrors
            skipped += fileSkipped
        }

        val passed = (total - failures - errors - skipped).coerceAtLeast(0)
        val successRate = if (total == 0) 0 else ((passed * 100.0) / total).toInt()

        println("🔬🔬🔬 QA PavlovAlexey: $successRate% of the tests were successfully completed 🧪🧪🧪")
        println("QA summary: total=$total, passed=$passed, failed=$failures, errors=$errors, skipped=$skipped")
    }
}
