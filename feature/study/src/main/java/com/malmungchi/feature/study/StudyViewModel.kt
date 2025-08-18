package com.malmungchi.feature.study

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.QuizAnswerRequest
import com.malmungchi.core.model.QuizItem
import com.malmungchi.core.model.WordItem
import com.malmungchi.core.repository.TodayStudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import retrofit2.HttpException


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

    /** ✅ 지정 날짜의 통합 학습(글감/필사/단어/퀴즈) 한 번에 바인딩 */
    fun fetchByDate(date: LocalDate) = viewModelScope.launch {
        // 초기화
        _studyId.value = null
        _quote.value = "로딩 중…"
        _savedWords.value = emptyList()
        _highlightWords.value = emptyList()
        _quizList.value = emptyList()
        savedInputs.clear()
        _userInput.value = ""

        repository.getStudyByDate(date)
            .onSuccess { b ->
                // 기본 바인딩
                _studyId.value = b.studyId
                _quote.value = b.content

                _sentences.value = b.content
                    .replace("\r\n", "\n")
                    .split(Regex("(?<=[.!?])\\s+|\n+"))
                    .map(String::trim)
                    .filter { it.isNotEmpty() }

                _savedWords.value = b.vocabulary
                _highlightWords.value = b.vocabulary.map { it.word }

                _quizList.value = b.quizzes

                if (b.handwriting.isNotBlank()) {
                    val parts = b.handwriting.split(" ")
                    parts.forEachIndexed { index, text -> savedInputs[index] = text }
                    _userInput.value = savedInputs[0] ?: ""
                }
            }
            .onFailure { e ->
                if (e is HttpException && e.code() == 404) {
                    // ✅ 데이터 없음: 에러로 취급하지 않고 "빈 상태"로 세팅
                    Log.d("API_STUDY_BY_DATE", "ℹ️ 해당 날짜 학습 데이터 없음(404). 빈 상태로 표시.")
                    _studyId.value = null
                    _quote.value = ""                 // ← UI에서 "학습한 글감이 없습니다."로 표시됨
                    _sentences.value = emptyList()
                    _savedWords.value = emptyList()
                    _highlightWords.value = emptyList()
                    _quizList.value = emptyList()
                    savedInputs.clear()
                    _userInput.value = ""
                } else {
                    // 그 외 에러만 에러로 표기
                    Log.e("API_STUDY_BY_DATE", "❌ 날짜별 학습 조회 실패: ${e.message}", e)
                    _quote.value = "❗ ${e.message ?: "오류가 발생했습니다."}"
                }
            }
    }

    /** ✅ 오늘의 학습 글감 API 호출 (토큰 인자 제거) */
    fun fetchTodayQuote() {
        Log.d("API_FETCH_QUOTE", "📡 [요청] /api/gpt/generate-quote")
        viewModelScope.launch {
            repository.generateTodayQuote()
                .onSuccess {
                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] studyId=${it.studyId}, level=${it.level}")
                    _quote.value = it.content
                    _studyId.value = it.studyId
                    // 필요하면 레벨도 상태로 보관해서 UI에 뱃지/라벨 표시
                    //_level.value = it.level ?: SessionManager.level
                }
//            repository.generateTodayQuote()
//                .onSuccess {
//                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] studyId=${it.studyId}")
//                    _quote.value = it.content
//                    _studyId.value = it.studyId
//                }
                .onFailure { e ->
                    Log.e("API_FETCH_QUOTE", "❌ [응답 실패] ${e.message}", e)
                    _quote.value = "❗ 오류: ${e.message}"
                }
        }
    }

    /** ✅ 단어 검색 (토큰 인자 제거) */
    fun searchWord(word: String) {
        Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/vocabulary/search")
        viewModelScope.launch {
            repository.searchWordDefinition(word)
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

    /** ✅ 단어 저장 후 하이라이트 갱신 (토큰 인자 제거) */
    fun saveWord(wordItem: WordItem, onSaved: () -> Unit) {
        val id = _studyId.value ?: return
        Log.d("API_SAVE_WORD", "📡 [요청] POST /api/vocabulary (studyId=$id, word=${wordItem.word})")
        viewModelScope.launch {
            repository.saveWord(id, wordItem)
                .onSuccess {
                    Log.d("API_SAVE_WORD", "✅ [응답 성공] 단어 저장 완료 -> 하이라이트 갱신")
                    loadVocabularyList(id)
                    onSaved()
                }
                .onFailure { e ->
                    Log.e("API_SAVE_WORD", "❌ [응답 실패] ${e.message}", e)
                }
        }
    }

    /** ✅ 노란펜 모드 UI용 (단어 수동 선택) */
    fun setSelectedWord(wordItem: WordItem) {
        Log.d("API_UI", "🟡 [UI 이벤트] 단어 선택: ${wordItem.word}")
        _selectedWord.value = wordItem
    }

    private val _savedWords = MutableStateFlow<List<WordItem>>(emptyList())
    val savedWords: StateFlow<List<WordItem>> = _savedWords

    /** ✅ 서버에서 단어 목록 가져와 전체 데이터 저장 (토큰 인자 제거) */
    fun loadVocabularyList(studyId: Int) {
        Log.d("API_LOAD_VOCAB", "📡 [요청] GET /api/vocabulary/$studyId")
        viewModelScope.launch {
            repository.getVocabularyList(studyId)
                .onSuccess { words ->
                    Log.d("API_LOAD_VOCAB", "✅ [응답 성공] 단어 개수=${words.size}")
                    _savedWords.value = words
                    _highlightWords.value = words.map { it.word }
                }
                .onFailure { e ->
                    Log.e("API_LOAD_VOCAB", "❌ [응답 실패] ${e.message}", e)
                    _savedWords.value = emptyList()
                    _highlightWords.value = emptyList()
                }
        }
    }

    // -------------------------------
    // ✅ 2단계 필사 기능
    // -------------------------------

    fun getInputFor(index: Int): String = savedInputs[index] ?: ""

    private val _sentences = MutableStateFlow<List<String>>(emptyList())
    val sentences: StateFlow<List<String>> = _sentences

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput

    private val savedInputs = mutableMapOf<Int, String>()

    /** ✅ 2단계 전용: 오늘의 학습 글감 + 문장 분리 (토큰 인자 제거) */
    fun initHandwritingStudy() {
        Log.d("API_FETCH_QUOTE_2STEP", "📡 [요청] /api/gpt/generate-quote (필사용)")
        viewModelScope.launch {
            repository.generateTodayQuote()
                .onSuccess {
                    _quote.value = it.content
                    _studyId.value = it.studyId

                    _sentences.value = it.content
                        .replace("\r\n", "\n")
                        .split(Regex("(?<=[.!?])\\s+|\n+"))
                        .map(String::trim)
                        .filter { s -> s.isNotEmpty() }

                    // ✅ studyId가 세팅된 후 필사 데이터 호출
                    fetchHandwriting()
                }
                .onFailure { e ->
                    _quote.value = "❗ 오류: ${e.message}"
                }
        }
    }


    fun setInputFor(index: Int, value: String) {
        savedInputs[index] = value
        if (index == _currentIndex.value) {
            _userInput.value = value
        }
    }

    /** ✅ 저장된 필사 내용 불러오기 (토큰 인자 제거) */
    fun fetchHandwriting(onLoaded: ((Map<Int, String>) -> Unit)? = null) {
        val id = _studyId.value ?: return
        viewModelScope.launch {
            repository.getHandwriting(id)
                .onSuccess { savedText ->
                    if (savedText.isNotEmpty()) {
                        val parts = savedText.split(" ")
                        parts.forEachIndexed { index, text -> savedInputs[index] = text }
                        _userInput.value = savedInputs[0] ?: ""
                        onLoaded?.invoke(savedInputs.toMap())
                    }
                }
        }
    }

    fun onUserInputChange(input: String) {
        _userInput.value = input
    }

    fun saveCurrentInput() {
        savedInputs[_currentIndex.value] = _userInput.value
    }

    fun nextSentence() {
        saveCurrentInput()
        if (_currentIndex.value < (_sentences.value.size - 1)) {
            _currentIndex.value += 1
            _userInput.value = savedInputs[_currentIndex.value] ?: ""
        }
    }

    fun previousSentence() {
        saveCurrentInput()
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            _userInput.value = savedInputs[_currentIndex.value] ?: ""
        }
    }

    /** ✅ 전체 필사 내용 최종 저장 (토큰 인자 제거) */
    fun finalizeHandwriting(onComplete: () -> Unit) {
        val id = _studyId.value ?: return
        val allText = savedInputs.toSortedMap().values.joinToString(" ")
        viewModelScope.launch {
            repository.saveHandwriting(id, allText)
                .onSuccess {
                    Log.d("API_SAVE_HANDWRITING", "✅ [저장 성공]")
                    onComplete()
                }
                .onFailure { e ->
                    Log.e("API_SAVE_HANDWRITING", "❌ [저장 실패] ${e.message}", e)
                }
        }
    }

    // -------------------------------
    // ✅ 퀴즈
    // -------------------------------

    private val _quizList = MutableStateFlow<List<QuizItem>>(emptyList())
    val quizList: StateFlow<List<QuizItem>> = _quizList

    fun tryGenerateQuiz() {
        val text = quote.value
        val id = studyId.value
        if (!text.isNullOrBlank() && id != null) {
            Log.d("QUIZ", "🧠 generateQuiz 호출 준비 완료 - studyId=$id")
            generateQuiz(text, id)
        } else {
            Log.w("QUIZ", "❌ generateQuiz 호출 실패 - quote or studyId null")
        }
    }

    fun generateQuiz(text: String, studyId: Int) {
        viewModelScope.launch {
            repository.generateQuiz(studyId, text)
                .onSuccess { _quizList.value = it }
                .onFailure { Log.e("QUIZ", "❌ 퀴즈 생성 실패: ${it.message}") }
        }
    }

    fun loadQuizList(studyId: Int) {
        viewModelScope.launch {
            Log.d("QUIZ", "📡 GET /api/quiz/$studyId")
            repository.getQuizList(studyId)
                .onSuccess { list ->
                    Log.d("QUIZ", "✅ 퀴즈 조회 성공: ${list.size}개")
                    _quizList.value = list
                }
                .onFailure { e ->
                    Log.e("QUIZ", "❌ 퀴즈 조회 실패: ${e.message}", e)
                }
        }
    }

    fun submitQuizAnswer(studyId: Int, index: Int, userChoice: String) {
        val req = QuizAnswerRequest(
            studyId = studyId,
            questionIndex = index +1,     // ✅ 필드명 변경
            userChoice = userChoice
        )
        viewModelScope.launch {
            repository.saveQuizAnswer(req)
                .onSuccess { Log.d("QUIZ", "✅ 응답 저장 완료")
                    Log.d("QUIZ", "📡 GET /api/gpt/quiz/$studyId")}
                .onFailure { Log.e("QUIZ", "❌ 응답 저장 실패: ${it.message}")
                    Log.d("QUIZ", "📡 GET /api/gpt/quiz/$studyId")}
        }
    }
}

//@HiltViewModel
//class StudyReadingViewModel @Inject constructor(
//    private val repository: TodayStudyRepository
//) : ViewModel() {
//
//    // ✅ 기본 토큰 (임시, 로그인 붙이기 전까지)
//   private val devToken get() = com.malmungchi.data.api.DevAuth.TOKEN
//
////    private val tokenFromStore = authPreference.token ?: DevAuth.TOKEN -> 로그인 구현시 수정.
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
//    /** ✅ 오늘의 학습 글감 API 호출 */
//    fun fetchTodayQuote(token: String) {
//        Log.d("API_FETCH_QUOTE", "📡 [요청] /api/gpt/generate-quote")
//        viewModelScope.launch {
//            repository.generateTodayQuote(token)
//                .onSuccess {
//                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] content='${it.content}', studyId=${it.studyId}")
//                    _quote.value = it.content
//                    _studyId.value = it.studyId
//                }
//                .onFailure { e ->
//                    Log.e("API_FETCH_QUOTE", "❌ [응답 실패] ${e.message}", e)
//                    _quote.value = "❗ 오류: ${e.message}"
//                }
//        }
//    }
//
//    /** ✅ 단어 검색 */
//    fun searchWord(token: String, word: String) {
//        Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/vocabulary/search")
//        viewModelScope.launch {
//            repository.searchWordDefinition(token, word)
//                .onSuccess {
//                    Log.d("API_SEARCH_WORD", "✅ [응답 성공] 단어='${it.word}', 뜻='${it.meaning}'")
//                    _selectedWord.value = it
//                }
//                .onFailure { e ->
//                    Log.e("API_SEARCH_WORD", "❌ [응답 실패] ${e.message}", e)
//                    _selectedWord.value = null
//                }
//        }
//    }
//
//    /** ✅ 단어 저장 후 하이라이트 갱신 */
//    fun saveWord(token: String, wordItem: WordItem, onSaved: () -> Unit) {
//        val id = _studyId.value ?: return
//        Log.d("API_SAVE_WORD", "📡 [요청] POST /api/vocabulary (studyId=$id, word=${wordItem.word})")
//        viewModelScope.launch {
//            repository.saveWord(token, id, wordItem)
//                .onSuccess {
//                    Log.d("API_SAVE_WORD", "✅ [응답 성공] 단어 저장 완료 -> 하이라이트 갱신")
//                    loadVocabularyList(token, id)
//                    onSaved()
//                }
//                .onFailure { e ->
//                    Log.e("API_SAVE_WORD", "❌ [응답 실패] ${e.message}", e)
//                }
//        }
//    }
//
//
//
//    /** ✅ 노란펜 모드 UI용 (단어 수동 선택) */
//    fun setSelectedWord(wordItem: WordItem) {
//        Log.d("API_UI", "🟡 [UI 이벤트] 단어 선택: ${wordItem.word}")
//        _selectedWord.value = wordItem
//    }
//
//    private val _savedWords = MutableStateFlow<List<WordItem>>(emptyList())
//    val savedWords: StateFlow<List<WordItem>> = _savedWords
//
//    /** ✅ 서버에서 단어 목록 가져와 전체 데이터 저장 */
//    fun loadVocabularyList(token: String, studyId: Int) {
//        Log.d("API_LOAD_VOCAB", "📡 [요청] GET /api/vocabulary/$studyId")
//        viewModelScope.launch {
//            repository.getVocabularyList(token, studyId)
//                .onSuccess { words ->
//                    Log.d("API_LOAD_VOCAB", "✅ [응답 성공] 단어 개수=${words.size}")
//                    _savedWords.value = words // ✅ 전체 데이터 저장
//                    _highlightWords.value = words.map { it.word } // 기존 하이라이트용도 유지
//                }
//                .onFailure { e ->
//                    Log.e("API_LOAD_VOCAB", "❌ [응답 실패] ${e.message}", e)
//                    _savedWords.value = emptyList()
//                    _highlightWords.value = emptyList()
//                }
//        }
//    }
//
//    // -------------------------------
//    // ✅ 2단계 필사 기능
//    // -------------------------------
//    fun getInputFor(index: Int): String = savedInputs[index] ?: ""
//
//    /** 🔥 문장 리스트 & 현재 인덱스 관리 */
//    private val _sentences = MutableStateFlow<List<String>>(emptyList())
//    val sentences: StateFlow<List<String>> = _sentences
//
//    private val _currentIndex = MutableStateFlow(0)
//    val currentIndex: StateFlow<Int> = _currentIndex
//
//    /** 🖋️ 현재 입력 중인 문장 */
//    private val _userInput = MutableStateFlow("")
//    val userInput: StateFlow<String> = _userInput
//
//    /** 🔥 사용자 입력 Map */
//    private val savedInputs = mutableMapOf<Int, String>()
//
//    /**
//     * ✅ 오늘의 학습 글감 불러오기 + 문장 분리
//     */
//    /**
//     * ✅ 2단계 전용: 오늘의 학습 글감 + 문장 분리
//     */
//    fun initHandwritingStudy(token: String) {
//        Log.d("API_FETCH_QUOTE_2STEP", "📡 [요청] /api/gpt/generate-quote (필사용)")
//        viewModelScope.launch {
//            repository.generateTodayQuote(token)
//                .onSuccess {
//                    _quote.value = it.content
//                    _studyId.value = it.studyId
//
//                    _sentences.value = it.content
//                        .replace("\r\n", "\n") // ✅ 줄바꿈 유지
//                        .split(Regex("(?<=[.!?])\\s+|\n+")) // ✅ 문장부호 또는 줄바꿈 기준으로 분리
//                        .map { s -> s.trim() }
//                        .filter { s -> s.isNotEmpty() }
//
//                    // ✅ studyId가 세팅된 후 필사 데이터 호출
//                    fetchHandwriting(token)
//                }
//                .onFailure { e ->
//                    _quote.value = "❗ 오류: ${e.message}"
//                }
//        }
//    }
//
//    /**
//     * ✅ 저장된 필사 내용 불러오기 (서버 → UI)
//     */
//    fun fetchHandwriting(token: String, onLoaded: ((Map<Int, String>) -> Unit)? = null) {
//        val id = _studyId.value ?: return
//        viewModelScope.launch {
//            repository.getHandwriting(token, id)
//                .onSuccess { savedText ->
//                    if (savedText.isNotEmpty()) {
//                        val parts = savedText.split(" ")
//                        parts.forEachIndexed { index, text -> savedInputs[index] = text }
//                        _userInput.value = savedInputs[0] ?: ""
//
//                        // ✅ UI 쪽 savedInputs에도 반영
//                        onLoaded?.invoke(savedInputs.toMap())
//                    }
//                }
//        }
//    }
//
//    /** ✅ 입력 변경 */
//    fun onUserInputChange(input: String) {
//        _userInput.value = input
//    }
//
//    /** ✅ 현재 문장 저장 (Map에만) */
//    fun saveCurrentInput() {
//        savedInputs[_currentIndex.value] = _userInput.value
//    }
//
//    /** ✅ 다음 문장 이동 */
//    fun nextSentence() {
//        saveCurrentInput()
//        if (_currentIndex.value < (_sentences.value.size - 1)) {
//            _currentIndex.value += 1
//            _userInput.value = savedInputs[_currentIndex.value] ?: ""
//        }
//    }
//
//    /** ✅ 이전 문장 이동 */
//    fun previousSentence() {
//        saveCurrentInput()
//        if (_currentIndex.value > 0) {
//            _currentIndex.value -= 1
//            _userInput.value = savedInputs[_currentIndex.value] ?: ""
//        }
//    }
//
//    /**
//     * ✅ 전체 필사 내용 최종 저장
//     */
//    fun finalizeHandwriting(token: String, onComplete: () -> Unit) {
//        val id = _studyId.value ?: return
//        val allText = savedInputs.toSortedMap().values.joinToString(" ")
//        viewModelScope.launch {
//            repository.saveHandwriting(token, id, allText)
//                .onSuccess {
//                    Log.d("API_SAVE_HANDWRITING", "✅ [저장 성공]")
//                    onComplete()
//                }
//                .onFailure { e ->
//                    Log.e("API_SAVE_HANDWRITING", "❌ [저장 실패] ${e.message}", e)
//                }
//        }
//    }
//    // 🔥 ViewModel에 문장별 입력 Map을 외부에서 받을 수 있도록 추가
//    fun saveAllInputs(inputs: Map<Int, String>) {
//        savedInputs.clear()
//        savedInputs.putAll(inputs)
//    }
//
//    //퀴즈
//    private val _quizList = MutableStateFlow<List<QuizItem>>(emptyList())
//    val quizList: StateFlow<List<QuizItem>> = _quizList
//
//    fun tryGenerateQuiz(token: String) {
//        val text = quote.value
//        val id = studyId.value
//
//        if (!text.isNullOrBlank() && id != null) {
//            Log.d("QUIZ", "🧠 generateQuiz 호출 준비 완료 - studyId=$id")
//            generateQuiz(token, text, id)
//        } else {
//            Log.w("QUIZ", "❌ generateQuiz 호출 실패 - quote or studyId null")
//        }
//    }
//
//
//    fun generateQuiz(token: String, text: String, studyId: Int) {
//        viewModelScope.launch {
//            repository.generateQuiz(token, studyId, text)
//                .onSuccess { _quizList.value = it }
//                .onFailure { Log.e("QUIZ", "❌ 퀴즈 생성 실패: ${it.message}") }
//        }
//    }
//
//
//
////    fun loadQuizList(token: String, studyId: Int) {
////        viewModelScope.launch {
////            repository.getQuizList(token, studyId)
////                .onSuccess { _quizList.value = it }
////                .onFailure { Log.e("QUIZ", "❌ 퀴즈 조회 실패: ${it.message}") }
////        }
////    }
//    fun loadQuizList(token: String, studyId: Int) {
//        viewModelScope.launch {
//            android.util.Log.d("QUIZ", "📡 GET /api/gpt/quiz/$studyId (Authorization=Bearer ...)")
//            repository.getQuizList(token, studyId)
//                .onSuccess { list ->
//                    android.util.Log.d("QUIZ", "✅ 퀴즈 조회 성공: ${list.size}개")
//                    _quizList.value = list
//                }
//                .onFailure { e ->
//                    android.util.Log.e("QUIZ", "❌ 퀴즈 조회 실패: ${e.message}", e)
//                }
//        }
//    }
//
//    fun submitQuizAnswer(token: String, studyId: Int, index: Int, userChoice: String, answer: String) {
//        val isCorrect = userChoice == answer
//        val request = QuizAnswerRequest(studyId, index, userChoice, isCorrect)
//        viewModelScope.launch {
//            repository.saveQuizAnswer(token, request)
//                .onSuccess { Log.d("QUIZ", "✅ 응답 저장 완료") }
//                .onFailure { Log.e("QUIZ", "❌ 응답 저장 실패: ${it.message}") }
//        }
//    }
//}




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