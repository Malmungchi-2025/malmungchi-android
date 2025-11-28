
package com.malmungchi.feature.ai

import android.app.Application
import android.media.MediaRecorder
import retrofit2.HttpException as RetrofitHttpException
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.repository.VoiceRepository
import com.malmungchi.data.implementation.repository.VoiceRepositoryImpl

// ▼▼▼ 와일드카드 대신 alias 임포트로 'model' 타입만 쓰게 고정 ▼▼▼
import com.malmungchi.feature.ai.model.ChatMessage as MChatMessage
import com.malmungchi.feature.ai.model.ChatUiState as MChatUiState
import com.malmungchi.feature.ai.model.Role as MRole
import com.malmungchi.feature.ai.model.BubbleStyle as MBubbleStyle
// ▲▲▲

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val LISTENING_PLACEHOLDER = "음성 인식 중..."

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    // ====== 모드 분기 ======
    enum class Mode { JOB, DAILY }
    private var currentMode: Mode = Mode.JOB

    fun setModeJob() { currentMode = Mode.JOB }
    fun setModeDaily() { currentMode = Mode.DAILY }

    // ====== 의존성 / 녹음 ======
    private val repo: VoiceRepository = VoiceRepositoryImpl(app)
    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null

    // ====== UI State ======
    var ui = androidx.compose.runtime.mutableStateOf(MChatUiState())
        private set

    override fun onCleared() {
        super.onCleared()
        safelyReleaseRecorder()
    }

    private fun safelyReleaseRecorder() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.reset() }
        runCatching { recorder?.release() }
        recorder = null
    }



    // =========================================================
    // 1) 서버가 먼저 인사/상황 제시 (텍스트 + TTS base64)
    //    - currentMode 에 따라 job/daily 라우팅
    // =========================================================
//    fun loadHello() {
//        viewModelScope.launch {
//
//            runCatching {
//                when (currentMode) {
//                    Mode.JOB   -> repo.voiceHello()
//                    Mode.DAILY -> repo.voiceHelloDaily()
//                }
//            }.onSuccess { resp ->
//                // 상황 + 질문을 하나의 말풍선으로 합침
//                val full = resp.text ?: "[${resp.situation}]\n: ${resp.question}"
//
//                val msgs = ui.value.messages + MChatMessage(
//                    role = MRole.Bot,
//                    text = full,
//                    style = MBubbleStyle.Normal   // 초기 스타터: 테두리 없음
//                )
//
//                ui.value = ui.value.copy(
//                    messages = msgs,
//                    botReplyCount = msgs.count { it.role == MRole.Bot }
//                )
//
//            }
//        }
//    }
    fun loadHello() {
        viewModelScope.launch {
            ui.value = ui.value.copy(isLoading = true)

            val resp = runCatching {
                when (currentMode) {
                    Mode.JOB   -> repo.voiceHello()        // <-- 서버의 실제 hello 사용
                    Mode.DAILY -> repo.voiceHelloDaily()    // <-- daily hello
                }
            }.getOrElse {
                ui.value = ui.value.copy(isLoading = false)
                return@launch
            }

            // 말풍선 텍스트 구성
            val full = resp.text ?: "[${resp.situation}]\n: ${resp.question}"

            val msg = MChatMessage(
                role = MRole.Bot,
                text = full,
                style = MBubbleStyle.Normal      // 첫 메시지는 피드백 없음!
            )

            ui.value = ui.value.copy(
                isLoading = false,
                messages = ui.value.messages + msg,
                botReplyCount = ui.value.messages.count { it.role == MRole.Bot } + 1
            )

            // TODO: resp.audioBase64 있으면 여기서 TTS 재생
        }
    }
//    fun loadHello() {
//        // GPT에게 "첫 상태 생성" 요청 (빈 문자열)
//        viewModelScope.launch {
//            ui.value = ui.value.copy(isLoading = true)
//
//            runCatching {
//                when (currentMode) {
//                    Mode.JOB   -> (repo as VoiceRepositoryImpl).voiceChatSendText("")
//                    Mode.DAILY -> (repo as VoiceRepositoryImpl).voiceChatSendText("")
//                }
//            }.onSuccess { resp ->
//
//                val msg = buildString {
//                    append(resp.text ?: "")
//                    resp.hint?.takeIf { it.isNotBlank() }?.let { append("\nTIP: ").append(it) }
//                    resp.critique?.takeIf { it.isNotBlank() }?.let { append("\n피드백: ").append(it) }
//                }
//
//                val newMsgs = ui.value.messages + MChatMessage(
//                    role = MRole.Bot,
//                    text = msg,
//                    style = if (!resp.hint.isNullOrBlank() || !resp.critique.isNullOrBlank())
//                        MBubbleStyle.BotFeedback else MBubbleStyle.Normal
//                )
//
//                ui.value = ui.value.copy(
//                    isLoading = false,
//                    messages = newMsgs,
//                    botReplyCount = newMsgs.count { it.role == MRole.Bot }
//                )
//            }.onFailure {
//                ui.value = ui.value.copy(isLoading = false)
//            }
//        }
//    }

    // =========================================================
    // 2) 녹음 제어
    // =========================================================
    fun startRecording() {
        if (ui.value.isRecording || ui.value.isLoading) return
        val ctx = getApplication<Application>()

        safelyReleaseRecorder()
        val file = File.createTempFile("malm_voice_", ".m4a", ctx.cacheDir)
        recordFile = file

        try {
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
        } catch (_: Exception) {
            safelyReleaseRecorder()
            ui.value = ui.value.copy(isRecording = false)
        }
    }

    // 녹음 종료 + 업로드
    fun stopAndSend() {
        if (!ui.value.isRecording) return
        safelyReleaseRecorder()

        // 1) 플레이스홀더 표시 (model 타입 고정)
        val withPlaceholder = ui.value.messages + MChatMessage(
            role = MRole.User,
            text = LISTENING_PLACEHOLDER,
            style = MBubbleStyle.Normal
        )
        ui.value = ui.value.copy(isRecording = false, isLoading = true, messages = withPlaceholder)

        val file = recordFile ?: run {
            removePlaceholderAndStopLoading()
            return
        }

        viewModelScope.launch {
            try {

                // 서버 전송 전 음성 인식 시작 상태 true
                ui.value = ui.value.copy(isTranscribing = true)

                // 2) 서버 전송
                val resp = withContext(Dispatchers.IO) {
                    val audioBody = file.asRequestBody("audio/mp4".toMediaType())
                    val part = MultipartBody.Part.createFormData("audio", file.name, audioBody)
                    when (currentMode) {
                        Mode.JOB   -> repo.voiceChat(part)
                        Mode.DAILY -> repo.voiceChatDaily(part)
                    }
                }

                // 3) 플레이스홀더 제거
                val filtered = ui.value.messages.filterNot { it.text == LISTENING_PLACEHOLDER }

                // 4) 실제 메시지 삽입
                val newMsgs = buildList {
                    addAll(filtered)

                    // 사용자 인식 텍스트
                    if (resp.userText.isNotBlank()) {
                        val style = if (resp.needRetry == true)
                            MBubbleStyle.UserRetryNeeded else MBubbleStyle.Normal
                        add(MChatMessage(MRole.User, resp.userText, style))
                    }

                    // Bot 응답 (+TIP / +피드백)
                    val botText = buildString {
                        append(resp.text ?: "") // ← null-safe
                        //append(resp.text)
                        resp.hint?.takeIf { it.isNotBlank() }?.let { append("\nTIP: ").append(it) }
                        resp.critique?.takeIf { it.isNotBlank() }?.let { append("\n피드백: ").append(it) }
                    }

                    // needRetry 여부에 따라 Bot 말풍선 스타일 (피드백 테두리)
//                    val botStyle = if (resp.needRetry == true) MBubbleStyle.BotFeedback else MBubbleStyle.Normal
//                    add(MChatMessage(MRole.Bot, botText, botStyle))
                    val hasFeedback = !resp.hint.isNullOrBlank() || !resp.critique.isNullOrBlank()
                    val botStyle = if (hasFeedback) MBubbleStyle.BotFeedback else MBubbleStyle.Normal
                    add(MChatMessage(MRole.Bot, botText, botStyle))
                }

                ui.value = ui.value.copy(
                    messages = newMsgs,
                    isLoading = false,
                    botReplyCount = newMsgs.count { it.role == MRole.Bot }
                )

                // TODO: resp.audioBase64 재생이 필요하다면 여기서 처리 -> 안 필요함.

            } catch (_: Throwable) {
                removePlaceholderAndStopLoading()
            } finally {

                // 여기! 성공/오류와 상관 없이 음성 인식 종료 false
                ui.value = ui.value.copy(isTranscribing = false)
                withContext(Dispatchers.IO) { runCatching { file.delete() } }
                recordFile = null
            }
        }
    }

    private fun removePlaceholderAndStopLoading() {
        val cleaned = ui.value.messages.filterNot { it.text == LISTENING_PLACEHOLDER }
        ui.value = ui.value.copy(messages = cleaned, isLoading = false)
    }

    // =========================================================
    // 3) 보상 지급 API (완료 화면용)
    // =========================================================
    private val _rewardLoading = MutableStateFlow(false)
    val rewardLoading = _rewardLoading.asStateFlow()

    private val _rewardToast = MutableStateFlow<String?>(null)
    val rewardToast = _rewardToast.asStateFlow() // UI에서 한번 보여주고 null로 초기화

    /**
     * 종료하기 클릭 시 호출: ai 채팅 보상 지급
     * - autoTouch=1 로 안전하게 호출 (today_ai_chat 미리 터치 안 해도 OK)
     * - 이미 지급(400)이어도 완료 플로우는 진행
     */
    fun giveAiChatRewardAndFinish(
        onNavigateFinish: () -> Unit
    ) {
        if (_rewardLoading.value) return
        viewModelScope.launch {
            _rewardLoading.value = true
            try {
                val resp = repo.completeAiChatReward(autoTouch = 1)
                _rewardToast.value = resp.message.ifBlank { "포인트가 지급되었습니다." }
                onNavigateFinish()

            } catch (e: retrofit2.HttpException) {
                val msg = e.serverMsg() ?: "보상 지급 요청 실패"
                when (e.code()) {
                    400 -> { _rewardToast.value = msg; onNavigateFinish() } // 이미 지급됨 등
                    401 -> _rewardToast.value = "로그인이 필요합니다."
                    else -> _rewardToast.value = msg
                }

            } catch (_: IOException) {
                _rewardToast.value = "네트워크 오류가 발생했어요."

            } catch (_: Throwable) {
                _rewardToast.value = "알 수 없는 오류가 발생했어요."

            } finally {
                _rewardLoading.value = false
            }
        }
    }

    fun consumeRewardToast() {
        _rewardToast.value = null
    }
}

// 🔽 파일 하단(클래스 바깥)에 확장 함수 유지
private fun retrofit2.HttpException.serverMsg(): String? = try {
    this.response()
        ?.errorBody()
        ?.string()
        ?.let { body ->
            JSONObject(body).optString("message", /* fallback */ null)
                ?.takeIf { it.isNotBlank() }
        }
} catch (_: Exception) {
    null
}
