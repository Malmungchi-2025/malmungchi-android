// data/implementation/repository/VoiceRepositoryImpl.kt
package com.malmungchi.data.implementation.repository

import android.content.Context
import com.malmungchi.core.model.AiChatRewardResp
import com.malmungchi.core.repository.VoiceRepository
import com.malmungchi.core.model.PromptResp
import com.malmungchi.core.model.VoiceChatResponse
import com.malmungchi.core.model.VoiceHelloResp
import com.malmungchi.data.api.VoiceApi
import com.malmungchi.data.net.RetrofitProvider
import okhttp3.MultipartBody
import okhttp3.RequestBody


class VoiceRepositoryImpl(
    private val context: Context
) : VoiceRepository {

    private val api: VoiceApi by lazy { RetrofitProvider.getVoiceApi(context) }

    override suspend fun getPrompts(): PromptResp = api.getPrompts()

    override suspend fun voiceHello(): VoiceHelloResp = api.voiceHello()  // ★ 새 모델 사용

    override suspend fun voiceChat(audio: MultipartBody.Part): VoiceChatResponse =
        api.voiceChat(audio)

    // --- daily (신규) ---
    override suspend fun getDailyPrompts(): PromptResp = api.getDailyPrompts()
    override suspend fun voiceHelloDaily(): VoiceHelloResp = api.voiceHelloDaily()
    override suspend fun voiceChatDaily(audio: MultipartBody.Part): VoiceChatResponse = api.voiceChatDaily(audio)

    override suspend fun completeAiChatReward(autoTouch: Int): AiChatRewardResp =
        api.completeAiChatReward(autoTouch)
}

//class VoiceRepositoryImpl(
//    private val context: Context
//) : VoiceRepository {
//
//    private val api: VoiceApi by lazy {
//        RetrofitProvider.getVoiceApi(context)
//    }
//
//    override suspend fun getPrompts(mode: String): PromptResp {
//        return api.getPrompts(mode)
//    }
//
//    override suspend fun voiceHello(mode: String): VoiceHelloResp = api.voiceHello(mode)  // ★ 추가
//
//    override suspend fun voiceChat(
//        audio: MultipartBody.Part,
//        mode: RequestBody
//    ): VoiceChatResp {
//        return api.voiceChat(audio, mode)
//    }
//}
