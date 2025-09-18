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

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import java.io.File

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isRecording: Boolean = false,
    val isLoading: Boolean = false,
    val botReplyCount: Int = 0,
    val mode: String = "job"
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: VoiceRepository = VoiceRepositoryImpl(app)

    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null

    var ui = androidx.compose.runtime.mutableStateOf(ChatUiState())
        private set

    // ─────────────────────────────────────────────
    // 생명주기 종료 시 안전하게 해제
    // ─────────────────────────────────────────────
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

    // ─────────────────────────────────────────────
    // 녹음 시작
    // ─────────────────────────────────────────────
    fun startRecording() {
        if (ui.value.isRecording || ui.value.isLoading) return
        val ctx = getApplication<Application>()

        // 혹시 남아있던 인스턴스 정리
        safelyReleaseRecorder()

        val file = File.createTempFile("malm_voice_", ".m4a", ctx.cacheDir)
        recordFile = file

        try {
            val rec = MediaRecorder().apply {
                // 필요 시 아래 줄을 VOICE_RECOGNITION으로 바꿔 테스트해보세요.
                // setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
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
        } catch (e: Exception) {
            // 권한 거부, 다른 앱 점유, 기기 정책 등으로 실패할 수 있음
            safelyReleaseRecorder()
            ui.value = ui.value.copy(isRecording = false)
            // 필요 시 사용자 알림(토스트/스낵바) 연결 가능
            // Log.e("ChatViewModel", "startRecording failed", e)
        }
    }

    // ─────────────────────────────────────────────
    // 녹음 종료 + 서버 전송
    // ─────────────────────────────────────────────
    fun stopAndSend() {
        if (!ui.value.isRecording) return

        safelyReleaseRecorder()
        ui.value = ui.value.copy(isRecording = false)

        val file = recordFile ?: return
        ui.value = ui.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    // NOTE: 서버가 m4a를 명시 요구하면 "audio/m4a"로 변경
                    val audioBody = file.asRequestBody("audio/mp4".toMediaType())
                    val part = MultipartBody.Part.createFormData("audio", file.name, audioBody)
                    val modeBody: RequestBody =
                        ui.value.mode.toRequestBody("text/plain".toMediaType())
                    repo.voiceChat(part, modeBody)
                }

                val newMsgs = buildList {
                    addAll(ui.value.messages)
                    if (resp.userText.isNotBlank()) add(ChatMessage(Role.User, resp.userText))
                    val botText = if (resp.hint.isNullOrBlank()) resp.text
                    else resp.text + "\nTIP: " + resp.hint
                    add(ChatMessage(Role.Bot, botText))
                }

                ui.value = ui.value.copy(
                    messages = newMsgs,
                    isLoading = false,
                    botReplyCount = newMsgs.count { it.role == Role.Bot }
                )
            } catch (_: Throwable) {
                ui.value = ui.value.copy(isLoading = false)
            } finally {
                withContext(Dispatchers.IO) { runCatching { file.delete() } }
                recordFile = null
            }
        }
    }

    fun setMode(newMode: String) {
        ui.value = ui.value.copy(mode = newMode)
    }
}

// ─────────────────────────────────────────────
// 하단 마이크 버튼 (권한 요청 + 녹음 토글)
// ─────────────────────────────────────────────
@Composable
fun MicButton(vm: ChatViewModel) {
    val context = LocalContext.current
    val permission = android.Manifest.permission.RECORD_AUDIO

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.startRecording()
        else Toast.makeText(context, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    val granted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    val isBusy = vm.ui.value.isRecording || vm.ui.value.isLoading

    Image(
        painter = painterResource(
            id = if (isBusy) R.drawable.ic_chat_mike_ing else R.drawable.ic_chat_mike
        ),
        contentDescription = "Mic",
        modifier = Modifier
            .size(56.dp)
            .clickable {
                if (vm.ui.value.isLoading) return@clickable
                if (!vm.ui.value.isRecording) {
                    if (granted) vm.startRecording() else launcher.launch(permission)
                } else {
                    vm.stopAndSend()
                }
            }
    )
}




//package com.malmungchi.feature.ai
//
//import android.app.Application
//import android.media.MediaRecorder
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.malmungchi.core.repository.VoiceRepository
//import com.malmungchi.data.implementation.repository.VoiceRepositoryImpl
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
//import android.content.pm.PackageManager
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//
//import java.io.File
//
//data class ChatUiState(
//    val messages: List<ChatMessage> = emptyList(),   // ★ 초기 비움
//    //val messages: List<ChatMessage> = listOf(ChatMessage(Role.Bot, "[면접 상황]\n: 본인의 장단점이 무엇인가요?")),
//    val isRecording: Boolean = false,
//    val isLoading: Boolean = false,       // 서버 왕복 중 마이크 아이콘 바꾸기 용
//    val botReplyCount: Int = 0,           // 초기 Bot 1개
//    val mode: String = "job"
//)
//
//class ChatViewModel(app: Application) : AndroidViewModel(app) {
//
//    private val repo: VoiceRepository = VoiceRepositoryImpl(app)
//
//    private var recorder: MediaRecorder? = null
//    private var recordFile: File? = null
//
//    var ui = androidx.compose.runtime.mutableStateOf(ChatUiState())
//        private set
//
//    // ─────────────────────────────────────────────
//    // 녹음 시작
//    // ─────────────────────────────────────────────
//    fun startRecording() {
//        if (ui.value.isRecording || ui.value.isLoading) return
//        val ctx = getApplication<Application>()
//        val file = File.createTempFile("malm_voice_", ".m4a", ctx.cacheDir)
//        recordFile = file
//
//        val rec = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setAudioEncodingBitRate(128_000)
//            setAudioSamplingRate(44_100)
//            setOutputFile(file.absolutePath)
//            prepare()
//            start()
//        }
//        recorder = rec
//        ui.value = ui.value.copy(isRecording = true)
//    }
//
//    // ─────────────────────────────────────────────
//    // 녹음 종료 + 서버 전송
//    // ─────────────────────────────────────────────
//    fun stopAndSend() {
//        if (!ui.value.isRecording) return
//
//        // 녹음 정지는 예외와 상태를 분리해 안전하게
//        try { recorder?.stop() } catch (_: Throwable) { /* ignore */ }
//        try { recorder?.reset() } catch (_: Throwable) { /* ignore */ }
//        try { recorder?.release() } catch (_: Throwable) { /* ignore */ }
//        recorder = null
//        ui.value = ui.value.copy(isRecording = false)
//
//        val file = recordFile ?: return
//        ui.value = ui.value.copy(isLoading = true)
//
//        viewModelScope.launch {
//            try {
//                val resp = withContext(Dispatchers.IO) {
//                    // NOTE: 서버가 m4a를 명시 요구하면 "audio/m4a"로 바꾸세요.
//                    val audioBody = file.asRequestBody("audio/mp4".toMediaType())
//                    val part = MultipartBody.Part.createFormData("audio", file.name, audioBody)
//                    val modeBody: RequestBody = ui.value.mode.toRequestBody("text/plain".toMediaType())
//                    repo.voiceChat(part, modeBody)
//                }
//
//                val newMsgs = buildList {
//                    addAll(ui.value.messages)
//                    if (resp.userText.isNotBlank()) add(ChatMessage(Role.User, resp.userText))
//                    val botText = if (resp.hint.isNullOrBlank()) resp.text else resp.text + "\nTIP: " + resp.hint
//                    add(ChatMessage(Role.Bot, botText))
//                }
//
//                ui.value = ui.value.copy(
//                    messages = newMsgs,
//                    isLoading = false,
//                    botReplyCount = newMsgs.count { it.role == Role.Bot }
//                )
//            } catch (e: Throwable) {
//                // TODO: 필요하면 이벤트/콜백으로 UI에 에러 알림
//                ui.value = ui.value.copy(isLoading = false)
//            } finally {
//                withContext(Dispatchers.IO) { runCatching { file.delete() } }
//                recordFile = null
//            }
//        }
//    }
////    fun stopAndSend() {
////        if (!ui.value.isRecording) return
////        try {
////            recorder?.run { stop(); reset(); release() }
////        } catch (_: Throwable) { /* ignore */ }
////        recorder = null
////        ui.value = ui.value.copy(isRecording = false)
////
////        val file = recordFile ?: return
////        ui.value = ui.value.copy(isLoading = true)
////
////        viewModelScope.launch {
////            val resp = withContext(Dispatchers.IO) {
////                // 멀티파트 구성
////                val audioBody = file.asRequestBody("audio/mp4".toMediaType())
////                val part = MultipartBody.Part.createFormData("audio", file.name, audioBody)
////                val modeBody: RequestBody = ui.value.mode.toRequestBody("text/plain".toMediaType())
////                runCatching { repo.voiceChat(part, modeBody) }.getOrElse { throw it }
////            }
////
////            // 서버 응답을 UI 메시지로 반영
////            val newMsgs = buildList {
////                addAll(ui.value.messages)
////                // 사용자 음성 → STT 결과를 user 말풍선(흰색)으로
////                if (resp.userText.isNotBlank()) add(ChatMessage(Role.User, resp.userText))
////                // Bot 답변 + TIP(있으면 말풍선 내부에 들어가도록 "TIP: ..."을 본문 뒤에 붙임)
////                val botText = if (resp.hint.isNullOrBlank()) resp.text
////                else resp.text + "\nTIP: " + resp.hint
////                add(ChatMessage(Role.Bot, botText))
////            }
////
////            ui.value = ui.value.copy(
////                messages = newMsgs,
////                isLoading = false,
////                botReplyCount = newMsgs.count { it.role == Role.Bot }
////            )
////
////            // 임시 파일 정리
////            withContext(Dispatchers.IO) { runCatching { file.delete() } }
////            recordFile = null
////        }
////    }
//
//    fun setMode(newMode: String) {
//        ui.value = ui.value.copy(mode = newMode)
//    }
//}
//
//
//
//
//
//@Composable
//fun MicButton(vm: ChatViewModel) {
//    val context = LocalContext.current
//    val permission = android.Manifest.permission.RECORD_AUDIO
//
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { granted ->
//        if (granted) vm.startRecording()
//        else Toast.makeText(context, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
//    }
//
//    val granted = ContextCompat.checkSelfPermission(context, permission) ==
//            PackageManager.PERMISSION_GRANTED
//    val isBusy = vm.ui.value.isRecording || vm.ui.value.isLoading
//
//    Image(
//        painter = painterResource(
//            id = if (isBusy) R.drawable.ic_chat_mike_ing else R.drawable.ic_chat_mike
//        ),
//        contentDescription = "Mic",
//        modifier = Modifier
//            .size(56.dp)
//            .clickable {
//                if (vm.ui.value.isLoading) return@clickable
//                if (!vm.ui.value.isRecording) {
//                    if (granted) vm.startRecording() else launcher.launch(permission)
//                } else {
//                    vm.stopAndSend()
//                }
//            }
//    )
//}
