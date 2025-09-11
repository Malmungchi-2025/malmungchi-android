package com.malmungchi.feature.quiz

/** 결과화면 등에서 받은 wrongIds 로 '재도전'용 QuizSet 을 만든다. */
fun buildRetrySetFromWrong(
    original: QuizSet,
    wrongIds: List<String>
): QuizSet {
    val filtered = original.steps.filter { it.id in wrongIds }
    return original.copy(
        id = original.id + "_retry",
        steps = filtered
    )
}

/** 확장함수 버전이 편하면 이것도 같이 제공해도 좋아요. */
fun QuizSet.rebuildForRetry(wrongIds: List<String>): QuizSet =
    buildRetrySetFromWrong(this, wrongIds)