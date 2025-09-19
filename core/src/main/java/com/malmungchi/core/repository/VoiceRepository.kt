// core/repository/VoiceRepository.kt
package com.malmungchi.core.repository

import com.malmungchi.core.model.AiChatRewardResp
import com.malmungchi.core.model.PromptResp
import com.malmungchi.core.model.VoiceChatResponse
import com.malmungchi.core.model.VoiceHelloResp
import okhttp3.MultipartBody
import okhttp3.RequestBody


interface VoiceRepository {
    suspend fun getPrompts(): PromptResp
    suspend fun voiceHello(): VoiceHelloResp
    suspend fun voiceChat(audio: MultipartBody.Part): VoiceChatResponse

    // daily (신규)
    suspend fun getDailyPrompts(): PromptResp
    suspend fun voiceHelloDaily(): VoiceHelloResp
    suspend fun voiceChatDaily(audio: MultipartBody.Part): VoiceChatResponse

    suspend fun completeAiChatReward(autoTouch: Int = 0): AiChatRewardResp

}

//interface VoiceRepository {
//    suspend fun getPrompts(mode: String = "job"): PromptResp
//    suspend fun voiceHello(mode: String = "job"): VoiceHelloResp   // ★ 추가
//    suspend fun voiceChat(audio: MultipartBody.Part, mode: RequestBody): VoiceChatResp
//}