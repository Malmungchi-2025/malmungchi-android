package com.malmungchi.data.net



import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.HttpUrl
import org.json.JSONObject

/**
 * 401/403이 오면 1회에 한해 refresh 토큰으로 access 토큰을 갱신하고,
 * 원 요청을 새 토큰으로 재시도한다. 실패하면 그대로 통과(다음 UnauthorizedInterceptor가 처리).
 */
class RetryAuthInterceptor(
    private val context: Context,
    private val refreshEndpoint: String = "/api/auth/refresh" // 서버에 맞게 필요시 변경
) : Interceptor {

    @Volatile private var isRefreshing = false
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val originalResponse = chain.proceed(request)

        val code = originalResponse.code
        val path = request.url.encodedPath

        // refresh 루프 방지 & 로그인/회원가입/헬스체크는 스킵
        if ((code != 401 && code != 403) ||
            path.startsWith(refreshEndpoint) ||
            path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/health")
        ) {
            return originalResponse
        }

        // 본문 소비 방지
        originalResponse.close()

        // 누군가 이미 갱신 중이면 잠깐 대기 → 저장소 토큰으로 재시도
        if (isRefreshing) {
            synchronized(lock) { /* 다른 스레드 갱신 완료 대기 */ }
            val newAccess = readAccessToken()
            return if (!newAccess.isNullOrBlank()) {
                val retried = request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
                chain.proceed(retried)
            } else {
                // 갱신 실패한 경우 → 원요청 그대로 (UnauthorizedInterceptor가 처리)
                chain.proceed(request)
            }
        }

        // 직접 갱신 시도
        val refresh = readRefreshToken()
        if (refresh.isNullOrBlank()) {
            // 리프레시 토큰이 없으면 재시도 불가
            return chain.proceed(request)
        }

        isRefreshing = true
        return try {
            val newAccess = refreshOnce(refresh, chain, refreshEndpoint)
            if (!newAccess.isNullOrBlank()) {
                // 새 토큰 저장 후, 원 요청을 새 토큰으로 재시도
                saveAccessToken(newAccess)
                val retried = request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
                chain.proceed(retried)
            } else {
                // 실패 → 이후 UnauthorizedInterceptor가 세션 정리/로그인 이동
                chain.proceed(request)
            }
        } finally {
            synchronized(lock) { isRefreshing = false }
        }
    }

    private fun readAccessToken(): String? =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            .getString("token", null)

    private fun readRefreshToken(): String? =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            .getString("refresh_token", null)

    private fun saveAccessToken(newToken: String) {
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            .edit().putString("token", newToken).apply()
    }

    private fun saveRefreshToken(newRefresh: String) {
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            .edit().putString("refresh_token", newRefresh).apply()
    }

    /**
     * OkHttp 순수 클라이언트로 /refresh 호출 (AuthInterceptor 비경유)
     * 서버 응답 예시를 폭넓게 처리:
     *  - { "token": "..." }
     *  - { "accessToken": "..." }
     *  - { "result": { "token": "..." } }
     *  - { "data": { "accessToken": "...", "refreshToken": "..." } }
     */
    private fun refreshOnce(
        refreshToken: String,
        chain: Interceptor.Chain,
        endpoint: String
    ): String? {
        val base: HttpUrl = chain.request().url
        val refreshUrl: HttpUrl = base.newBuilder()
            .encodedPath(endpoint) // 호스트/스킴 유지 + 경로만 교체
            .build()

        val payload = JSONObject().apply {
            // ★ 서버 스펙에 맞게 키명 조정 필요할 수 있음:
            // 예: "refreshToken", "refresh_token"
            put("refreshToken", refreshToken)
        }.toString()

        val client = OkHttpClient.Builder()
            // 필요 시 로깅 추가 .addInterceptor(HttpLoggingInterceptor().setLevel(BASIC))
            .build()

        val req: Request = Request.Builder()
            .url(refreshUrl)
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return null
            val raw = res.body?.string() ?: return null

            // 유연 파싱
            val root = JSONObject(raw)
            val container = when {
                root.has("result") -> root.optJSONObject("result") ?: root
                root.has("data")   -> root.optJSONObject("data")   ?: root
                else               -> root
            }

            val newAccess =
                container.optString("token", null)
                    ?: container.optString("accessToken", null)

            val newRefresh =
                container.optString("refreshToken", null)
                    ?: container.optString("refresh_token", null)

            if (!newRefresh.isNullOrBlank()) saveRefreshToken(newRefresh)
            return newAccess
        }
    }
}
//import android.content.Context
//import okhttp3.Interceptor
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.toRequestBody
//import okhttp3.Response
//import org.json.JSONObject
//
///**
// * 401/403이 오면 1회에 한해 refresh 토큰으로 access 토큰을 갱신하고,
// * 원 요청을 새 토큰으로 재시도한다. 실패하면 세션 제거.
// */
//class RetryAuthInterceptor(
//    private val context: Context,
//    private val refreshEndpoint: String,           // e.g. "/api/auth/refresh"
//) : Interceptor {
//
//    // 동시 다발 갱신 방지
//    @Volatile private var isRefreshing = false
//    private val lock = Any()
//
//    override fun intercept(chain: Interceptor.Chain): Response {
//        var request = chain.request()
//        val originalResponse = chain.proceed(request)
//
//        if (originalResponse.code != 401 && originalResponse.code != 403) {
//            return originalResponse
//        }
//        originalResponse.close() // 본문 소모 방지
//
//        // 이미 누군가 갱신 중이면 잠깐 대기 후, 저장소 토큰으로 재시도
//        if (isRefreshing) {
//            synchronized(lock) { /* 다른 스레드 갱신 완료 대기 */ }
//            val newAccess = readAccessToken()
//            return if (!newAccess.isNullOrBlank()) {
//                val retried = request.newBuilder()
//                    .header("Authorization", "Bearer $newAccess")
//                    .build()
//                chain.proceed(retried)
//            } else {
//                chain.proceed(request) // 실패 시 원요청 그대로(아래 UnauthorizedInterceptor가 처리)
//            }
//        }
//
//        // 갱신 시도
//        val refresh = readRefreshToken() ?: return chain.proceed(request)
//        isRefreshing = true
//        return try {
//            val newAccess = refreshOnce(refresh, chain)
//            if (!newAccess.isNullOrBlank()) {
//                // 새 토큰으로 재시도
//                val retried = request.newBuilder()
//                    .header("Authorization", "Bearer $newAccess")
//                    .build()
//                chain.proceed(retried)
//            } else {
//                chain.proceed(request) // 실패 → 이후 UnauthorizedInterceptor가 로그인으로 보냄
//            }
//        } finally {
//            synchronized(lock) { isRefreshing = false }
//        }
//    }
//
//    private fun readAccessToken(): String? =
//        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
//            .getString("token", null)
//
//    private fun readRefreshToken(): String? =
//        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
//            .getString("refresh_token", null)
//
//    private fun saveAccessToken(newToken: String) {
//        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
//            .edit().putString("token", newToken).apply()
//    }
//
//    /**
//     * OkHttp 순수클라이언트로 /refresh 호출 (AuthInterceptor 비경유)
//     * 서버 응답 예시: { "accessToken": "...", "refreshToken": "..." } or BaseResponse<Data>
//     */
//    private fun refreshOnce(refreshToken: String, chain: Interceptor.Chain): String? {
//        val baseUrl = chain.request().url.newBuilder()
//            .encodedPath("/")
//            .build()
//            .toString()
//            .trimEnd('/')
//
//        val url = "$baseUrl$refreshEndpoint"
//
//        val bodyJson = JSONObject().apply {
//            put("refreshToken", refreshToken) // ★ 서버 스펙에 맞게 키명 조정
//        }.toString()
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(HttpLogging.noAuthLogging) // 선택: 로그 보고 싶으면
//            .build()
//
//        val req: Request = Request.Builder()
//            .url(url)
//            .post(bodyJson.toRequestBody("application/json".toMediaType()))
//            .build()
//
//        client.newCall(req).execute().use { res ->
//            if (!res.isSuccessful) return null
//            val raw = res.body?.string() ?: return null
//
//            // ★ 서버 스펙에 맞게 파싱 (BaseResponse 래핑 여부 등)
//            val obj = JSONObject(raw)
//            val data = if (obj.has("data")) obj.getJSONObject("data") else obj
//            val newAccess = data.optString("accessToken", null)
//            val newRefresh = data.optString("refreshToken", null)
//
//            if (!newAccess.isNullOrBlank()) saveAccessToken(newAccess)
//            if (!newRefresh.isNullOrBlank()) {
//                context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
//                    .edit().putString("refresh_token", newRefresh).apply()
//            }
//            return newAccess
//        }
//    }
//}
//
//// (선택) 간단 로거
//private object HttpLogging {
//    val noAuthLogging = okhttp3.logging.HttpLoggingInterceptor().apply {
//        level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
//    }
//}