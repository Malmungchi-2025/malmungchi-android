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



// local.properties 로딩

val localProps = java.util.Properties()
val localFile = rootProject.file("local.properties")

if (localFile.exists()) {
    localFile.inputStream().use { localProps.load(it) }
}

// 안전한 getter
fun prop(key: String): String? = localProps.getProperty(key)


// 전역 변수(ext)에 등록

ext["BASE_URL"] = prop("BASE_URL")
ext["KAKAO_NATIVE_APP_KEY"] = prop("KAKAO_NATIVE_APP_KEY")

ext["STORE_FILE"] = prop("STORE_FILE")
ext["STORE_PASSWORD"] = prop("STORE_PASSWORD")
ext["KEY_ALIAS"] = prop("KEY_ALIAS")
ext["KEY_PASSWORD"] = prop("KEY_PASSWORD")
