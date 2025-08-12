plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt") // ✅ Hilt 사용 시 필요
}

android {
    namespace = "com.malmungchi.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17   // ✅ JDK 17 적용
        targetCompatibility = JavaVersion.VERSION_17


    }
    kotlinOptions {
        jvmTarget = "17"                              // ✅ JDK 17 적용
    }

    buildFeatures {
        compose = true                                // ✅ Compose 활성화
        buildConfig = true   // ✅ BuildConfig 사용 가능하게 설정

    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"     // ✅ Compose 최신 버전
    }
    lint {
        abortOnError = false
    }
}

dependencies {
    // ✅ Compose 기본 의존성
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.activity.compose)   // ✅ setContent 사용 가능

    // ✅ Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // ✅ Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Moshi
    implementation("com.squareup.moshi:moshi:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
}
