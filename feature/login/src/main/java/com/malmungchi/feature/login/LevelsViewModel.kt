package com.malmungchi.feature.login

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.LevelTestQuestion
import com.malmungchi.core.model.LevelsGenerateResult
import com.malmungchi.core.repository.LevelTestRepository
import com.malmungchi.feature.login.LevelGeneratingScreen
import com.malmungchi.feature.login.LevelReadingQuizScreen
import com.malmungchi.feature.login.LevelSetCompleteScreen
import com.malmungchi.feature.login.LevelTestIntroScreen
import dagger.hilt.android.lifecycle.HiltViewModel
//import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class VocabLevel { BASIC, PRACTICAL, ADVANCED, EXPERT }

data class ResultDetail(
    val questionIndex: Int,      // API 기준(1..N) — 없으면 포지션 매칭
    val answerIndex: Int,
    val userChoice: Int,
    val isCorrect: Boolean,
    val explanation: String? = null
)


fun mapResultLevelToKo(code: String): String = when (code.lowercase()) {
    "basic"     -> "기초"
    "practical" -> "실용"
    "advanced"  -> "심화"
    "expert"    -> "고급"
    else        -> "기초"
}

private fun VocabLevel.toStage(): Int = when (this) {
    VocabLevel.BASIC      -> 0
    VocabLevel.PRACTICAL  -> 1
    VocabLevel.ADVANCED   -> 2
    VocabLevel.EXPERT     -> 3
}

/** Intro: 단계 선택 → /levels/start 성공 시 Generating으로 진입 */
@Composable
fun LevelTestIntroRoute(
    onGoGenerating: (stage: Int) -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: LevelsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        when (val s = state) {
            is LevelsUiState.Started -> onGoGenerating(s.stage)
            else -> Unit
        }
    }

    val isStarting = state is LevelsUiState.Starting

    LevelTestIntroScreen(
        onBackClick = onBackClick,
        onLevelChosen = { level ->
            if (!isStarting) viewModel.start(level)
        },
        isSubmitting = isStarting
    )
}



/** Generating: 진입 즉시 /levels/generate → 준비되면 Reading/Quiz 화면으로 */
@Composable
fun LevelGeneratingRoute(
    onReady: (stage: Int, passage: String, questions: List<LevelTestQuestion>) -> Unit,
    onCancel: (() -> Unit)? = null,
    viewModel: LevelsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.generate() }

    when (val s = state) {
        is LevelsUiState.Generating -> {
            LevelGeneratingScreen(
                progressPercent = s.progress,
                onCancel = onCancel
            )
        }
        is LevelsUiState.Ready -> {
            onReady(s.stage, s.passage, s.questions)
        }
        is LevelsUiState.Error -> {
            // 에러 시에도 onCancel 넘길 수 있도록 기본값 있는 시그니처 사용
            LevelGeneratingScreen(progressPercent = 0, onCancel = onCancel)
            // TODO: 에러 UI/리트라이
        }
        else -> {
            // Idle/Starting/Submitting/Result는 보통 도달 X
            LevelGeneratingScreen(progressPercent = 0, onCancel = onCancel)
        }
    }
}

/** Reading/Quiz: 문제 풀고 → 제출 → 결과 수신 시 완료 화면으로 */
@Composable
fun LevelReadingQuizRoute(
    onBackClick: () -> Unit = {},
    onRetry: () -> Unit = {},      // 완료화면 "다시하기" → Intro로
    onGoHome: () -> Unit = {},     // 완료화면 "시작하기" → 바텀네비 첫 탭으로
    viewModel: LevelsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // 완료 화면을 보여줄지 여부(결과 검토 후 '다음으로'를 눌렀을 때 true)
    var showComplete by remember { mutableStateOf(false) }

    when (val s = state) {
        is LevelsUiState.Ready,
        is LevelsUiState.Answering -> {
            val passage = when (s) {
                is LevelsUiState.Ready     -> s.passage
                is LevelsUiState.Answering -> s.passage
                else -> ""
            }
            val questions = when (s) {
                is LevelsUiState.Ready     -> s.questions
                is LevelsUiState.Answering -> s.questions
                else -> emptyList()
            }
            val selected = when (s) {
                is LevelsUiState.Answering -> s.answers
                else -> List(questions.size) { null }
            }

            LevelReadingQuizScreen(
                onBackClick = onBackClick,
                passage = passage,
                questions = questions,
                selectedAnswers = selected,
                onSelectAnswer = { qIdx, choiceIdx -> viewModel.selectAnswer(qIdx, choiceIdx) },
                onShowResult = { answers ->
                    if (answers.all { it != null }) viewModel.submit()
                },
                onNext = { /* 사용 안 함: 결과 수신 후 완료화면으로 이동 */ }
            )
        }

        is LevelsUiState.Submitting -> {
            // 간단 로딩 대체
            LevelGeneratingScreen(progressPercent = 100)
        }

        is LevelsUiState.Result -> {
            if (showComplete) {
                // ⬇️ 결과 레벨 한글 라벨 전달 (3번에서 만든 함수 사용)
                LevelSetCompleteScreen(
                    levelTitle = mapResultLevelToKo(s.resultLevel),
                    onRetry = { viewModel.reset(); showComplete = false; onRetry() },
                    onStart = onGoHome
                )
            } else {
                // 결과 검토 화면(문항/선택 복기) 먼저 보여주기
                LevelReadingQuizScreen(
                    onBackClick = onBackClick,  // 결과에서 back → 퀴즈로(스크린 내부 로직)
                    passage = viewModel.passageForUi(),          // 뷰모델에서 값 노출 함수 제공
                    questions = viewModel.questionsForUi(),
                    selectedAnswers = viewModel.answersForUi(),  // 사용자가 고른 값들
                    resultDetails = s.detail?.map {
                        ResultDetail(
                            questionIndex = it.questionIndex,
                            answerIndex   = it.answerIndex,
                            userChoice    = it.userChoice,
                            isCorrect     = it.isCorrect,
                            explanation   = it.explanation
                        )
                    },
                    onSelectAnswer = { _, _ -> },                // 결과에선 선택 비활성
                    onShowResult = { },                          // 이미 제출됨
                    onNext = { showComplete = true },            // ➜ 완료 화면으로 전환
                    startStage = Stage.RESULT
                )
            }
        }

//        is LevelsUiState.Result -> {
//            // 서버가 이미 레벨 반영(UPDATE users.level) → 사용자는 “시작하기”로 홈 이동
//            LevelSetCompleteScreen(
//                onRetry = {
//                    viewModel.reset()
//                    onRetry()
//                },
//                onStart = {
//                    // 필요하면 여기서 s.resultLevel 사용 가능
//                    onGoHome()
//                }
//            )
//        }

        is LevelsUiState.Error -> {
            // TODO: 에러 UI/리트라이
            LevelGeneratingScreen(progressPercent = 0)
        }

        else -> {
            // Idle/Starting/Generating 등은 이 화면에선 보통 도달 X
            LevelGeneratingScreen(progressPercent = 0)
        }
    }
}

@HiltViewModel
class LevelsViewModel @Inject constructor(
    private val repository: LevelTestRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LevelsUiState>(LevelsUiState.Idle)
    val state: StateFlow<LevelsUiState> = _state

    private var stage: Int = 0
    private var passage: String = ""
    private var questions: List<LevelTestQuestion> = emptyList()
    private var answers: MutableList<Int?> = mutableListOf(null, null, null)

    private var progressJob: Job? = null

    fun passageForUi(): String = passage
    fun questionsForUi(): List<LevelTestQuestion> = questions
    fun answersForUi(): List<Int?> = answers.toList()

    /** Intro → /levels/start */
    fun start(level: VocabLevel) {
        stage = level.toStage()
        _state.value = LevelsUiState.Starting
        viewModelScope.launch {
            repository.levelsStart(stage)
                .onSuccess { _state.value = LevelsUiState.Started(stage) }
                .onFailure { _state.value = LevelsUiState.Error(it.message ?: "시작 실패") }
        }
    }

    /** Generating 진입 → /levels/generate */
    fun generate() {
        _state.value = LevelsUiState.Generating(progress = 0)

        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            var p = 0
            while (p < 85 && state.value is LevelsUiState.Generating) {
                delay(40)
                p += 2
                _state.value = LevelsUiState.Generating(progress = p)
            }
        }

        viewModelScope.launch {
            repository.levelsGenerate(stage)
                .onSuccess { res: LevelsGenerateResult ->
                    progressJob?.cancel()
                    passage = res.passage
                    questions = res.questions
                    answers = MutableList(questions.size) { null }
                    _state.value = LevelsUiState.Ready(stage, passage, questions)
                }
                .onFailure {
                    progressJob?.cancel()
                    _state.value = LevelsUiState.Error(it.message ?: "문제 생성 실패")
                }
        }
    }

    fun selectAnswer(questionIndex: Int, choiceIndex: Int) {
        if (questionIndex !in questions.indices) return
        if (choiceIndex !in 0..3) return
        answers[questionIndex] = choiceIndex
        _state.value = LevelsUiState.Answering(stage, passage, questions, answers.toList())
    }

    /** /levels/submit */
    fun submit() {
        if (answers.any { it == null }) {
            _state.value = LevelsUiState.Error("모든 문항에 답을 선택해주세요.")
            return
        }
        _state.value = LevelsUiState.Submitting
        viewModelScope.launch {
            repository.levelsSubmit(
                stage = stage,
                questions = questions,
                answers = answers.map { it ?: 0 }
            ).onSuccess { res ->
                // res.detail 이 API의 detail 배열이라고 가정
                val reviews: List<AnswerReview>? = res.detail?.map {
                    AnswerReview(
                        questionIndex = it.questionIndex,
                        answerIndex   = it.answerIndex,
                        userChoice    = it.userChoice,
                        isCorrect     = it.isCorrect,
                        explanation   = it.explanation
                    )
                }
                _state.value = LevelsUiState.Result(
                    correctCount = res.correctCount,
                    resultLevel  = res.resultLevel,
                    detail       = reviews
                )
            }.onFailure {
                _state.value = LevelsUiState.Error(it.message ?: "제출 실패")
            }
        }
    }
//    fun submit() {
//        if (answers.any { it == null }) {
//            _state.value = LevelsUiState.Error("모든 문항에 답을 선택해주세요.")
//            return
//        }
//        _state.value = LevelsUiState.Submitting
//        viewModelScope.launch {
//            repository.levelsSubmit(
//                stage = stage,
//                questions = questions,
//                answers = answers.map { it ?: 0 }
//            ).onSuccess { res ->
//                _state.value = LevelsUiState.Result(
//                    correctCount = res.correctCount,
//                    resultLevel  = res.resultLevel
//                )
//            }.onFailure {
//                _state.value = LevelsUiState.Error(it.message ?: "제출 실패")
//            }
//        }
//    }

    fun reset() {
        stage = 0
        passage = ""
        questions = emptyList()
        answers = mutableListOf(null, null, null)
        _state.value = LevelsUiState.Idle
    }
}

/** 화면 상태 */
data class AnswerReview(
    val questionIndex: Int,  // API 기준(로그에 1..N)
    val answerIndex: Int,    // 정답 인덱스 (0..3)
    val userChoice: Int,     // 사용자 선택 (0..3)
    val isCorrect: Boolean,
    val explanation: String? = null
)
sealed interface LevelsUiState {
    data object Idle : LevelsUiState
    data object Starting : LevelsUiState
    data class Started(val stage: Int) : LevelsUiState
    data class Generating(val progress: Int) : LevelsUiState
    data class Ready(
        val stage: Int,
        val passage: String,
        val questions: List<LevelTestQuestion>
    ) : LevelsUiState
    data class Answering(
        val stage: Int,
        val passage: String,
        val questions: List<LevelTestQuestion>,
        val answers: List<Int?> // 0..3
    ) : LevelsUiState

    data object Submitting : LevelsUiState
    //data class Result(val correctCount: Int, val resultLevel: String) : LevelsUiState
    data class Error(val message: String) : LevelsUiState
    data class Result(
        val correctCount: Int,
        val resultLevel: String,
        val detail: List<AnswerReview>? = null
    ) : LevelsUiState
}


//// package com.malmungchi.feature.login
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.malmungchi.core.model.LevelTestQuestion
//import com.malmungchi.core.model.LevelsGenerateResult
//import com.malmungchi.core.repository.LevelTestRepository
//import com.malmungchi.feature.login.LevelReadingQuizScreen
//import com.malmungchi.feature.login.LevelTestIntroScreen
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//enum class VocabLevel { BASIC, PRACTICAL, ADVANCED, EXPERT }
//
//private fun VocabLevel.toStage(): Int = when (this) {
//    VocabLevel.BASIC      -> 0
//    VocabLevel.PRACTICAL  -> 1
//    VocabLevel.ADVANCED   -> 2
//    VocabLevel.EXPERT     -> 3
//}
//
//@Composable
//fun LevelTestIntroRoute(
//    // 다음 화면(Generating)으로 넘어가는 네비게이션 콜백
//    onGoGenerating: (stage: Int) -> Unit,
//    onBackClick: () -> Unit = {},
//    viewModel: LevelsViewModel = hiltViewModel()
//) {
//    val state by viewModel.state.collectAsState()
//
//    // 상태 변화에 따른 단발성 네비게이션
//    LaunchedEffect(state) {
//        when (val s = state) {
//            is LevelsUiState.Started -> onGoGenerating(s.stage) // start 성공 → Generating 화면으로
//            else -> Unit
//        }
//    }
//
//    // 로딩 여부만 판단해서 버튼 상태에 반영
//    val isStarting = state is LevelsUiState.Starting
//
//    LevelTestIntroScreen(
//        onBackClick = onBackClick,
//        onLevelChosen = { level ->
//            if (!isStarting) viewModel.start(level)   // 단계 선택 → 서버 /levels/start 호출
//        },
//        isSubmitting = isStarting
//    )
//}
//
//
//@Composable
//fun LevelGeneratingRoute(
//    onReady: (stage: Int, passage: String, questions: List<LevelTestQuestion>) -> Unit,
//    onCancel: (() -> Unit)? = null,
//    viewModel: LevelsViewModel = hiltViewModel()
//) {
//    val state by viewModel.state.collectAsState()
//
//    // 최초 진입 시 한 번만 generate()
//    LaunchedEffect(Unit) { viewModel.generate() }
//
//    when (val s = state) {
//        is LevelsUiState.Generating -> {
//            LevelGeneratingScreen(
//                progressPercent = s.progress,
//                onCancel = onCancel
//            )
//        }
//        is LevelsUiState.Ready -> {
//            // passage/문항 3개 도착 → 다음 화면으로 전달
//            onReady(s.stage, s.passage, s.questions)
//        }
//        is LevelsUiState.Error -> {
//            // 간단한 에러 처리 (원하시면 디자인 맞춰 다이얼로그 등으로 교체)
//            // 임시로 제로-UI: 로딩 대신 에러 메시지 출력 가능
//            LevelGeneratingScreen(progressPercent = 0)
//            // TODO: 에러 UI/리트라이 버튼 등
//        }
//        else -> {
//            // Idle/Starting/Submitting/Result는 이 화면에선 보통 나오지 않음
//            LevelGeneratingScreen(progressPercent = 0)
//        }
//    }
//}
//
//@Composable
//fun LevelReadingQuizRoute(
//    // Generating에서 받던 데이터가 이미 ViewModel 내부에도 복제 저장돼 있지만,
//    // 화면 간 독립성/안전성 위해 한 번 더 주입할 수 있게 두었음(선택).
//    stage: Int? = null,
//    passage: String? = null,
//    questions: List<LevelTestQuestion>? = null,
//    onBackClick: () -> Unit = {},
//    onFinish: () -> Unit = {},     // "다음으로" 탭 시 외부로 알려주기
//    viewModel: LevelsViewModel = hiltViewModel()
//) {
//    val state by viewModel.state.collectAsState()
//
//    // 선택/제출을 LevelReadingQuizScreen 콜백들과 연결
//    // 네비는 아직 안 하므로, 이 라우트 안에서 Stage 전환만 신경 쓰면 됨.
//    LevelReadingQuizScreen(
//        onBackClick = onBackClick,
//        onShowResult = { answers ->
//            // answers: List<Int?> (0..3)
//            // 선택되지 않은 게 없을 때만 제출
//            if (answers.all { it != null }) {
//                viewModel.submit()
//            }
//        },
//        onNext = onFinish // 결과 화면의 "다음으로"
//    )
//
//    // 뷰모델 상태 변화를 이 화면에서 관찰해 UI와 동기화하고 싶다면,
//    // 필요에 맞게 UI 상태를 업데이트하거나, 스낵바/다이얼로그를 띄울 수 있어요.
//    when (state) {
//        is LevelsUiState.Error -> {
//            // TODO: 에러 UI (스낵바/다이얼로그/재시도 등)
//        }
//        else -> Unit
//    }
//}
//
//@Composable
//fun LevelGeneratingScreen(progressPercent: Int, onCancel: (() -> Unit)?) {
//
//}
//
//@HiltViewModel
//class LevelsViewModel @Inject constructor(
//    private val repository: LevelTestRepository
//) : ViewModel() {
//
//    private val _state = MutableStateFlow<LevelsUiState>(LevelsUiState.Idle)
//    val state: StateFlow<LevelsUiState> = _state
//
//    // 내부 보관 (UI 필요)
//    private var stage: Int = 0
//    private var passage: String = ""
//    private var questions: List<LevelTestQuestion> = emptyList()
//    private var answers: MutableList<Int?> = mutableListOf(null, null, null)
//
//    private var progressJob: Job? = null
//
//    /** Intro 에서 단계 선택 → 서버에 start 통지 */
//    fun start(level: VocabLevel) {
//        stage = level.toStage()
//        _state.value = LevelsUiState.Starting
//        viewModelScope.launch {
//            repository.levelsStart(stage)
//                .onSuccess {
//                    _state.value = LevelsUiState.Started(stage)
//                }
//                .onFailure {
//                    _state.value = LevelsUiState.Error(it.message ?: "시작 실패")
//                }
//        }
//    }
//
//    /** Generating 화면 진입 시 호출 → passage+질문 3개 생성 */
//    fun generate() {
//        _state.value = LevelsUiState.Generating(progress = 0)
//        // 진행률 UI 부드럽게 (API 완료 전 85%까지만 증가)
//        progressJob?.cancel()
//        progressJob = viewModelScope.launch {
//            var p = 0
//            while (p < 85 && state.value is LevelsUiState.Generating) {
//                delay(40)
//                p += 2
//                _state.value = LevelsUiState.Generating(progress = p)
//            }
//        }
//
//        viewModelScope.launch {
//            repository.levelsGenerate(stage)
//                .onSuccess { res: LevelsGenerateResult ->
//                    progressJob?.cancel()
//                    passage = res.passage
//                    questions = res.questions
//                    answers = MutableList(questions.size) { null }
//                    _state.value = LevelsUiState.Ready(
//                        stage = stage,
//                        passage = passage,
//                        questions = questions
//                    )
//                }
//                .onFailure {
//                    progressJob?.cancel()
//                    _state.value = LevelsUiState.Error(it.message ?: "문제 생성 실패")
//                }
//        }
//    }
//
//    /** 보기 선택 (0..3) */
//    fun selectAnswer(questionIndex: Int, choiceIndex: Int) {
//        if (questionIndex !in questions.indices) return
//        if (choiceIndex !in 0..3) return
//        answers[questionIndex] = choiceIndex
//        _state.value = LevelsUiState.Answering(
//            stage = stage,
//            passage = passage,
//            questions = questions,
//            answers = answers.toList()
//        )
//    }
//
//    /** 제출 */
//    fun submit() {
//        val filled = answers.all { it != null }
//        if (!filled) {
//            _state.value = LevelsUiState.Error("모든 문항에 답을 선택해주세요.")
//            return
//        }
//        _state.value = LevelsUiState.Submitting
//        viewModelScope.launch {
//            repository.levelsSubmit(
//                stage = stage,
//                questions = questions,
//                answers = answers.map { it ?: 0 }
//            ).onSuccess { res ->
//                _state.value = LevelsUiState.Result(
//                    correctCount = res.correctCount,
//                    resultLevel = res.resultLevel
//                )
//            }.onFailure {
//                _state.value = LevelsUiState.Error(it.message ?: "제출 실패")
//            }
//        }
//    }
//
//    /** 다시하기 (상태 초기화) */
//    fun reset() {
//        stage = 0
//        passage = ""
//        questions = emptyList()
//        answers = mutableListOf(null, null, null)
//        _state.value = LevelsUiState.Idle
//    }
//}
//
///** 화면 바인딩용 상태 */
//sealed interface LevelsUiState {
//    data object Idle : LevelsUiState
//    data object Starting : LevelsUiState
//    data class Started(val stage: Int) : LevelsUiState
//    data class Generating(val progress: Int) : LevelsUiState
//    data class Ready(
//        val stage: Int,
//        val passage: String,
//        val questions: List<LevelTestQuestion>
//    ) : LevelsUiState
//    data class Answering(
//        val stage: Int,
//        val passage: String,
//        val questions: List<LevelTestQuestion>,
//        val answers: List<Int?> // index 기반(0..3)
//    ) : LevelsUiState
//    data object Submitting : LevelsUiState
//    data class Result(val correctCount: Int, val resultLevel: String) : LevelsUiState
//    data class Error(val message: String) : LevelsUiState
//}
