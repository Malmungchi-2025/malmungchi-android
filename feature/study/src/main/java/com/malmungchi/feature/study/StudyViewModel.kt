package com.malmungchi.feature.study

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.malmungchi.core.network.api.CheckTodayStudyResponse
import com.malmungchi.core.network.api.GptRequest
import com.malmungchi.core.network.api.GptResponse
import com.malmungchi.core.network.api.SaveTodayStudyRequest
import com.malmungchi.core.network.api.SaveTodayStudyResponse
import com.malmungchi.core.network.api.SaveWordRequest   // ✅ 추가
import com.malmungchi.core.network.api.SaveWordResponse  // ✅ 추가
import com.malmungchi.core.network.api.StudyApi
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class StudyReadingViewModel @Inject constructor(
    private val studyApi: StudyApi
) : ViewModel() {

    /** 📌 오늘의 학습 본문 */
    private val _content = mutableStateOf("로딩 중...")
    val content: State<String> = _content

    /** 📌 현재 학습 ID */
    private val _studyId = mutableStateOf<Int?>(null)
    val studyId: State<Int?> = _studyId

    /** 📌 단어 저장 결과 메시지 */
    private val _saveResult = mutableStateOf<String?>(null)
    val saveResult: State<String?> = _saveResult

    // ✅ 오늘 학습 불러오기
    fun loadTodayStudy() {
        studyApi.checkTodayStudy().enqueue(object : Callback<CheckTodayStudyResponse> {
            override fun onResponse(call: Call<CheckTodayStudyResponse>, response: Response<CheckTodayStudyResponse>) {
                if (response.isSuccessful && response.body()?.exists == true) {
                    val data = response.body()!!
                    _content.value = data.content ?: ""
                    _studyId.value = data.study_id
                } else {
                    fetchQuoteFromGPT()
                }
            }

            override fun onFailure(call: Call<CheckTodayStudyResponse>, t: Throwable) {
                _content.value = "❌ 서버 연결 실패: ${t.message}"
            }
        })
    }

    // ✅ GPT 요청 → 오늘 학습 생성
    private fun fetchQuoteFromGPT() {
        val prompt = """
            20대 사회초년생을 위한 문해력 학습용 글 작성.
            - 일상적이고 실무적인 소재 사용
            - 쉬운 단어 위주
            - 전체 480~520자
        """.trimIndent()

        studyApi.getGptResponse(GptRequest(prompt)).enqueue(object : Callback<GptResponse> {
            override fun onResponse(call: Call<GptResponse>, response: Response<GptResponse>) {
                if (response.isSuccessful) {
                    val text = response.body()?.result ?: ""
                    _content.value = text
                    saveTodayStudy(text)
                }
            }

            override fun onFailure(call: Call<GptResponse>, t: Throwable) {
                _content.value = "❌ GPT 요청 실패: ${t.message}"
            }
        })
    }

    // ✅ 오늘 학습 저장 API
    private fun saveTodayStudy(text: String) {
        studyApi.saveTodayStudy(SaveTodayStudyRequest(user_id = null, content = text))
            .enqueue(object : Callback<SaveTodayStudyResponse> {
                override fun onResponse(call: Call<SaveTodayStudyResponse>, response: Response<SaveTodayStudyResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        _studyId.value = response.body()?.study_id
                    }
                }

                override fun onFailure(call: Call<SaveTodayStudyResponse>, t: Throwable) {}
            })
    }

    // ✅ 단어 저장 API (BottomSheet → 서버 → DB)
    fun saveWord(word: String, meaning: String, example: String) {
        val studyId = _studyId.value
        if (studyId == null) {
            _saveResult.value = "❗ study_id가 설정되지 않았습니다."
            return
        }

        val request = SaveWordRequest(word, meaning, example, studyId)
        studyApi.saveWord(request).enqueue(object : Callback<SaveWordResponse> {
            override fun onResponse(call: Call<SaveWordResponse>, response: Response<SaveWordResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    _saveResult.value = "✅ 단어 [$word] 저장 성공!"
                } else {
                    _saveResult.value = "❌ 저장 실패: ${response.body()?.message}"
                }
            }

            override fun onFailure(call: Call<SaveWordResponse>, t: Throwable) {
                _saveResult.value = "❌ 서버 오류: ${t.message}"
            }
        })
    }
}