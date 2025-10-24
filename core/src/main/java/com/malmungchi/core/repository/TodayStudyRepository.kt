package com.malmungchi.core.repository


import com.malmungchi.core.model.QuizAnswerRequest
import com.malmungchi.core.model.QuizItem
import com.malmungchi.core.model.StudyBundle
import com.malmungchi.core.model.TodayQuote
import com.malmungchi.core.model.WordItem

import java.time.LocalDate

interface TodayStudyRepository {
    // ✅ 지정 날짜의 통합 학습(글감/필사/단어/퀴즈+채점)
    suspend fun getStudyByDate(date: LocalDate): Result<StudyBundle>

    // ✅ 달력용: 해당 연월의 학습 존재 날짜 목록
    suspend fun getAvailableDates(year: String, month: String): Result<List<String>>
    suspend fun generateTodayQuote(): Result<TodayQuote>
    suspend fun searchWordDefinition(word: String): Result<WordItem>
    suspend fun saveWord(studyId: Int, word: WordItem): Result<Unit>
    suspend fun getVocabularyList(studyId: Int): Result<List<WordItem>>
    suspend fun getHandwriting(studyId: Int): Result<String>
    suspend fun saveHandwriting(studyId: Int, content: String): Result<Unit>
    suspend fun generateQuiz(studyId: Int, text: String): Result<List<QuizItem>>
    suspend fun getQuizList(studyId: Int): Result<List<QuizItem>>
    suspend fun saveQuizAnswer(req: QuizAnswerRequest): Result<Unit>
    /** ✅ 오늘의 학습 포인트 지급 (성공시 오늘 지급분 리턴) */
    suspend fun rewardTodayStudy(): Result<Int>

    /** ✅ 특정 날짜의 학습 단계(progress_level) 조회 */
    suspend fun getStudyProgress(date: LocalDate): Result<Int>

    /** ✅ 특정 날짜의 학습 단계 업데이트 */
    suspend fun updateStudyProgress(date: LocalDate, step: Int): Result<Unit>

    /** ✅ 특정 주(week)의 날짜별 학습 단계 맵 조회 */
    suspend fun getStudyProgressWeek(center: LocalDate): Result<Map<String, Int>>
}

//interface TodayStudyRepository {
//    /** ✅ 오늘의 학습 글귀 생성 (하루 1회) */
//    suspend fun generateTodayQuote(token: String): Result<TodayQuote>
//
//    /** ✅ 단어 검색 (GPT 호출만, DB 저장 안 함) */
//    suspend fun searchWordDefinition(token: String, word: String): Result<WordItem>
//
//    /** ✅ 단어 저장 (사용자가 저장 버튼 클릭 시) */
//    suspend fun saveWord(token: String, studyId: Int, wordItem: WordItem): Result<Unit>
//
//    /** ✅ 단어 목록 조회 (오늘의 학습 문단 단어 목록) */
//    suspend fun getVocabularyList(token: String, studyId: Int): Result<List<WordItem>>
//
//    // ✅ [추가] 필사 내용 저장
//    suspend fun saveHandwriting(token: String, studyId: Int, content: String): Result<Unit>
//
//    //필사한 내용 불러오기
//    suspend fun getHandwriting(token: String, studyId: Int): Result<String>
//
//    //퀴즈
//    suspend fun generateQuiz(token: String, studyId: Int, text: String): Result<List<QuizItem>>
//    suspend fun getQuizList(token: String, studyId: Int): Result<List<QuizItem>>
//    suspend fun saveQuizAnswer(token: String, request: QuizAnswerRequest): Result<Unit>
//}