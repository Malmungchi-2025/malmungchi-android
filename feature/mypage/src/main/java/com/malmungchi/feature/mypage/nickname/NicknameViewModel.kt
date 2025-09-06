package com.malmungchi.feature.mypage.nickname

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class McqQuestion(
    val id: Int,
    val numberLabel: String,
    val text: String,
    val options: List<McqOption>,
    val answerOptionId: Int
)
// ===== OX 모델 =====
data class OxQuestion(
    val id: Int,
    val numberLabel: String,   // "Q10" 등
    val statement: String,     // 본문
    val answerIsO: Boolean     // 정답: O=true, X=false
)
// ===== 모델 =====
data class ReadingMcqQuestion(
    val id: Int,
    val numberLabel: String,    // "Q10"
    val statement: String,      // 본문 (카드에 표시)
    val questionText: String,   // 문제 문장 (가운데 굵게)
    val options: List<McqOption>,
    val answerOptionId: Int
)

data class McqOption(val id: Int, val label: String)

enum class VocabularyTier { 상, 중, 하 }

// ===== 별명 매핑 =====
private fun toNickname(vocabTier: VocabularyTier, readingTier: VocabularyTier): String = when {
    vocabTier == VocabularyTier.상 && readingTier == VocabularyTier.상 -> "언어연금술사"
    vocabTier == VocabularyTier.하 && readingTier == VocabularyTier.상 -> "눈치번역가"
    vocabTier == VocabularyTier.하 && readingTier == VocabularyTier.중 -> "감각해석가"
    vocabTier == VocabularyTier.중 && readingTier == VocabularyTier.상 -> "맥락추리자"
    vocabTier == VocabularyTier.중 && readingTier == VocabularyTier.중 -> "언어균형술사"
    vocabTier == VocabularyTier.중 && readingTier == VocabularyTier.하 -> "낱말며행자"
    vocabTier == VocabularyTier.상 && readingTier == VocabularyTier.하 -> "단어수집가"
    vocabTier == VocabularyTier.상 && readingTier == VocabularyTier.중 -> "의미해석가"
    else -> "언어모험가" // vocab 하 + reading 하
}


private fun toTier(correctCount: Int): VocabularyTier = when (correctCount) {
    in 7..9 -> VocabularyTier.상
    in 4..6 -> VocabularyTier.중
    else -> VocabularyTier.하
}

// ===== 진행 단계 =====
// ✅ private 빼기 (또는 명시적으로 public)
enum class Step { Vocab_1_9, Ox_10_11, Reading_12_18, Finished }


// ===== 서버 전송 인터페이스 (예시) =====
interface NicknameApi {
    // 실제 프로젝트의 Retrofit 서비스에 맞춰 바꿔줘
    suspend fun postNicknameResult(
        nickname: String,
        vocabTier: VocabularyTier,
        readingTier: VocabularyTier,
        vocabCorrect: Int,
        readingCorrect: Int
    )
}

// 데모용 더미 구현
class FakeNicknameApi : NicknameApi {
    override suspend fun postNicknameResult(
        nickname: String,
        vocabTier: VocabularyTier,
        readingTier: VocabularyTier,
        vocabCorrect: Int,
        readingCorrect: Int
    ) {
        // 서버 전송 로그/무시
    }
}

// ===== ViewModel =====
data class NicknameUiState(
    val step: Step = Step.Vocab_1_9,
    val answeredGlobalCount: Int = 0,   // 전체 18문항 진행바용 베이스
    val vocabCorrect: Int = 0,          // 1~9 맞춘 개수
    val oxCorrect: Int = 0,             // 10~11 맞춘 개수(문해력에 포함)
    val readingMcqCorrect: Int = 0,     // 12~18 맞춘 개수(문해력에 포함)
    val finishedNickname: String? = null,
    val finishedVocabTier: VocabularyTier? = null,
    val finishedReadingTier: VocabularyTier? = null
)

@HiltViewModel
class NicknameViewModel @Inject constructor(
    // 실제에선 DI로 주입: private val api: NicknameApi
) : ViewModel() {

    private val api: NicknameApi = FakeNicknameApi() // 데모용

    private val _state = MutableStateFlow(NicknameUiState())
    val state: StateFlow<NicknameUiState> = _state

    // ✅ 여기 추가: ViewModel 멤버 함수로 resetFlow 정의
    fun resetFlow() {
        _state.value = NicknameUiState()
    }

    // ===== 문제 세트 (로컬 상수) =====

    // 1~9 어휘력 (문제/정답은 요청자가 제공한 값 사용)
    val vocabQuestions: List<McqQuestion> = listOf(
        McqQuestion(1, "Q1", "“망라하다”의 의미로 알맞은 것은?",
            listOf(
                McqOption(1, "널리 받아들여 모두 포함하다"),
                McqOption(2, "여러 가지를 혼합하다"),
                McqOption(3, "결점을 보완하다"),
                McqOption(4, "순서대로 나열하다")
            ), answerOptionId = 1
        ),
        McqQuestion(2, "Q2", "다음 중 “불가피하다”와 의미가 가장 유사한 단어는?",
            listOf(
                McqOption(1, "필연적이다"),
                McqOption(2, "우연하다"),
                McqOption(3, "변덕스럽다"),
                McqOption(4, "가변적이다")
            ), answerOptionId = 1
        ),
        McqQuestion(3, "Q3", "“엄정하다”의 의미로 알맞은 것은?",
            listOf(
                McqOption(1, "날카롭고 공정하다"),
                McqOption(2, "단순하고 명확하다"),
                McqOption(3, "유연하고 융통성있다"),
                McqOption(4, "불확실하고 애매하다")
            ), answerOptionId = 1
        ),
        McqQuestion(4, "Q4", "‘규제 완화’의 의미를 올바르게 사용한 문장은?",
            listOf(
                McqOption(1, "정부는 기업 활동을 촉진하기 위해 규제 완화를 검토 중이다."),
                McqOption(2, "규제 완화는 모든 규정을 폐지하는 것을 의미한다."),
                McqOption(3, "규제 완화는 개인의 선택에 영향을 미치지 않는다."),
                McqOption(4, "규제 완화는 경제적 측면에서 중요하지 않다.")
            ), answerOptionId = 1
        ),
        McqQuestion(5, "Q5", "‘기탄없이’의 쓰임이 적절하지 않은 것은?",
            listOf(
                McqOption(1, "그는 기탄없이 비판했다."),
                McqOption(2, "그녀는 기탄없이 눈물을 흘렸다."),
                McqOption(3, "기탄없이 의견을 말해 주세요."),
                McqOption(4, "기탄없이 질문을 던졌다.")
            ), answerOptionId = 2
        ),
        McqQuestion(6, "Q6", "‘절차’의 의미를 올바르게 사용한 문장은?",
            listOf(
                McqOption(1, "중요한 절차를 무시하고 진행했다."),
                McqOption(2, "절차는 항상 불필요한 과정이다."),
                McqOption(3, "절차는 오랜 시간이 걸리지 않는다."),
                McqOption(4, "절차는 항상 컴퓨터를 사용해야 한다.")
            ), answerOptionId = 1
        ),
        McqQuestion(7, "Q7", "다음 중 ‘구사하다’의 올바른 쓰임은?",
            listOf(
                McqOption(1, "그는 경기에서 다양한 전략을 구사하며 상대를 압도했다."),
                McqOption(2, "그녀는 무기를 구사하는 능력이 뛰어나다."),
                McqOption(3, "힘든 상황에서도 긍정적인 태도를 구사했다."),
                McqOption(4, "운동선수는 규칙을 철저히 구사해야 한다.")
            ),
            answerOptionId = 2 // 요청자가 지정한 정답 유지
        ),
        McqQuestion(8, "Q8", "“타개하다”의 의미로 알맞은 것은?",
            listOf(
                McqOption(1, "문제를 해결할 방법을 모색하다"),
                McqOption(2, "매우 어렵거나 막힌 일을 잘 처리하여 해결의 길을 열다"),
                McqOption(3, "상대방을 비판하고 공격하다"),
                McqOption(4, "상황을 모른 척하고 피하다")
            ), answerOptionId = 2
        ),
        McqQuestion(9, "Q9", "“난삽하다”의 의미로 알맞은 것은?",
            listOf(
                McqOption(1, "글이나 말이 매끄럽지 못하면서 어렵고 까다롭다."),
                McqOption(2, "신중하고 차분하다"),
                McqOption(3, "쉽게 이해할 수 있다"),
                McqOption(4, "정리가 잘 되어 깔끔하다")
            ), answerOptionId = 1
        )
    )

    // 10~11 OX (문해력에 포함)
    val oxQuestions: List<OxQuestion> = listOf(
        OxQuestion(
            10, "Q10",
            "지진이 발생하면 건물 붕괴뿐만 아니라 화재와 가스 누출 같은 2차 피해도 발생할 수 있다. 따라서 지진 대비 훈련에서는 단순히 대피 방법뿐만 아니라 화재 예방 조치도 포함되어야 한다.",
            answerIsO = false // 질문은 '대피만'이므로 정답 X가 되도록, 본문 정답은 O
        ),
        OxQuestion(
            11, "Q11",
            "북극곰의 주요 서식지는 북극 지역이다. 최근 기후 변화로 인해 해빙이 줄어들면서 북극곰의 생존이 위협받고 있다. 이에 따라 과학자들은 북극곰 보호를 위한 다양한 방안을 모색하고 있다.",
            answerIsO = false // 본문 팩트 O, 주어진 질문 정답은 X
        )
    )

    // 12~18 독해 4지선다 (문해력에 포함)
    val readingQuestions: List<ReadingMcqQuestion> = listOf(
        ReadingMcqQuestion(
            12, "Q12",
            statement = "일부 연구에서는 독서가 공감 능력을 향상시킨다고 주장한다. 특히 소설을 읽으며 등장인물의 감정을 이해하는 과정이 감성 발달에 기여할 수 있다.",
            questionText = "위 글의 주장과 가장 관련이 있는 것은?",
            options = listOf(
                McqOption(1, "소설은 현실과 동떨어져 있으니 읽을 필요 없다."),
                McqOption(2, "독서는 감정 발달과 관련이 없다."),
                McqOption(3, "소설 읽기는 공감 능력 향상에 도움을 줄 수 있다."),
                McqOption(4, "감성 발달과 독서는 관계가 없다.")
            ),
            answerOptionId = 3
        ),
        ReadingMcqQuestion(
            13, "Q13",
            statement = "현대 사회에서는 디지털 기기의 사용이 증가하면서 사람들의 독서 습관이 변화하고 있다. 종이책보다 전자책을 선호하는 사람들이 늘어났으며, 짧은 글을 빠르게 소비하는 경향이 강해졌다. 이에 따라 깊이 있는 독서보다는 단편적인 정보 습득이 많아지는 것이 문제로 지적되고 있다.",
            questionText = "위 글의 핵심 내용을 가장 잘 요약한 것은?",
            options = listOf(
                McqOption(1, "디지털 기기 사용이 줄어 종이책이 다시 유행한다."),
                McqOption(2, "전자책보다 종이책이 정보 습득에 더 유리하다."),
                McqOption(3, "짧은 글 소비 증가로 깊이 있는 독서가 줄어드는 문제가 있다."),
                McqOption(4, "독서 습관은 시대와 무관하게 변하지 않는다.")
            ),
            answerOptionId = 3
        ),
        ReadingMcqQuestion(
            14, "Q14",
            statement = "최근 건강한 식습관이 중요해지고 있다. 패스트푸드보다 신선한 채소와 과일을 섭취하는 것이 건강에 이롭다는 연구 결과가 많아지고 있다. 이에 따라 건강식을 제공하는 레스토랑과 배달 서비스가 증가하는 추세이다.",
            questionText = "가장 적절한 제목은?",
            options = listOf(
                McqOption(1, "패스트푸드의 장점"),
                McqOption(2, "건강한 식습관의 중요성"),
                McqOption(3, "패스트푸드 섭취 증가 현상"),
                McqOption(4, "건강식보다 패스트푸드가 유리하다")
            ),
            answerOptionId = 2
        ),
        ReadingMcqQuestion(
            15, "Q15",
            statement = "최근 미세먼지가 심각해지고 있다. 마스크를 착용하고, 외출을 자제하는 것이 좋다.",
            questionText = "글쓴이의 의도는?",
            options = listOf(
                McqOption(1, "미세먼지는 건강에 영향을 줄 수 있으므로 조심해야 한다."),
                McqOption(2, "미세먼지가 많을수록 건강에 좋다."),
                McqOption(3, "마스크를 쓰면 미세먼지가 증가한다."),
                McqOption(4, "외출 자제보다 마스크를 쓰고 나가는 게 낫다.")
            ),
            answerOptionId = 1
        ),
        ReadingMcqQuestion(
            16, "Q16",
            statement = "다음 중 논리적 오류가 포함된 주장은?",
            questionText = "가장 적절한 선택은?",
            options = listOf(
                McqOption(1, "모든 철학자는 깊은 사고를 한다. 따라서 철학을 전공하면 누구나 깊은 사고를 하게 된다."),
                McqOption(2, "경제적 불평등이 심화되면 사회적 불안이 증가할 가능성이 높다."),
                McqOption(3, "기후 변화는 복합적인 원인이 작용하지만, 산업화 이후 가속화된 것은 분명하다."),
                McqOption(4, "개인의 선택은 환경에 영향을 받지만, 전적으로 환경에 의해 결정되지는 않는다.")
            ),
            answerOptionId = 1
        ),
        ReadingMcqQuestion(
            17, "Q17",
            statement = "뉴스에서 보도된 정보가 모두 사실일까?",
            questionText = "위 질문이 요구하는 사고 방식은?",
            options = listOf(
                McqOption(1, "단순한 정보 암기"),
                McqOption(2, "감정적인 판단"),
                McqOption(3, "비판적 사고"),
                McqOption(4, "직관적인 해석")
            ),
            answerOptionId = 3
        ),
        ReadingMcqQuestion(
            18, "Q18",
            statement = "SNS의 확산은 정보 접근성을 높이는 긍정적인 측면이 있지만, 동시에 허위 정보의 전파 속도를 가속화한다. 이에 따라 현대인은 정보의 신뢰성을 판단하는 능력을 더욱 요구받고 있다.",
            questionText = "위 글의 핵심 메시지는?",
            options = listOf(
                McqOption(1, "SNS는 정보 확산에 기여한다."),
                McqOption(2, "허위 정보 확산이 문제이며, 정보 판단 능력이 필요하다."),
                McqOption(3, "대중은 항상 진실을 구별할 수 있다."),
                McqOption(4, "SNS는 사회적 논의를 활성화하는 긍정적 도구다.")
            ),
            answerOptionId = 2
        )
    )

    // ===== 콜백들 =====

    fun onVocabFinished(correctCount: Int) {
        val vocabTier = toTier(correctCount)
        _state.value = _state.value.copy(
            step = Step.Ox_10_11,
            answeredGlobalCount = 9,         // 다음 섹션 시작점
            vocabCorrect = correctCount,
            finishedVocabTier = vocabTier
        )
    }

    // ✅ 여기 추가!
    fun onOxFinishedWithCount(oxCorrectCount: Int) {
        _state.value = _state.value.copy(
            oxCorrect = oxCorrectCount,
            step = Step.Reading_12_18,
            answeredGlobalCount = 11
        )
    }


    fun onOxSubmit(isCorrect: Boolean) {
        _state.value = _state.value.copy(
            oxCorrect = _state.value.oxCorrect + if (isCorrect) 1 else 0
        )
    }

    fun onOxFinished() {
        _state.value = _state.value.copy(
            step = Step.Reading_12_18,
            answeredGlobalCount = 11 // 9 + 2
        )
    }

    fun onReadingFinished(readingMcqCorrect: Int) {
        val totalReadingCorrect = _state.value.oxCorrect + readingMcqCorrect // OX(2) + 독해(7) = 9
        val readingTier = toTier(totalReadingCorrect)
        val vocabTier = _state.value.finishedVocabTier ?: toTier(_state.value.vocabCorrect)
        val nickname = toNickname(vocabTier, readingTier)

        _state.value = _state.value.copy(
            step = Step.Finished,
            readingMcqCorrect = readingMcqCorrect,
            finishedReadingTier = readingTier,
            finishedNickname = nickname
        )

        // 서버로 전송
        viewModelScope.launch {
            api.postNicknameResult(
                nickname = nickname,
                vocabTier = vocabTier,
                readingTier = readingTier,
                vocabCorrect = _state.value.vocabCorrect,
                readingCorrect = totalReadingCorrect
            )
        }
    }
}



// ===== 오케스트레이션 Screen =====
@Composable
fun NicknameTestFlowScreen(
    viewModel: NicknameViewModel = hiltViewModel(),
    onAllFinished: (nickname: String, vocabTier: VocabularyTier, readingTier: VocabularyTier) -> Unit = { _, _, _ -> }
    ,
    onExitToMyPage: () -> Unit = {},          // ← 마이페이지로 나가기
    onRetryFromStart: () -> Unit = {}         // ← 로딩스크린부터 재시작
) {
    val state by viewModel.state.collectAsState()

    // 🔹 알럿 on/off
    var showExitAlert by remember { mutableStateOf(false) }

    // 🔹 알럿 렌더
    if (showExitAlert) {
        SkipNickNameTestAlert.Show(
            onConfirm = {
                // 네(그만하기): 플로우 초기화 후 마이페이지로
                showExitAlert = false
                viewModel.resetFlow()
                onExitToMyPage()
            },
            onDismiss = {
                // 아니요(이어하기): 그냥 닫기
                showExitAlert = false
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        when (state.step) {
            Step.Vocab_1_9 -> {
                NicknameTestMcqScreen(
                    questions = viewModel.vocabQuestions,
                    answeredGlobalCount = 0,
                    // ⬇️ 첫 문항에서 뒤로가기를 누르면 알럿 띄우도록
                    onBackClick = { showExitAlert = true },
                    onFinishVocabulary = { _, correct ->
                        viewModel.onVocabFinished(correct)
                    }
                )
            }
            Step.Ox_10_11 -> {
                NicknameTestOxScreen(
                    questions = viewModel.oxQuestions,
                    answeredGlobalCount = 9,
                    onBackClick = { showExitAlert = true },   // ⬅️ 동일
                    onFinishOx = { oxCorrectCount ->
                        viewModel.onOxFinishedWithCount(oxCorrectCount)
                    }
                )
            }
            Step.Reading_12_18 -> {
                NicknameTestReadingMcqScreen(
                    questions = viewModel.readingQuestions,
                    answeredGlobalCount = 11,
                    onBackClick = { showExitAlert = true },   // ⬅️ 동일
                    onFinishReadingMcq = { _, correctCount ->
                        viewModel.onReadingFinished(correctCount)
                    }
                )
            }
            Step.Finished -> {
                val nick = state.finishedNickname ?: "별명 계산 중"
                NicknameTestResultScreen(
                    nickname = nick,
                    onRetry = {
                        viewModel.resetFlow()
                        onRetryFromStart()
                    },
                    onExit = { onExitToMyPage() }
                )
            }
        }
    }
}


// ===== Preview (DI 없이 미리보기용) =====
@Preview(showBackground = true)
@Composable
private fun PreviewNicknameTestFlowScreen_Vocab() {
    // Hilt 없이 ViewModel 대체: remember로 임시 인스턴스
    val fakeVm = remember { NicknameViewModel() }
    MaterialTheme {
        Surface {
            NicknameTestFlowScreen(
                viewModel = fakeVm,
                onAllFinished = { _, _, _ -> }
            )
        }
    }
}