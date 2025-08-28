package com.malmungchi.data.net


import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 401이면 세션을 정리하고(자동로그인 해제) 앱 레벨 콜백을 호출.
 * 콜백에서 로그인 화면 이동 등을 처리하세요.
 */
class UnauthorizedInterceptor(
    private val context: Context,
    private val onUnauthorized: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val res = chain.proceed(chain.request())
        if (res.code == 401) {
            Log.w("HTTP", "401 Unauthorized: clearing local session")
            context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
                .edit().remove("user_id").remove("token").apply()

            // UI 쪽 콜백은 메인스레드에서
            CoroutineScope(Dispatchers.Main).launch {
                onUnauthorized.invoke()
            }
        }
        return res
    }
}