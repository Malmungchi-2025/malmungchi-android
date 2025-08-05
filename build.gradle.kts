// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false      // ✅ 라이브러리 모듈에서도 사용
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ✅ Hilt Gradle Plugin 추가
    alias(libs.plugins.hilt.android) apply false

    // ✅ KSP 플러그인 추가
    alias(libs.plugins.ksp) apply false
}