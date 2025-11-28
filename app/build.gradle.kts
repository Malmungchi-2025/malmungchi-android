import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Hilt
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.malmungchi"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val storeFilePath = project.properties["STORE_FILE"] as String
            storeFile = file(storeFilePath)

            storePassword = project.properties["STORE_PASSWORD"] as String
            keyAlias = project.properties["KEY_ALIAS"] as String
            keyPassword = project.properties["KEY_PASSWORD"] as String
        }
    }

    defaultConfig {
        applicationId = "com.example.malmungchi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BASE_URL (local.properties → BuildConfig)
        buildConfigField(
            "String",
            "BASE_URL",
            "\"${project.properties["BASE_URL"]}\""
        )

        // Kakao Key (local.properties → BuildConfig)
        buildConfigField(
            "String",
            "KAKAO_APP_KEY",
            "\"${project.properties["KAKAO_NATIVE_APP_KEY"]}\""
        )

        // manifest에 주입
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] =
            project.properties["KAKAO_NATIVE_APP_KEY"] as String
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Kakao SDK
    implementation("com.kakao.sdk:v2-user:2.20.5")
    implementation("com.kakao.sdk:v2-auth:2.20.5")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Core modules
    implementation(project(":core"))
    implementation(project(":design"))
    implementation(project(":data"))

    // Feature modules
    implementation(project(":feature:ai"))
    implementation(project(":feature:friend"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:study"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:login"))

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
