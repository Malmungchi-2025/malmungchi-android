package com.malmungchi.feature.study

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.WordItem
import com.malmungchi.core.repository.TodayStudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyReadingViewModel @Inject constructor(
    private val repository: TodayStudyRepository
) : ViewModel() {

    private val _quote = MutableStateFlow("생성 중…")
    val quote: StateFlow<String> = _quote

    private val _selectedWord = MutableStateFlow<WordItem?>(null)
    val selectedWord: StateFlow<WordItem?> = _selectedWord

    private val _studyId = MutableStateFlow<Int?>(null)
    val studyId: StateFlow<Int?> = _studyId

    private val _highlightWords = MutableStateFlow<List<String>>(emptyList())
    val highlightWords: StateFlow<List<String>> = _highlightWords

    /** ✅ 오늘의 학습 글감 API 호출 */
    fun fetchTodayQuote(token: String) {
        Log.d("API_FETCH_QUOTE", "📡 [요청] /api/gpt/generate-quote")
        viewModelScope.launch {
            repository.generateTodayQuote(token)
                .onSuccess {
                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] content='${it.content}', studyId=${it.studyId}")
                    _quote.value = it.content
                    _studyId.value = it.studyId
                }
                .onFailure { e ->
                    Log.e("API_FETCH_QUOTE", "❌ [응답 실패] ${e.message}", e)
                    _quote.value = "❗ 오류: ${e.message}"
                }
        }
    }

    /** ✅ 단어 검색 */
    fun searchWord(token: String, word: String) {
        Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/vocabulary/search")
        viewModelScope.launch {
            repository.searchWordDefinition(token, word)
                .onSuccess {
                    Log.d("API_SEARCH_WORD", "✅ [응답 성공] 단어='${it.word}', 뜻='${it.meaning}'")
                    _selectedWord.value = it
                }
                .onFailure { e ->
                    Log.e("API_SEARCH_WORD", "❌ [응답 실패] ${e.message}", e)
                    _selectedWord.value = null
                }
        }
    }

    /** ✅ 단어 저장 후 하이라이트 갱신 */
    fun saveWord(token: String, wordItem: WordItem, onSaved: () -> Unit) {
        val id = _studyId.value ?: return
        Log.d("API_SAVE_WORD", "📡 [요청] POST /api/vocabulary (studyId=$id, word=${wordItem.word})")
        viewModelScope.launch {
            repository.saveWord(token, id, wordItem)
                .onSuccess {
                    Log.d("API_SAVE_WORD", "✅ [응답 성공] 단어 저장 완료 -> 하이라이트 갱신")
                    loadVocabularyList(token, id)
                    onSaved()
                }
                .onFailure { e ->
                    Log.e("API_SAVE_WORD", "❌ [응답 실패] ${e.message}", e)
                }
        }
    }

    /** ✅ 서버에서 단어 목록 가져와 하이라이트 적용 */
    fun loadVocabularyList(token: String, studyId: Int) {
        Log.d("API_LOAD_VOCAB", "📡 [요청] GET /api/vocabulary/$studyId")
        viewModelScope.launch {
            repository.getVocabularyList(token, studyId)
                .onSuccess { words ->
                    Log.d("API_LOAD_VOCAB", "✅ [응답 성공] 단어 개수=${words.size} -> 하이라이트 적용")
                    _highlightWords.value = words.map { it.word }
                }
                .onFailure { e ->
                    Log.e("API_LOAD_VOCAB", "❌ [응답 실패] ${e.message}", e)
                    _highlightWords.value = emptyList()
                }
        }
    }

    /** ✅ 노란펜 모드 UI용 (단어 수동 선택) */
    fun setSelectedWord(wordItem: WordItem) {
        Log.d("API_UI", "🟡 [UI 이벤트] 단어 선택: ${wordItem.word}")
        _selectedWord.value = wordItem
    }
}

//@HiltViewModel
//class StudyReadingViewModel @Inject constructor(
//    private val repository: TodayStudyRepository
//) : ViewModel() {
//
//    private val _quote = MutableStateFlow("생성 중…")
//    val quote: StateFlow<String> = _quote
//
//    private val _selectedWord = MutableStateFlow<WordItem?>(null)
//    val selectedWord: StateFlow<WordItem?> = _selectedWord
//
//    private val _studyId = MutableStateFlow<Int?>(null)
//    val studyId: StateFlow<Int?> = _studyId
//
//    private val _highlightWords = MutableStateFlow<List<String>>(emptyList())
//    val highlightWords: StateFlow<List<String>> = _highlightWords
//
//    /** ✅ 오늘의 학습 글감 API 호출 (DB 자동 저장) */
//    fun fetchTodayQuote(token: String) {
//        viewModelScope.launch {
//            repository.generateTodayQuote(token)
//                .onSuccess {
//                    _quote.value = it.content
//                    _studyId.value = it.studyId // Repository에서 studyId도 반환하도록 수정 필요
//                }
//                .onFailure { _quote.value = "❗ 오류: ${it.message}" }
//        }
//    }
//
//    /** ✅ 단어 검색 */
//    fun searchWord(token: String, word: String) {
//        viewModelScope.launch {
//            repository.searchWordDefinition(token, word)
//                .onSuccess { _selectedWord.value = it }
//                .onFailure { _selectedWord.value = null }
//        }
//    }
//
//    /** ✅ 단어 저장 후 하이라이트 갱신 */
//    fun saveWord(token: String, wordItem: WordItem, onSaved: () -> Unit) {
//        val id = _studyId.value ?: return
//        viewModelScope.launch {
//            repository.saveWord(token, id, wordItem)
//                .onSuccess { loadVocabularyList(token, id); onSaved() }
//                .onFailure { }
//        }
//    }
//
//    /** ✅ 서버에서 단어 목록 가져와 하이라이트 적용 */
//    fun loadVocabularyList(token: String, studyId: Int) {
//        viewModelScope.launch {
//            repository.getVocabularyList(token, studyId)
//                .onSuccess { words -> _highlightWords.value = words.map { it.word } }
//                .onFailure { _highlightWords.value = emptyList() }
//        }
//    }
//
//    /** ✅ 단어 수동 선택 (노란펜 → UI용) */
//    fun setSelectedWord(wordItem: WordItem) {
//        _selectedWord.value = wordItem
//    }
//}

//@HiltViewModel
//class StudyReadingViewModel @Inject constructor(
//    private val repository: TodayStudyRepository
//) : ViewModel() {
//
//    private val _quote = MutableStateFlow<String>("생성 중…")
//    val quote: StateFlow<String> = _quote
//
//    private val _selectedWord = MutableStateFlow<WordItem?>(null)
//    val selectedWord: StateFlow<WordItem?> = _selectedWord
//
//    // ✅ 오늘의 글귀 불러오기
//    fun fetchTodayQuote(token: String) {
//        viewModelScope.launch {
//            repository.generateTodayQuote(token)
//                .onSuccess { _quote.value = it.content }
//                .onFailure { _quote.value = "❗ 오류: ${it.message}" }
//        }
//    }
//
//    // ✅ 단어 검색
//    fun searchWord(token: String, word: String) {
//        viewModelScope.launch {
//            repository.searchWordDefinition(token, word)
//                .onSuccess { _selectedWord.value = it }
//                .onFailure { _selectedWord.value = null }
//        }
//    }
//
//    // ✅ 단어 저장
//    fun saveWord(token: String, studyId: Int, wordItem: WordItem, onSaved: () -> Unit) {
//        viewModelScope.launch {
//            repository.saveWord(token, studyId, wordItem)
//                .onSuccess { onSaved() }
//                .onFailure { /* 오류 처리 */ }
//        }
//    }
//
//    fun setSelectedWord(wordItem: WordItem) {
//        _selectedWord.value = wordItem
//    }
//}

//
//import androidx.compose.runtime.State
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import com.malmungchi.core.network.api.CheckTodayStudyResponse
//import com.malmungchi.core.network.api.GptRequest
//import com.malmungchi.core.network.api.GptResponse
//import com.malmungchi.core.network.api.SaveTodayStudyRequest
//import com.malmungchi.core.network.api.SaveTodayStudyResponse
//import com.malmungchi.core.network.api.SaveWordRequest   // ✅ 추가
//import com.malmungchi.core.network.api.SaveWordResponse  // ✅ 추가
//import com.malmungchi.core.network.api.StudyApi
//import dagger.hilt.android.lifecycle.HiltViewModel
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import javax.inject.Inject
//
//@HiltViewModel
//class StudyReadingViewModel @Inject constructor(
//    private val studyApi: StudyApi
//) : ViewModel() {
//
//    /** 📌 오늘의 학습 본문 */
//    private val _content = mutableStateOf("로딩 중...")
//    val content: State<String> = _content
//
//    /** 📌 현재 학습 ID */
//    private val _studyId = mutableStateOf<Int?>(null)
//    val studyId: State<Int?> = _studyId
//
//    /** 📌 단어 저장 결과 메시지 */
//    private val _saveResult = mutableStateOf<String?>(null)
//    val saveResult: State<String?> = _saveResult
//
//    // ✅ 오늘 학습 불러오기
//    fun loadTodayStudy() {
//        studyApi.checkTodayStudy().enqueue(object : Callback<CheckTodayStudyResponse> {
//            override fun onResponse(call: Call<CheckTodayStudyResponse>, response: Response<CheckTodayStudyResponse>) {
//                if (response.isSuccessful && response.body()?.exists == true) {
//                    val data = response.body()!!
//                    _content.value = data.content ?: ""
//                    _studyId.value = data.study_id
//                } else {
//                    fetchQuoteFromGPT()
//                }
//            }
//
//            override fun onFailure(call: Call<CheckTodayStudyResponse>, t: Throwable) {
//                _content.value = "❌ 서버 연결 실패: ${t.message}"
//            }
//        })
//    }
//
//    // ✅ GPT 요청 → 오늘 학습 생성
//    private fun fetchQuoteFromGPT() {
//        val prompt = """
//            20대 사회초년생을 위한 문해력 학습용 글 작성.
//            - 일상적이고 실무적인 소재 사용
//            - 쉬운 단어 위주
//            - 전체 480~520자
//        """.trimIndent()
//
//        studyApi.getGptResponse(GptRequest(prompt)).enqueue(object : Callback<GptResponse> {
//            override fun onResponse(call: Call<GptResponse>, response: Response<GptResponse>) {
//                if (response.isSuccessful) {
//                    val text = response.body()?.result ?: ""
//                    _content.value = text
//                    saveTodayStudy(text)
//                }
//            }
//
//            override fun onFailure(call: Call<GptResponse>, t: Throwable) {
//                _content.value = "❌ GPT 요청 실패: ${t.message}"
//            }
//        })
//    }
//
//    // ✅ 오늘 학습 저장 API
//    private fun saveTodayStudy(text: String) {
//        studyApi.saveTodayStudy(SaveTodayStudyRequest(user_id = null, content = text))
//            .enqueue(object : Callback<SaveTodayStudyResponse> {
//                override fun onResponse(call: Call<SaveTodayStudyResponse>, response: Response<SaveTodayStudyResponse>) {
//                    if (response.isSuccessful && response.body()?.success == true) {
//                        _studyId.value = response.body()?.study_id
//                    }
//                }
//
//                override fun onFailure(call: Call<SaveTodayStudyResponse>, t: Throwable) {}
//            })
//    }
//
//    // ✅ 단어 저장 API (BottomSheet → 서버 → DB)
//    fun saveWord(word: String, meaning: String, example: String) {
//        val studyId = _studyId.value
//        if (studyId == null) {
//            _saveResult.value = "❗ study_id가 설정되지 않았습니다."
//            return
//        }
//
//        val request = SaveWordRequest(word, meaning, example, studyId)
//        studyApi.saveWord(request).enqueue(object : Callback<SaveWordResponse> {
//            override fun onResponse(call: Call<SaveWordResponse>, response: Response<SaveWordResponse>) {
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _saveResult.value = "✅ 단어 [$word] 저장 성공!"
//                } else {
//                    _saveResult.value = "❌ 저장 실패: ${response.body()?.message}"
//                }
//            }
//
//            override fun onFailure(call: Call<SaveWordResponse>, t: Throwable) {
//                _saveResult.value = "❌ 서버 오류: ${t.message}"
//            }
//        })
//    }
//}