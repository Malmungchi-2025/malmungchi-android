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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


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

    // ================================
    // 텍스트 기반 GPT 첫 메시지 시작
    // ================================
    suspend fun voiceChatSendText(text: String): VoiceChatResponse {

        // 1) dummy audio 파일 생성
        val dummyFile = File.createTempFile("dummy_audio_", ".m4a", context.cacheDir)
        dummyFile.writeBytes(ByteArray(1))   // 1바이트짜리 더미 데이터

        // 2) 파일을 Part 형태로 생성 (서버는 audio가 비어도 GPT reply 생성)
        val audioBody = dummyFile
            .asRequestBody("audio/mp4".toMediaType())

        val part = MultipartBody.Part.createFormData(
            "audio",
            dummyFile.name,
            audioBody
        )

        // 3) API 호출
        return api.voiceChat(part)
    }
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
