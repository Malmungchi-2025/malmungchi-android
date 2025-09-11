package com.malmungchi.feature.quiz

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/* ===== 도메인 ===== */
enum class QuizCategory(val displayName: String) {
    JOB_PREP("취업 준비"), BASIC("기초"), PRACTICE("활용"), DEEP("심화"), ADVANCED("고급")
}
enum class QuizType { MCQ, OX, SHORT }

data class QuizSet(val id: String, val category: QuizCategory, val steps: List<QuizStep>) {
    val total get() = steps.size
}

sealed interface QuizStep { val id: String; val type: QuizType }
data class QuizOption(val id: Int, val label: String)

data class McqStep(
    override val id: String,
    val text: String,
    val options: List<QuizOption>,
    val correctOptionId: Int?
) : QuizStep { override val type = QuizType.MCQ }

data class OxStep(
    override val id: String,
    val statement: String,
    val answerIsO: Boolean?
) : QuizStep { override val type = QuizType.OX }

data class ShortStep(
    override val id: String,
    val guide: String,
    val sentence: String,
    val underlineText: String?,
    val answerText: String?
) : QuizStep { override val type = QuizType.SHORT }

/* ===== 제출 페이로드 ===== */
data class Submission(
    val questionId: String,
    val type: QuizType,
    val selectedOptionId: Int? = null,
    val selectedIsO: Boolean? = null,
    val text: String? = null
)

/* ===== UI/이벤트 ===== */
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
    data object Submit : QuizEvent         // 채점/진행률만 증가(이동 X)
    data object Next : QuizEvent           // 다음으로 이동(마지막이면 종료)
    data object Back : QuizEvent
    data object ShowExplanation : QuizEvent
    data object HideExplanation : QuizEvent
}

class QuizFlowViewModel : ViewModel() {
    private val _ui = MutableStateFlow(QuizUiState(loading = true))
    val ui: StateFlow<QuizUiState> = _ui

    private var quizSet: QuizSet? = null
    private val selections = linkedMapOf<String, Submission>()
    private val submitted = linkedMapOf<String, Boolean>()

    fun onEvent(e: QuizEvent) {
        when (e) {
            is QuizEvent.LoadWithSet -> loadWithSet(e.quizSet)
            is QuizEvent.SelectMcq   -> updateSelection(mcq = e.optionId)
            is QuizEvent.SelectOx    -> updateSelection(isO = e.isO)
            is QuizEvent.FillShort   -> updateSelection(text = e.text)
            QuizEvent.Submit         -> submitOnly()
            QuizEvent.Next           -> goNextFromSubmitted()
            QuizEvent.Back           -> goBack()
            QuizEvent.ShowExplanation ->
                _ui.update { it.copy(showExplanation = true, explanation = currentExplanation()) }
            QuizEvent.HideExplanation ->
                _ui.update { it.copy(showExplanation = false, explanation = null) }
        }
    }

    /* --- 동기 로딩(프리뷰 안정화) --- */
    private fun loadWithSet(set: QuizSet) {
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
    }

    /* --- 입력 저장 --- */
    private fun updateSelection(mcq: Int? = null, isO: Boolean? = null, text: String? = null) {
        val set = quizSet ?: return
        val q = set.steps.getOrNull(_ui.value.index) ?: return
        val base = selections[q.id] ?: Submission(q.id, q.type)
        selections[q.id] = base.copy(
            selectedOptionId = mcq ?: base.selectedOptionId,
            selectedIsO = isO ?: base.selectedIsO,
            text = text ?: base.text
        )
    }

    /* --- 제출만(이동 X) --- */
    private fun submitOnly() {
        val set = quizSet ?: return
        val idx = _ui.value.index
        val q = set.steps.getOrNull(idx) ?: return
        val sel = selections[q.id] ?: return
        if (submitted[q.id] == true) return

        submitted[q.id] = true
        val correctInc = if (canGradeLocally(q) && isCorrect(q, sel)) 1 else 0
        val completedNext = _ui.value.completed + 1
        val progressNext = (completedNext.toFloat() / set.total).coerceIn(0f, 1f)

        _ui.update {
            it.copy(
                completed = completedNext,
                progress = progressNext,
                displayStep = completedNext.coerceAtMost(set.total),
                correctCount = it.correctCount + correctInc
            )
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
    }

    /* --- 해설(더미) --- */
    private fun currentExplanation(): String? {
        val set = quizSet ?: return null
        val q = set.steps.getOrNull(_ui.value.index) ?: return null
        return when (q) {
            is McqStep   -> q.correctOptionId?.let { "정답은 ${it}번입니다.\n\n(보기 해설은 서버에서 전달)" }
            is OxStep    -> q.answerIsO?.let { if (it) "정답은 O" else "정답은 X" }
            is ShortStep -> q.answerText?.let { "정답은 ‘$it’ 입니다." }
        }
    }

    /* --- 채점 유틸 --- */
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

    /* --- 바인딩용 Getter --- */
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
