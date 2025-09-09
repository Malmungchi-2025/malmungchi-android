package com.malmungchi.feature.quiz

enum class QuizCategory(val id: String, val displayName: String) {
    JOB("job", "취업 준비"),
    BASIC("basic", "기초"),
    PRACTICE("practice", "활용"),
    DEEP("deep", "심화"),
    ADVANCED("advanced", "고급");

    companion object {
        fun fromId(id: String?): QuizCategory? = values().firstOrNull { it.id == id }
    }
}