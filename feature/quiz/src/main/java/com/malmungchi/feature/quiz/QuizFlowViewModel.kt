package com.malmungchi.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.quiz.*
import com.malmungchi.core.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/* ===== UI 상태/이벤트 ===== */
data class QuizUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val headerTitle: String = "",
    val total: Int = 7,
    val index: Int = 0,
    val completed: Int = 0,
    val progress: Float = 0f,
    val displayStep: Int = 1,
    val current: QuizStep? = null,
    val finished: Boolean = false,
    val correctCount: Int = 0,
    val showExplanation: Boolean = false,
    val explanation: String? = null
)

sealed interface QuizEvent {
    data class LoadWithSet(val quizSet: QuizSet) : QuizEvent
    data class SelectMcq(val optionId: Int) : QuizEvent
    data class SelectOx(val isO: Boolean) : QuizEvent
    data class FillShort(val text: String) : QuizEvent
    data object Submit : QuizEvent           // 서버 제출 + 진행률 갱신
    data object Next : QuizEvent             // 다음 문항(마지막이면 종료)
    data object Back : QuizEvent
    data object ShowExplanation : QuizEvent
    data object HideExplanation : QuizEvent
}

/* ===== 임시 레포(Fake) ===== */
class FakeQuizRepo(
    private val set: QuizSet = QuizSet(
        id = "123",
        category = QuizCategory.BASIC,
        steps = listOf(
            McqStep("1", "MCQ Q1",
                listOf(QuizOption(1, "A"), QuizOption(2, "B"), QuizOption(3, "C"), QuizOption(4, "D")),
                correctOptionId = 2, explanation = "정답은 2번 B 입니다."
            ),
            OxStep("2", "OX Q2", answerIsO = true, explanation = "사실입니다."),
            ShortStep("3", "가이드", "오늘 안에 보내줄게.", "오늘", "금일", "격식체로 바꾸면 ‘금일’.")
        )
    )
) : QuizRepository {

    override suspend fun createBatch(categoryKor: String, len: Int?) = set

    override suspend fun getBatch(batchId: Long) = set

    override suspend fun submit(batchId: Long, questionIndex: Int, submission: Submission) = true

    // ⬇️ 추가된 부분: rewardAttempt 메서드 구현
    override suspend fun rewardAttempt(attemptId: Long): RewardResult {
        // 여기서는 간단한 테스트용 로직
        val rewardPoint = if (attemptId == 123L) 20 else 15 // 예시: attemptId가 123일 경우 20포인트 지급
        val totalPoint = 100 + rewardPoint // 예시: 기존 포인트 + 지급 포인트

        return RewardResult(
            rewardPoint = rewardPoint,
            basePoint = 15,
            bonusAllCorrect = if (rewardPoint == 20) 5 else 0,
            allCorrect = rewardPoint == 20,
            totalPoint = totalPoint
        )
    }
}

/* ===== ViewModel ===== */
@HiltViewModel
class QuizFlowViewModel @Inject constructor(
    private val repo: QuizRepository
) : ViewModel() {

    /* ---------- 공개 상태 ---------- */
    private val _ui = MutableStateFlow(QuizUiState(loading = true))
    val ui: StateFlow<QuizUiState> = _ui

    // 현재 문항에 대한 즉시 반응형 선택 상태 (MCQ/OX/SHORT)
    private val _currentMcq = MutableStateFlow<Int?>(null)
    val currentMcq: StateFlow<Int?> = _currentMcq

    private val _currentOx = MutableStateFlow<Boolean?>(null)
    val currentOx: StateFlow<Boolean?> = _currentOx

    private val _currentShort = MutableStateFlow("")
    val currentShort: StateFlow<String> = _currentShort

    private var rootSet: QuizSet? = null     // 최초 7문항(원 세트) 보관
    private var isRetryMode: Boolean = false // 재도전 여부

    // 추가: 재도전 모드 여부 외부 노출
    fun isRetryMode(): Boolean = isRetryMode

    // 시도별 제출/정답 기록 (문항 id -> 값)
    private val firstSelections = linkedMapOf<String, Submission>()
    private val firstCorrectness = linkedMapOf<String, Boolean?>()

    private val secondSelections = linkedMapOf<String, Submission>()
    private val secondCorrectness = linkedMapOf<String, Boolean?>()

    /* ---------- 내부 상태 ---------- */
    private var quizSet: QuizSet? = null
    private val selections = linkedMapOf<String, Submission>()
    private val submitted = linkedMapOf<String, Boolean>()

    /* ---------- 생성자(미리보기용) ---------- */
    constructor() : this(FakeQuizRepo())

    fun internalCurrentSetOrNull(): QuizSet? = quizSet

    /* ---------- API 엔트리 ---------- */

    /** 카테고리 선택 → 새 세트 생성 후 화면 상태 로딩 */
    fun startQuiz(categoryKorForApi: String, len: Int? = 80) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.createBatch(categoryKorForApi, len) }
                .onSuccess { set -> loadWithSet(set) }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message ?: "세트 생성 실패") }
                }
        }
    }

    /** 기존 배치 이어하기 */
    fun loadExistingBatch(batchId: Long) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.getBatch(batchId) }
                .onSuccess { set -> loadWithSet(set) }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message ?: "세트 조회 실패") }
                }
        }
    }

    /** 현재 문항 서버 제출/채점 */
    private fun submitCurrent() {
        val set = quizSet ?: return
        val idx = _ui.value.index
        val q = set.steps.getOrNull(idx) ?: return
        val sel = selections[q.id] ?: return
        if (submitted[q.id] == true) return

        val originalIndex1Based = rootSet?.steps
            ?.indexOfFirst { it.id == q.id }
            ?.let { it + 1 }
            ?: (idx + 1) // fallback

        viewModelScope.launch {
            runCatching {
                repo.submit(
                    batchId = set.id.toLongOrNull() ?: error("잘못된 batch id"),
                    questionIndex = originalIndex1Based,
                    // questionIndex = q.id.toInt(),   // 서버가 1-based 인덱스라면 그대로 사용
                    submission = sel
                )
            }.onSuccess { isCorrect ->
                submitted[q.id] = true
                val correctInc = if (isCorrect == true) 1 else 0
                val completedNext = _ui.value.completed + 1
                val progressNext = (completedNext.toFloat() / set.total).coerceIn(0f, 1f)

                val exp = when (q) {
                    is McqStep   -> q.explanation
                    is OxStep    -> q.explanation
                    is ShortStep -> q.explanation
                }

                _ui.update {
                    it.copy(
                        completed = completedNext,
                        progress = progressNext,
                        displayStep = completedNext.coerceAtMost(set.total),
                        correctCount = it.correctCount + correctInc,
                        showExplanation = false,
                        explanation = exp
                    )
                }

                // 시도별 기록 (결과 화면/재도전용)
                if (!isRetryMode) {
                    firstSelections[q.id] = sel
                    firstCorrectness[q.id] = isCorrect
                } else {
                    secondSelections[q.id] = sel
                    secondCorrectness[q.id] = isCorrect
                }
//                // ✅ 재도전 모드에서는 제출 직후 자동으로 다음 문항으로 이동
//                if (isRetryMode) {
//                    goNextFromSubmitted()
//                }
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "제출 실패") }
            }
        }
    }

    /** 현재 문항의 서버 채점 결과 (정답/오답 플래시 등에서 사용 가능) */
    fun currentServerCorrectness(): Boolean? {
        val set = quizSet ?: return null
        val q = set.steps.getOrNull(_ui.value.index) ?: return null
        return if (!isRetryMode) firstCorrectness[q.id] else secondCorrectness[q.id]
    }

    /** 1차(원 세트)에서 틀린 문제만 추려 재도전 세트를 시작 */
    fun startRetryFromWrong(): Boolean {
        val base = rootSet ?: return false
        val wrongSteps = base.steps.filter { step ->
            val wasSubmitted = firstSelections.containsKey(step.id)
            val wasCorrect = firstCorrectness[step.id] == true
            wasSubmitted && !wasCorrect
        }
        if (wrongSteps.isEmpty()) {
            _ui.update { it.copy(finished = true) }
            return false
        }
        isRetryMode = true
        val retrySet = QuizSet(id = base.id, category = base.category, steps = wrongSteps)
        loadWithSet(retrySet)
        return true
    }

    /** 전체 결과 리스트 */
    fun buildRetryResultItems(): List<RetryResultItem> {
        val base = rootSet ?: return emptyList()
        val total = base.total

        fun groupScore(stepId: String): Int {
            val first = firstCorrectness[stepId]
            val second = secondCorrectness[stepId]
            return when {
                first == true -> 1
                first == false && second == true -> 2
                first == false && (second == false || second == null) -> 3
                else -> 3
            }
        }

        return base.steps.mapIndexed { idx, step ->
            val order = idx + 1

            val correctAnswer: String = when (step) {
                is McqStep   -> mcqAnswerLabel(step, step.correctOptionId) ?: ""
                is OxStep    -> oxLabel(step.answerIsO) ?: ""
                is ShortStep -> step.answerText.orEmpty()
            }

            val finalUser: String? = when (step) {
                is McqStep -> mcqAnswerLabel(
                    step,
                    (secondSelections[step.id]?.selectedOptionId)
                        ?: (firstSelections[step.id]?.selectedOptionId)
                )
                is OxStep  -> oxLabel(
                    (secondSelections[step.id]?.selectedIsO)
                        ?: (firstSelections[step.id]?.selectedIsO)
                )
                is ShortStep -> (secondSelections[step.id]?.text)
                    ?: (firstSelections[step.id]?.text)
            }

            val options: List<String> = when (step) {
                is McqStep   -> step.options.map { it.label }
                is OxStep    -> listOf("O", "X")
                is ShortStep -> emptyList()
            }

            val type = when (step) {
                is McqStep -> RetryResultType.MCQ
                is OxStep  -> RetryResultType.OX
                is ShortStep -> RetryResultType.SHORT
            }

            RetryResultItem(
                id = step.id,
                type = type,
                order = order,
                total = total,
                question = when (step) {
                    is McqStep   -> step.text
                    is OxStep    -> step.statement
                    is ShortStep -> step.sentence
                },
                options = options,
                userAnswer = finalUser,
                correctAnswer = correctAnswer,
                explanation = when (step) {
                    is McqStep   -> step.explanation.orEmpty()
                    is OxStep    -> step.explanation.orEmpty()
                    is ShortStep -> step.explanation.orEmpty()
                }
            )
        }.sortedWith(
            compareBy<RetryResultItem>({ when (it.type) {
                RetryResultType.MCQ -> 1; RetryResultType.OX -> 2; RetryResultType.SHORT -> 3 } })
                .thenBy { groupScore(it.id) }
                .thenBy { it.order }
        )
    }

    /* ---------- 이벤트 ---------- */
    fun onEvent(e: QuizEvent) {
        when (e) {
            is QuizEvent.LoadWithSet   -> loadWithSet(e.quizSet)
            is QuizEvent.SelectMcq     -> updateSelection(mcq = e.optionId)
            is QuizEvent.SelectOx      -> updateSelection(isO = e.isO)
            is QuizEvent.FillShort     -> updateSelection(text = e.text)
            QuizEvent.Submit           -> submitCurrent()           // ★ 서버 제출 사용
            QuizEvent.Next             -> goNextFromSubmitted()
            QuizEvent.Back             -> goBack()
            QuizEvent.ShowExplanation -> {
                val set = quizSet ?: return
                val cur = set.steps.getOrNull(_ui.value.index) ?: return
                val (answerText, exp) = buildAnswerAndExplanation(cur)
                val dialogText = "정답은 [$answerText] 입니다!\n$exp"  // ← 제목 + 본문
                _ui.update { it.copy(showExplanation = true, explanation = dialogText) }
            }
//            QuizEvent.ShowExplanation  ->
//                _ui.update { it.copy(showExplanation = true, explanation = currentExplanation()) }
            QuizEvent.HideExplanation  ->
                _ui.update { it.copy(showExplanation = false, explanation = null) }
        }
    }

    //포인트 지급 api 연동
    fun rewardCurrentAttempt(
        onSuccess: (RewardResult) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val attemptId = quizSet?.id?.toLongOrNull()
        if (attemptId == null) {
            onError("잘못된 시도 ID")
            return
        }
        viewModelScope.launch {
            runCatching { repo.rewardAttempt(attemptId) }
                .onSuccess { onSuccess(it) }
                .onFailure { e -> onError(e.message ?: "포인트 지급 실패") }
        }
    }

    /* ---------- 유틸 ---------- */
    private fun mcqAnswerLabel(step: McqStep, optionId: Int?): String? {
        val id = optionId ?: return null
        return step.options.firstOrNull { it.id == id }?.label
    }

    private fun oxLabel(v: Boolean?): String? = when (v) {
        true -> "O"
        false -> "X"
        null -> null
    }

    //정답 이벤트(해설)
    private fun buildAnswerAndExplanation(step: QuizStep): Pair<String, String> = when (step) {
        is McqStep -> {
            val ans = step.options.firstOrNull { it.id == step.correctOptionId }?.label.orEmpty()
            ans to (step.explanation.orEmpty())
        }
        is OxStep -> {
            val ans = if (step.answerIsO == true) "O" else "X"
            ans to (step.explanation.orEmpty())
        }
        is ShortStep -> {
            val ans = step.answerText.orEmpty()
            ans to (step.explanation.orEmpty())
        }
    }

    /* --- 동기 로딩 & 초기화 --- */
    private fun loadWithSet(set: QuizSet) {
        if (rootSet == null) rootSet = set

        quizSet = set
        selections.clear()
        submitted.clear()
        _ui.update {
            it.copy(
                loading = false, error = null,
                headerTitle = categoryTitle(set.category),
                total = set.total, index = 0,
                completed = 0, progress = 0f,
                displayStep = 1,
                current = set.steps.firstOrNull(),
                finished = false, correctCount = 0,
                showExplanation = false, explanation = null
            )
        }
        restoreSelectionFor(0) // ★ 첫 문항 선택값 복원(없으면 초기화)
    }

    /* --- 입력 저장(즉시 반응형 StateFlow도 함께 갱신) --- */
    private fun updateSelection(mcq: Int? = null, isO: Boolean? = null, text: String? = null) {
        val set = quizSet ?: return
        val q = set.steps.getOrNull(_ui.value.index) ?: return
        val base = selections[q.id] ?: Submission(q.id, q.type)
        val new = base.copy(
            selectedOptionId = mcq ?: base.selectedOptionId,
            selectedIsO = isO ?: base.selectedIsO,
            text = text ?: base.text
        )
        selections[q.id] = new

        when (q) {
            is McqStep   -> if (mcq != null) _currentMcq.value = mcq
            is OxStep    -> if (isO != null) _currentOx.value = isO
            is ShortStep -> if (text != null) _currentShort.value = text
        }
    }

    /* --- 다음으로 이동(또는 종료) --- */
    private fun goNextFromSubmitted() {
        val set = quizSet ?: return
        val idx = _ui.value.index
        val last = idx >= set.steps.lastIndex
        if (last) {
            _ui.update {
                it.copy(
                    finished = true,
                    current = null,
                    displayStep = set.total,
                    showExplanation = false,
                    explanation = null
                )
            }
            clearTransientSelections()
            return
        }
        val nextIdx = idx + 1
        val nextQ = set.steps[nextIdx]
        _ui.update {
            it.copy(
                index = nextIdx,
                current = nextQ,
                displayStep = (it.completed + 1).coerceAtMost(set.total),
                showExplanation = false,
                explanation = null
            )
        }
        restoreSelectionFor(nextIdx) // ★ 이동 후 선택값 복원
    }

    /* --- 뒤로 --- */
    private fun goBack() {
        val set = quizSet ?: return
        val idx = _ui.value.index
        if (idx == 0 || _ui.value.finished) return
        val prevIdx = idx - 1
        _ui.update { prev ->
            prev.copy(
                index = prevIdx,
                current = set.steps[prevIdx],
                displayStep = (prev.completed + 1).coerceAtMost(set.total),
                showExplanation = false,
                explanation = null
            )
        }
        restoreSelectionFor(prevIdx) // ★ 이동 후 선택값 복원
    }

    /* --- 해설 --- */
    private fun currentExplanation(): String? {
        val set = quizSet ?: return null
        val q = set.steps.getOrNull(_ui.value.index) ?: return null
        return when (q) {
            is McqStep   -> q.explanation
            is OxStep    -> q.explanation
            is ShortStep -> q.explanation
        }
    }

    /* --- 채점(로컬 대비용; 현재는 서버 제출을 사용) --- */
    private fun canGradeLocally(q: QuizStep) = when (q) {
        is McqStep -> q.correctOptionId != null
        is OxStep -> q.answerIsO != null
        is ShortStep -> q.answerText != null
    }

    private fun norm(s: String?) = s?.trim()?.lowercase()
        ?.replace("\\s+".toRegex(), " ")
        ?.let { java.text.Normalizer.normalize(it, java.text.Normalizer.Form.NFC) } ?: ""

    private fun isCorrect(q: QuizStep, s: Submission) = when (q) {
        is ShortStep -> q.answerText != null && norm(q.answerText) == norm(s.text)
        is McqStep   -> q.correctOptionId != null && q.correctOptionId == s.selectedOptionId
        is OxStep    -> q.answerIsO != null && q.answerIsO == s.selectedIsO
    }

    private fun categoryTitle(cat: QuizCategory) = when (cat) {
        QuizCategory.JOB_PREP -> "취업 준비"
        QuizCategory.BASIC -> "기초"
        QuizCategory.PRACTICE -> "활용"
        QuizCategory.DEEP -> "심화"
        QuizCategory.ADVANCED -> "고급"
    }

    /* --- 선택 복원/초기화 헬퍼 --- */
    private fun restoreSelectionFor(targetIndex: Int) {
        val set = quizSet
        val q = set?.steps?.getOrNull(targetIndex)
        when (q) {
            is McqStep -> {
                val v = selections[q.id]?.selectedOptionId
                _currentMcq.value = v
                _currentOx.value = null
                _currentShort.value = ""
            }
            is OxStep -> {
                val v = selections[q.id]?.selectedIsO
                _currentMcq.value = null
                _currentOx.value = v
                _currentShort.value = ""
            }
            is ShortStep -> {
                val v = selections[q.id]?.text ?: ""
                _currentMcq.value = null
                _currentOx.value = null
                _currentShort.value = v
            }
            else -> clearTransientSelections()
        }
    }

    private fun clearTransientSelections() {
        _currentMcq.value = null
        _currentOx.value = null
        _currentShort.value = ""
    }

    /* --- (레거시) 바인딩 Getter: 남겨둬도 무방 --- */
    fun currentSubmitted(): Boolean {
        val q = quizSet?.steps?.getOrNull(_ui.value.index) ?: return false
        return submitted[q.id] == true
    }
    fun currentSelectedOptionId(): Int? {
        val q = quizSet?.steps?.getOrNull(_ui.value.index) ?: return null
        return selections[q.id]?.selectedOptionId
    }
    fun currentSelectedIsO(): Boolean? {
        val q = quizSet?.steps?.getOrNull(_ui.value.index) ?: return null
        return selections[q.id]?.selectedIsO
    }
    fun currentShortText(): String {
        val q = quizSet?.steps?.getOrNull(_ui.value.index) ?: return ""
        return selections[q.id]?.text ?: ""
    }
    fun isCurrentCorrect(): Boolean {
        val set = quizSet ?: return false
        val q = set.steps.getOrNull(_ui.value.index) ?: return false
        val sel = selections[q.id] ?: return false
        return canGradeLocally(q) && isCorrect(q, sel)
    }
    fun isLastStep(): Boolean {
        val set = quizSet ?: return true
        return _ui.value.index >= set.steps.lastIndex
    }
}
