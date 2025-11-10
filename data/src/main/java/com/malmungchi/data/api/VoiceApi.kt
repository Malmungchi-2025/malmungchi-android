package com.malmungchi.data.api

import com.malmungchi.core.model.AiChatRewardResp
import com.malmungchi.core.model.PromptResp
import com.malmungchi.core.model.VoiceChatResponse
import com.malmungchi.core.model.VoiceHelloResp
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface VoiceApi {

    @GET("/api/voice/prompts")
    suspend fun getPrompts(): PromptResp

    @GET("/api/voice/hello")
    suspend fun voiceHello(): VoiceHelloResp   // ★ 응답 타입만 새로운 모델

    @Multipart
    @POST("/api/voice/chat")
    suspend fun voiceChat(
        @Part audio: MultipartBody.Part
    ): VoiceChatResponse


    // ✅ 추가: 하루 1회 보상 (autoTouch=1 주면 행 없을 때 자동 생성)
    @POST("/api/gpt/ai-chat/complete-reward")
    suspend fun completeAiChatReward(
        @Query("autoTouch") autoTouch: Int = 0 // 0 or 1
    ): AiChatRewardResp

    // --- daily (신규) ---
    @GET("/api/voice/daily/prompts")
    suspend fun getDailyPrompts(): PromptResp

    @GET("/api/voice/daily/hello")
    suspend fun voiceHelloDaily(): VoiceHelloResp

    @Multipart
    @POST("/api/voice/daily/chat")
    suspend fun voiceChatDaily(@Part audio: MultipartBody.Part): VoiceChatResponse
}

//interface VoiceApi {
//    @GET("/api/voice/prompts")
//    suspend fun getPrompts(@Query("mode") mode: String = "job"): PromptResp
//
//    @GET("/api/voice/hello")                               // ★ 추가
//    suspend fun voiceHello(@Query("mode") mode: String = "job"): VoiceHelloResp
//
//
//    @Multipart
//    @POST("/api/voice/chat")
//    suspend fun voiceChat(
//        @Part audio: MultipartBody.Part,
//        @Part("mode") mode: RequestBody // "job" | "work" | "daily"
//    ): VoiceChatResp
//}