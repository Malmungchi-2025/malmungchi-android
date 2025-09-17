// data/implementation/repository/VoiceRepositoryImpl.kt
package com.malmungchi.data.implementation.repository

import android.content.Context
import com.malmungchi.core.repository.VoiceRepository
import com.malmungchi.core.model.PromptResp
import com.malmungchi.core.model.VoiceChatResp
import com.malmungchi.data.api.VoiceApi
import com.malmungchi.data.net.RetrofitProvider
import okhttp3.MultipartBody
import okhttp3.RequestBody

class VoiceRepositoryImpl(
    private val context: Context
) : VoiceRepository {

    private val api: VoiceApi by lazy {
        RetrofitProvider.getVoiceApi(context)
    }

    override suspend fun getPrompts(mode: String): PromptResp {
        return api.getPrompts(mode)
    }

    override suspend fun voiceChat(
        audio: MultipartBody.Part,
        mode: RequestBody
    ): VoiceChatResp {
        return api.voiceChat(audio, mode)
    }
}
