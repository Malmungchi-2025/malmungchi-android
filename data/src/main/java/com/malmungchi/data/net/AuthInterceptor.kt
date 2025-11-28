package com.malmungchi.data.net


import okhttp3.Interceptor
import okhttp3.Response

/**
 * 보호된 API에 자동으로 Authorization 헤더를 붙인다.
 */
private fun isPublicPath(path: String): Boolean {
    return path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/refresh") ||
            path.startsWith("/api/health") ||
            path.startsWith("/auth/kakao")
}

class AuthInterceptor(
    private val tokenProvider: AuthTokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath
        val builder = req.newBuilder()

        val token = tokenProvider.getToken()
        val attach = !isPublicPath(path) && !token.isNullOrBlank()

        android.util.Log.d(
            "AUTH_INT",
            "path=$path, attachAuth=$attach, tokenIsNullOrBlank=${token.isNullOrBlank()}"
        )

        if (attach) {
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
//class AuthInterceptor(
//    private val tokenProvider: AuthTokenProvider
//) : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val req = chain.request()
//
//        // 공개 엔드포인트는 제외 (로그인/회원가입/헬스체크 등)
//        val path = req.url.encodedPath
//        val isPublic =
//            path.contains("/api/auth/login") ||
//                    path.contains("/api/auth/register") ||
//                    path.contains("/api/health") ||
//                    path.contains("/api/auth/refresh") // ★ 추가
//
//        val builder = req.newBuilder()
//        if (!isPublic) {
//            tokenProvider.getToken()?.let { token ->
//                if (token.isNotBlank()) {
//                    builder.header("Authorization", "Bearer $token")
//                }
//            }
//        }
//        return chain.proceed(builder.build())
//    }
//}