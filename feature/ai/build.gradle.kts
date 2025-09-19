plugins {
    id("com.android.library")               // ✅ 버전 지정 제거
    id("org.jetbrains.kotlin.android")      // ✅ alias 사용 안함 → id 사용
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.malmungchi.feature.ai"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
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

    // ✅ Hilt (플러그인 없이 라이브러리만)
    implementation(libs.hilt.android)
    implementation(project(":data"))
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ✅ retrofit2.HttpException 사용을 위해 (이 모듈에서도 필요)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // ✅ collectAsStateWithLifecycle 사용 시 필요
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // ✅ 코루틴(Android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Core & Design 모듈 참조
    implementation(project(":core"))
    implementation(project(":design"))

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
}
