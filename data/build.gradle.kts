plugins {
    id("com.android.library")                     // ✅ 라이브러리 모듈
    id("org.jetbrains.kotlin.android")            // ✅ Kotlin Android
    id("dagger.hilt.android.plugin")              // ✅ Hilt Gradle Plugin 추가
    id("com.google.devtools.ksp")                 // ✅ KSP 플러그인 (alias 제거하고 id로 명확 지정)
}

android {
    namespace = "com.malmungchi.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ 임시 서버 URL (나중에 실제 서버 URL로 교체)
        buildConfigField(
            "String",
            "SERVER_BASE_URL",
            "\"https://dummy-server.local/\"" // 🔥 로컬 테스트용 URL , 무조건 수정!
        )
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
        buildConfig = true                          // ✅ BuildConfig 사용 가능하게 설정
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // ✅ Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")   // ✅ Gson 변환기 추가
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // ✅ Moshi (필요 시 유지)
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")

    // ✅ Gson Core
    implementation("com.google.code.gson:gson:2.11.0")

    // ✅ Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core"))

    // ✅ javax.inject
    implementation("javax.inject:javax.inject:1")

    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

}