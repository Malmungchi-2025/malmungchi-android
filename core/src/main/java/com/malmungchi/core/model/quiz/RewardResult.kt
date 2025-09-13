package com.malmungchi.core.model.quiz

data class RewardResult(
    val rewardPoint: Int,     // 이번 시도에 지급된 포인트 (15 또는 20)
    val basePoint: Int,       // 15
    val bonusAllCorrect: Int, // 0 또는 5
    val allCorrect: Boolean,
    val totalPoint: Int       // 지급 후 유저 총 포인트
)