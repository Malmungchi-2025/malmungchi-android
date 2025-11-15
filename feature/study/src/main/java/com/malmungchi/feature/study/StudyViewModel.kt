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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update


@HiltViewModel
class StudyReadingViewModel @Inject constructor(
    private val repository: TodayStudyRepository
) : ViewModel() {

    // -------------------------------
    // ✅ 학습 단계 진행도 관리 (progress_step1~3)
    // -------------------------------
    private val _progressLevel = MutableStateFlow(0) // 0~3단계
    val progressLevel: StateFlow<Int> = _progressLevel

    private val _quote = MutableStateFlow("생성 중…")
    val quote: StateFlow<String> = _quote

    private val _selectedWord = MutableStateFlow<WordItem?>(null)
    val selectedWord: StateFlow<WordItem?> = _selectedWord

    private val _studyId = MutableStateFlow<Int?>(null)
    val studyId: StateFlow<Int?> = _studyId

    private val _highlightWords = MutableStateFlow<List<String>>(emptyList())
    val highlightWords: StateFlow<List<String>> = _highlightWords

    private val _studiedDates = MutableStateFlow<Set<String>>(emptySet())
    val studiedDates: StateFlow<Set<String>> = _studiedDates

    // 기존 함수 전체 교체
    fun refreshStudiedDatesForWeek(center: LocalDate) = viewModelScope.launch {
        // API 26 없이 '그 주의 월요일' 구하기 (월=1 … 일=7)
        val dayValue = center.dayOfWeek.value           // 1..7
        val daysBackToMonday = (dayValue - 1).toLong()  // 월요일까지 되돌아갈 일수
        val monday = center.minusDays(daysBackToMonday)

        val days: List<LocalDate> = (0..6).map { monday.plusDays(it.toLong()) }

        // 병렬 조회 (suspend 안전 영역)
        val results: List<Boolean> = coroutineScope {
            days.map { d ->
                async { repository.getStudyByDate(d).isSuccess }
            }.awaitAll()
        }

        _studiedDates.value = days
            .zip(results)               // Pair<LocalDate, Boolean>
            .filter { pair -> pair.second }
            .map { pair -> pair.first.toString() } // "yyyy-MM-dd"
            .toSet()
    }

    /** ✅ 지정 날짜의 통합 학습(글감/필사/단어/퀴즈) 한 번에 바인딩 */
    /** 지난 날짜 통합 조회 (채점결과는 무시해서 세팅) */
    fun fetchPastStudyByDate(date: LocalDate) = viewModelScope.launch {
        _studyId.value = null
        _quote.value = "로딩 중…"
        _savedWords.value = emptyList()
        _highlightWords.value = emptyList()
        _quizList.value = emptyList()

        repository.getStudyByDate(date)
            .onSuccess { b ->
                _studyId.value = b.studyId

                // ✅ 글감 텍스트 정리 (줄바꿈·들여쓰기 제거, 한 문단으로)
                val cleaned = b.content
                    .replace("\r\n", " ")
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replace(Regex("[ \t]+"), " ")
                    .trim()

                _quote.value = cleaned
                _savedWords.value = b.vocabulary
                _highlightWords.update { current ->
                    (current + b.vocabulary.map { it.word }).distinct()
                }

                // ✅ 채점 관련 필드(userChoice, isCorrect)는 버린다
                _quizList.value = b.quizzes.map { q ->
                    q.copy(
                        userChoice = null,
                        isCorrect = null
                    )
                }
            }
            .onFailure { e ->
                // 404면 빈 상태
                if (e is HttpException && e.code() == 404) {
                    _studyId.value = null
                    _quote.value = ""
                    _savedWords.value = emptyList()
                    _highlightWords.value = emptyList()
                    _quizList.value = emptyList()
                } else {
                    _quote.value = "❗ ${e.message ?: "오류가 발생했습니다."}"
                }
            }
    }
//    fun fetchByDate(date: LocalDate) = viewModelScope.launch {
//        // 초기화
//        _studyId.value = null
//        _quote.value = "로딩 중…"
//        _savedWords.value = emptyList()
//        _highlightWords.value = emptyList()
//        _quizList.value = emptyList()
//        savedInputs.clear()
//        _userInput.value = ""
//
//        repository.getStudyByDate(date)
//            .onSuccess { b ->
//                // 기본 바인딩
//                _studyId.value = b.studyId
//                _quote.value = b.content
//
//                _sentences.value = b.content
//                    .replace("\r\n", "\n")
//                    .split(Regex("(?<=[.!?])\\s+|\n+"))
//                    .map(String::trim)
//                    .filter { it.isNotEmpty() }
//
//                _savedWords.value = b.vocabulary
//                _highlightWords.value = b.vocabulary.map { it.word }
//
//                _quizList.value = b.quizzes
//
//                if (b.handwriting.isNotBlank()) {
//                    val parts = b.handwriting.split(" ")
//                    parts.forEachIndexed { index, text -> savedInputs[index] = text }
//                    _userInput.value = savedInputs[0] ?: ""
//                }
//            }
//            .onFailure { e ->
//                if (e is HttpException && e.code() == 404) {
//                    // ✅ 데이터 없음: 에러로 취급하지 않고 "빈 상태"로 세팅
//                    Log.d("API_STUDY_BY_DATE", "ℹ️ 해당 날짜 학습 데이터 없음(404). 빈 상태로 표시.")
//                    _studyId.value = null
//                    _quote.value = ""                 // ← UI에서 "학습한 글감이 없습니다."로 표시됨
//                    _sentences.value = emptyList()
//                    _savedWords.value = emptyList()
//                    _highlightWords.value = emptyList()
//                    _quizList.value = emptyList()
//                    savedInputs.clear()
//                    _userInput.value = ""
//                } else {
//                    // 그 외 에러만 에러로 표기
//                    Log.e("API_STUDY_BY_DATE", "❌ 날짜별 학습 조회 실패: ${e.message}", e)
//                    _quote.value = "❗ ${e.message ?: "오류가 발생했습니다."}"
//                }
//            }
//    }
    /** ✅ 오늘의 학습 글감 API 호출 (토큰 인자 제거) */
    fun fetchTodayQuote() {
        viewModelScope.launch {
            repository.generateTodayQuote()
                .onSuccess {
                    val cleaned = it.content
                        .replace("\r\n", " ")
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replace(Regex("[ \t]+"), " ")
                        .trim()
                    _quote.value = cleaned
                    _studyId.value = it.studyId
                }
                .onFailure { e ->
                    _quote.value = "❗ 오류: ${e.message}"
                }
        }
    }
//    fun fetchTodayQuote() {
//        Log.d("API_FETCH_QUOTE", "📡 [요청] /api/gpt/generate-quote")
//        viewModelScope.launch {
//            repository.generateTodayQuote()
//                .onSuccess {
//                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] studyId=${it.studyId}, level=${it.level}")
//
//                    // ✅ 불필요한 replace 제거하고 원문 그대로 사용
//                    _quote.value = it.content
//                    _studyId.value = it.studyId
//                }
//                .onFailure { e ->
//                    Log.e("API_FETCH_QUOTE", "❌ [응답 실패] ${e.message}", e)
//                    _quote.value = "❗ 오류: ${e.message}"
//                }
//        }
//    }

//    /** ✅ 오늘의 학습 글감 API 호출 (토큰 인자 제거) */
//    fun fetchTodayQuote() {
//        Log.d("API_FETCH_QUOTE", "📡 [요청] /api/gpt/generate-quote")
//        viewModelScope.launch {
//            repository.generateTodayQuote()
//                .onSuccess {
//                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] studyId=${it.studyId}, level=${it.level}")
//
//                    val cleaned = it.content
//                        .replace("\r\n", " ")          // 윈도우 개행 → 공백
//                        .replace("\r", " ")            // 혹시 모를 \r 단독 → 공백
//                        .replace("\n", " ")            // 모든 줄바꿈 → 공백
//                        .replace(Regex("[ \t]+"), " ") // 다중 공백 1칸으로
//                        .trim()
//                    _quote.value = cleaned
//                    _studyId.value = it.studyId
//                }
//                .onFailure { e ->
//                    Log.e("API_FETCH_QUOTE", "❌ [응답 실패] ${e.message}", e)
//                    _quote.value = "❗ 오류: ${e.message}"
//                }
//        }
//    }
//    fun fetchTodayQuote() {
//        Log.d("API_FETCH_QUOTE", "📡 [요청] /api/gpt/generate-quote")
//        viewModelScope.launch {
//            repository.generateTodayQuote()
//                .onSuccess {
//                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] studyId=${it.studyId}, level=${it.level}")
//                    _quote.value = it.content
//                    _studyId.value = it.studyId
//                    // 필요하면 레벨도 상태로 보관해서 UI에 뱃지/라벨 표시
//                    //_level.value = it.level ?: SessionManager.level
//                }
////            repository.generateTodayQuote()
////                .onSuccess {
////                    Log.d("API_FETCH_QUOTE", "✅ [응답 성공] studyId=${it.studyId}")
////                    _quote.value = it.content
////                    _studyId.value = it.studyId
////                }
//                .onFailure { e ->
//                    Log.e("API_FETCH_QUOTE", "❌ [응답 실패] ${e.message}", e)
//                    _quote.value = "❗ 오류: ${e.message}"
//                }
//        }
//    }

//    /** ✅ 단어 검색 (토큰 인자 제거) */
//    fun searchWord(word: String) {
//        Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/vocabulary/search")
//        viewModelScope.launch {
//            repository.searchWordDefinition(word)
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

    /** ✅ 단어 검색 (토큰 인자 제거) */
    fun searchWord(word: String) {
        Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/vocabulary/search")
        viewModelScope.launch {
            repository.searchWordDefinition(word)
                .onSuccess { list ->
                    val first = list.firstOrNull()
                    if (first != null) {
                        Log.d("API_SEARCH_WORD", "✅ [응답 성공] 단어='${first.word}', 뜻='${first.meaning}'")
                        _selectedWord.value = first
                    } else {
                        Log.w("API_SEARCH_WORD", "⚠️ 결과가 비어 있습니다.")
                        _selectedWord.value = null
                    }
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
    // 수정된 함수 본문 ↓
    fun loadVocabularyList(studyId: Int) {
        Log.d("API_LOAD_VOCAB", "📡 [요청] GET /api/vocabulary/$studyId")
        viewModelScope.launch {
            repository.getVocabularyList(studyId)
                .onSuccess { words ->
                    Log.d("API_LOAD_VOCAB", "✅ [응답 성공] 단어 개수=${words.size}")
                    _savedWords.value = words

                    // ✅ 기존 하이라이트를 유지하며 새 단어 누적
                    _highlightWords.update { current ->
                        (current + words.map { w -> w.word }).distinct()
                    }
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
            questionIndex = index, // +1,     // ✅ 필드명 변경
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

    private val _pointRewarded = MutableStateFlow(false)
    val pointRewarded: StateFlow<Boolean> = _pointRewarded

    // (선택) 메시지 필요하면
    private val _rewardMessage = MutableStateFlow<String?>(null)
    val rewardMessage: StateFlow<String?> = _rewardMessage

    /** ✅ 완료 화면 진입 시 한 번만 포인트 지급 */
    fun rewardOnEnterIfNeeded(
        onResult: (success: Boolean, message: String) -> Unit = { _, _ -> }
    ) {
        // 이미 이 세션에서 지급 시도/성공했다면 재호출 안 함 (Recomposition 방지)
        if (_pointRewarded.value) return

        viewModelScope.launch {
            repository.rewardTodayStudy()
                .onSuccess {
                    _pointRewarded.value = true
                    _rewardMessage.value = "포인트 15점 지급 완료!"
                    onResult(true, "포인트 15점 지급 완료!")
                }
                .onFailure { e ->
                    // 서버에서 이미 지급된 날이면 여기로 옴: UI는 조용히 통과해도 됨
                    _pointRewarded.value = true // 재시도 막기 위해 true로 고정
                    val msg = e.message ?: "이미 지급되었거나 오류가 발생했어요."
                    _rewardMessage.value = msg
                    onResult(false, msg)
                }
        }
    }

    /** ✅ 현재 날짜의 학습 단계 조회 */
    fun loadTodayProgress() {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.getStudyProgress(today)
                .onSuccess { level ->
                    _progressLevel.value = level
                    Log.d("PROGRESS", "✅ 오늘 진행 단계: ${level}단계")
                }
                .onFailure { e ->
                    Log.e("PROGRESS", "❌ 단계 조회 실패: ${e.message}")
                    _progressLevel.value = 0
                }
        }
    }

    /** ✅ 특정 단계 완료 시 서버에 반영 (예: step=1,2,3) */
    fun markStepComplete(step: Int) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.updateStudyProgress(today, step)
                .onSuccess {
                    _progressLevel.value = step
                    Log.d("PROGRESS", "✅ ${step}단계 완료 반영 성공")
                }
                .onFailure { e ->
                    Log.e("PROGRESS", "❌ 단계 업데이트 실패: ${e.message}")
                }
        }
    }

    // ✅ 날짜별 진행 단계 (0~4)
    private val _progressMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val progressMap: StateFlow<Map<String, Int>> = _progressMap

    /** ✅ 특정 주(week)의 모든 날짜 진행도 불러오기 (서버 1회 호출 버전) */
    /** ✅ 특정 주(week)의 모든 날짜 진행도 불러오기 (서버 1회 호출 버전) */
    fun refreshStudyProgressForWeek(center: LocalDate) = viewModelScope.launch {
        // 🔹 일요일이면 하루 빼서 전달 (주차 어긋남 보정)
        val correctedCenter = if (center.dayOfWeek.value == 7) center.minusDays(1) else center

        repository.getStudyProgressWeek(correctedCenter)
            .onSuccess { map ->
                val adjusted = map.mapValues { (_, v) -> if (v == 3) 4 else v }
                _progressMap.value = adjusted
                Log.d("PROGRESS_WEEK", "✅ 주간 진행도 로드 성공 (${adjusted.size}일): $adjusted")
            }
            .onFailure { e ->
                Log.e("PROGRESS_WEEK", "❌ 주간 진행도 로드 실패: ${e.message}")
                _progressMap.value = emptyMap()
            }
    }

}

