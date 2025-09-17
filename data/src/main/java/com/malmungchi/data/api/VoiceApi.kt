package com.malmungchi.data.api

import com.malmungchi.core.model.PromptResp
import com.malmungchi.core.model.VoiceChatResp
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*



interface VoiceApi {
    @GET("/api/voice/prompts")
    suspend fun getPrompts(@Query("mode") mode: String = "job"): PromptResp

    @Multipart
    @POST("/api/voice/chat")
    suspend fun voiceChat(
        @Part audio: MultipartBody.Part,
        @Part("mode") mode: RequestBody // "job" | "work" | "daily"
    ): VoiceChatResp
}