package com.example.malmungchi

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MalmungchiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "914410e1996d6afee39176f9f8ea782e")
    }
}