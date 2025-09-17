// core/repository/VoiceRepository.kt
package com.malmungchi.core.repository

import com.malmungchi.core.model.PromptResp
import com.malmungchi.core.model.VoiceChatResp
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface VoiceRepository {
    suspend fun getPrompts(mode: String = "job"): PromptResp
    suspend fun voiceChat(audio: MultipartBody.Part, mode: RequestBody): VoiceChatResp
}