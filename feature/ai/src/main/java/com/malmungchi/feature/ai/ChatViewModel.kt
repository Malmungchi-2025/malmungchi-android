package com.malmungchi.feature.ai

import android.app.Application
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.repository.VoiceRepository
import com.malmungchi.data.implementation.repository.VoiceRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.File

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(ChatMessage(Role.Bot, "[면접 상황]\n: 본인의 장단점이 무엇인가요?")),
    val isRecording: Boolean = false,
    val isLoading: Boolean = false,       // 서버 왕복 중 마이크 아이콘 바꾸기 용
    val botReplyCount: Int = 1,           // 초기 Bot 1개
    val mode: String = "job"
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: VoiceRepository = VoiceRepositoryImpl(app)

    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null

    var ui = androidx.compose.runtime.mutableStateOf(ChatUiState())
        private set

    // ─────────────────────────────────────────────
    // 녹음 시작
    // ─────────────────────────────────────────────
    fun startRecording() {
        if (ui.value.isRecording || ui.value.isLoading) return
        val ctx = getApplication<Application>()
        val file = File.createTempFile("malm_voice_", ".m4a", ctx.cacheDir)
        recordFile = file

        val rec = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = rec
        ui.value = ui.value.copy(isRecording = true)
    }

    // ─────────────────────────────────────────────
    // 녹음 종료 + 서버 전송
    // ─────────────────────────────────────────────
    fun stopAndSend() {
        if (!ui.value.isRecording) return
        try {
            recorder?.run { stop(); reset(); release() }
        } catch (_: Throwable) { /* ignore */ }
        recorder = null
        ui.value = ui.value.copy(isRecording = false)

        val file = recordFile ?: return
        ui.value = ui.value.copy(isLoading = true)

        viewModelScope.launch {
            val resp = withContext(Dispatchers.IO) {
                // 멀티파트 구성
                val audioBody = file.asRequestBody("audio/mp4".toMediaType())
                val part = MultipartBody.Part.createFormData("audio", file.name, audioBody)
                val modeBody: RequestBody = ui.value.mode.toRequestBody("text/plain".toMediaType())
                runCatching { repo.voiceChat(part, modeBody) }.getOrElse { throw it }
            }

            // 서버 응답을 UI 메시지로 반영
            val newMsgs = buildList {
                addAll(ui.value.messages)
                // 사용자 음성 → STT 결과를 user 말풍선(흰색)으로
                if (resp.userText.isNotBlank()) add(ChatMessage(Role.User, resp.userText))
                // Bot 답변 + TIP(있으면 말풍선 내부에 들어가도록 "TIP: ..."을 본문 뒤에 붙임)
                val botText = if (resp.hint.isNullOrBlank()) resp.text
                else resp.text + "\nTIP: " + resp.hint
                add(ChatMessage(Role.Bot, botText))
            }

            ui.value = ui.value.copy(
                messages = newMsgs,
                isLoading = false,
                botReplyCount = newMsgs.count { it.role == Role.Bot }
            )

            // 임시 파일 정리
            withContext(Dispatchers.IO) { runCatching { file.delete() } }
            recordFile = null
        }
    }

    fun setMode(newMode: String) {
        ui.value = ui.value.copy(mode = newMode)
    }
}
