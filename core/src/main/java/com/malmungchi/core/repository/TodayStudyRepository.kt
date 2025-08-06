package com.malmungchi.core.repository


import com.malmungchi.core.model.TodayQuote
import com.malmungchi.core.model.WordItem

interface TodayStudyRepository {
    /** ✅ 오늘의 학습 글귀 생성 (하루 1회) */
    suspend fun generateTodayQuote(token: String): Result<TodayQuote>

    /** ✅ 단어 검색 (GPT 호출만, DB 저장 안 함) */
    suspend fun searchWordDefinition(token: String, word: String): Result<WordItem>

    /** ✅ 단어 저장 (사용자가 저장 버튼 클릭 시) */
    suspend fun saveWord(token: String, studyId: Int, wordItem: WordItem): Result<Unit>

    /** ✅ 단어 목록 조회 (오늘의 학습 문단 단어 목록) */
    suspend fun getVocabularyList(token: String, studyId: Int): Result<List<WordItem>>

    // ✅ [추가] 필사 내용 저장
    suspend fun saveHandwriting(token: String, studyId: Int, content: String): Result<Unit>

    //필사한 내용 불러오기
    suspend fun getHandwriting(token: String, studyId: Int): Result<String>
}