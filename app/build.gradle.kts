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

    defaultConfig {
        applicationId = "com.example.malmungchi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ 간단하게 BASE_URL 하드코딩 (local.properties 제거)
        buildConfigField("String", "BASE_URL", "\"https://malmungchi-server.onrender.com\"")

    }

    buildTypes {
        release {
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

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Core & Design 모듈 참조
    implementation(project(":core"))
    implementation(project(":design"))
    implementation(project(":data"))
    //implementation(project(":feature"))

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.35.0-alpha")


    implementation("androidx.core:core-splashscreen:1.0.1")
    //다른 모듈 참조
    implementation(project(":feature:ai"))
    implementation(project(":feature:friend"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:study"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:login"))

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}