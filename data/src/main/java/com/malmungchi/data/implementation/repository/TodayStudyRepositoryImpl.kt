package com.malmungchi.data.implementation.repository

import com.malmungchi.data.api.ServerApi
import com.malmungchi.data.preference.AuthPreference
import javax.inject.Inject


import android.util.Log
import com.malmungchi.core.model.QuizAnswerRequest
import com.malmungchi.core.model.QuizGenerationRequest
import com.malmungchi.core.model.QuizItem
import com.malmungchi.core.model.StudyBundle
import com.malmungchi.core.model.TodayQuote
import com.malmungchi.core.model.WordItem
import com.malmungchi.core.repository.TodayStudyRepository
import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.api.WordRequest
import com.malmungchi.data.api.WordSaveRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.malmungchi.data.api.mapper.toDomain
import java.time.format.DateTimeFormatter


class TodayStudyRepositoryImpl(
    private val api: TodayStudyApi
) : TodayStudyRepository {

    /** ✅ 날짜별 통합 조회 (도메인 반환) */
    override suspend fun getStudyByDate(date: LocalDate): Result<StudyBundle> = withContext(Dispatchers.IO) {
        val iso = date.format(DateTimeFormatter.ISO_DATE)
        Log.d("API_STUDY_BY_DATE", "📡 [요청] GET /api/gpt/study/by-date?date=$iso")
        runCatching {
            val res = api.getStudyByDate(iso)
            check(res.success && res.result != null) { res.message ?: "해당 날짜 학습 없음" }
            res.result!!.toDomain()   // DTO → 도메인
        }
    }

    /** ✅ 달력용: 해당 연월의 학습 날짜 목록 */
    override suspend fun getAvailableDates(year: String, month: String): Result<List<String>> = withContext(Dispatchers.IO) {
        Log.d("API_STUDY_DATES", "📡 [요청] GET /api/gpt/study/available-dates?year=$year&month=$month")
        runCatching {
            val res = api.getAvailableDates(year, month)
            check(res.success && res.result != null) { res.message ?: "학습 날짜 목록 조회 실패" }
            res.result!!
        }
    }


    override suspend fun generateTodayQuote(): Result<TodayQuote> = withContext(Dispatchers.IO) {
        Log.d("API_GENERATE_QUOTE", "📡 [요청] POST /api/gpt/generate-quote")
        runCatching {
            val res = api.generateQuote() // <-- QuoteResponse
            Log.d("API_GENERATE_QUOTE", "📥 [응답] success=${res.success}, msg=${res.message}, studyId=${res.studyId}")

            check(res.success) { res.message ?: "글감 생성 실패" }
            val content = res.result ?: error("result(본문)가 null")
            val studyId = res.studyId ?: error("studyId 누락")

            TodayQuote(content = content, studyId = studyId)
        }
    }

    /** ✅ 단어 검색 */
    override suspend fun searchWordDefinition(word: String): Result<WordItem> = withContext(Dispatchers.IO) {
        Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/vocabulary/search word=$word")
        runCatching {
            val res = api.searchWord(WordRequest(word))
            check(res.success && res.result != null) { res.message ?: "단어 검색 실패" }
            res.result!!
        }
    }

    /** ✅ 단어 저장 */
    override suspend fun saveWord(studyId: Int, word: WordItem): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d("API_SAVE_WORD", "📡 [요청] POST /api/vocabulary studyId=$studyId word=${word.word}")
        runCatching {
            val res = api.saveWord(WordSaveRequest(studyId, word.word, word.meaning, word.example))
            check(res.success) { res.message ?: "단어 저장 실패" }
            Unit
        }
    }

    /** ✅ 단어 목록 조회 */
    override suspend fun getVocabularyList(studyId: Int): Result<List<WordItem>> = withContext(Dispatchers.IO) {
        Log.d("API_VOCAB_LIST", "📡 [요청] GET /api/vocabulary/$studyId")
        runCatching {
            val res = api.getVocabularyList(studyId)
            check(res.success && res.result != null) { res.message ?: "단어 목록 조회 실패" }
            res.result!!
        }
    }

    /** ✅ 필사 저장 */
    override suspend fun saveHandwriting(studyId: Int, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d("API_HANDWRITING_SAVE", "📡 [요청] POST /api/study/handwriting")
        runCatching {
            val res = api.saveHandwriting(
                TodayStudyApi.HandwritingRequest(
                    study_id = studyId,
                    content = content
                )
            )
            check(res.success) { res.message ?: "필사 저장 실패" }
            Unit
        }
    }

    /** ✅ 필사 조회 */
    override suspend fun getHandwriting(studyId: Int): Result<String> = withContext(Dispatchers.IO) {
        Log.d("API_HANDWRITING_GET", "📡 [요청] GET /api/study/handwriting/$studyId")
        runCatching {
            val res = api.getHandwriting(studyId)
            check(res.success && res.result != null) { res.message ?: "필사 로드 실패" }
            res.result!!
        }
    }

    /** ✅ 퀴즈 생성 */
    override suspend fun generateQuiz(studyId: Int, text: String): Result<List<QuizItem>> = withContext(Dispatchers.IO) {
        Log.d("API_QUIZ_GEN", "📡 [요청] POST /api/gpt/generate-quiz")
        runCatching {
            val res = api.generateQuiz(QuizGenerationRequest(text = text, studyId = studyId))
            check(res.success && res.result != null) { res.message ?: "퀴즈 생성 실패" }
            res.result!!
        }
    }

    /** ✅ 퀴즈 목록 조회 */
    override suspend fun getQuizList(studyId: Int): Result<List<QuizItem>> = withContext(Dispatchers.IO) {
        Log.d("API_QUIZ_LIST", "📡 [요청] GET /api/gpt/quiz/$studyId")
        runCatching {
            val res = api.getQuizList(studyId)
            check(res.success && res.result != null) { res.message ?: "퀴즈 조회 실패" }
            res.result!!
        }
    }

    /** ✅ 퀴즈 답안 저장 */
    override suspend fun saveQuizAnswer(req: QuizAnswerRequest): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d("API_QUIZ_ANSWER", "📡 [요청] POST /api/gpt/quiz/answer")
        runCatching {
            val res = api.saveQuizAnswer(req)
            check(res.success) { res.message ?: "퀴즈 저장 실패" }
            Unit
        }
    }

    /** ✅ 오늘의 학습 포인트 지급 */
    override suspend fun rewardTodayStudy(): Result<Int> = withContext(Dispatchers.IO) {
        Log.d("API_REWARD", "📡 [요청] POST /api/gpt/study/complete-reward")
        runCatching {
            val res = api.rewardTodayStudy()
            check(res.success && res.result != null) { res.message ?: "포인트 지급 실패" }
            res.result!!.todayReward // 오늘 지급 포인트(보통 15)
            // 필요하다면 res.result!!.totalPoint로 전체 포인트도 반환 가능
        }
    }
}
//이전 작동 코드
//class TodayStudyRepositoryImpl(
//    private val api: TodayStudyApi
//) : TodayStudyRepository {
//
//    /** ✅ 오늘의 학습 글감 가져오기 */
//    override suspend fun generateTodayQuote(token: String): Result<TodayQuote> =
//        withContext(Dispatchers.IO) {
//            Log.d("API_GENERATE_QUOTE", "📡 [요청] POST /api/gpt/generate-quote")
//            Log.d("API_GENERATE_QUOTE", "👉 Header.Authorization = Bearer ${token.take(10)}...")
//
//            try {
//                val res = api.generateQuote("Bearer $token")
//                Log.d(
//                    "API_GENERATE_QUOTE",
//                    "📥 [응답] success=${res.success}, message=${res.message}, result=${res.result}, studyId=${res.studyId}"
//                )
//
//                if (res.success && res.result != null && res.studyId != null) {
//                    Result.success(TodayQuote(content = res.result, studyId = res.studyId))
//                } else {
//                    Log.e("API_GENERATE_QUOTE", "❌ [실패] ${res.message}")
//                    Result.failure(Exception(res.message ?: "글감 생성 실패"))
//                }
//            } catch (e: Exception) {
//                Log.e("API_GENERATE_QUOTE", "❌ [예외] ${e.localizedMessage}", e)
//                Result.failure(e)
//            }
//        }
//
//    /** ✅ 단어 검색 (서버 응답이 배열이므로 첫 번째 단어만 사용) */
//    override suspend fun searchWordDefinition(token: String, word: String): Result<WordItem> =
//        withContext(Dispatchers.IO) {
//            Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/vocabulary/search")
//            Log.d("API_SEARCH_WORD", "👉 Header.Authorization = Bearer ${token.take(10)}...")
//            Log.d("API_SEARCH_WORD", "👉 Body = WordRequest(word=$word)")
//
//            try {
//                val res = api.searchWord("Bearer $token", com.malmungchi.data.api.WordRequest(word))
//                Log.d(
//                    "API_SEARCH_WORD",
//                    "📥 [응답] success=${res.success}, message=${res.message}"
//                )
//
//                val item = res.result
//                if (res.success && item != null) {
//                    Result.success(item)
//                } else {
//                    Log.e("API_SEARCH_WORD", "❌ [실패] ${res.message}")
//                    Result.failure(Exception(res.message ?: "단어 검색 실패"))
//                }
//            } catch (e: Exception) {
//                Log.e("API_SEARCH_WORD", "❌ [예외] ${e.localizedMessage}", e)
//                Result.failure(e)
//            }
//        }
//
//    /** ✅ 단어 저장 */
//    override suspend fun saveWord(token: String, studyId: Int, wordItem: WordItem): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            Log.d("API_SAVE_WORD", "📡 [요청] POST /api/vocabulary")
//            Log.d("API_SAVE_WORD", "👉 Header.Authorization = Bearer ${token.take(10)}...")
//            Log.d(
//                "API_SAVE_WORD",
//                "👉 Body = WordSaveRequest(studyId=$studyId, word=${wordItem.word}, meaning=${wordItem.meaning}, example=${wordItem.example})"
//            )
//
//            try {
//                val res = api.saveWord(
//                    "Bearer $token",
//                    WordSaveRequest(studyId, wordItem.word, wordItem.meaning, wordItem.example)
//                )
//                Log.d("API_SAVE_WORD", "📥 [응답] success=${res.success}, message=${res.message}")
//
//                if (res.success) {
//                    Result.success(Unit)
//                } else {
//                    Log.e("API_SAVE_WORD", "❌ [실패] ${res.message}")
//                    Result.failure(Exception(res.message ?: "단어 저장 실패"))
//                }
//            } catch (e: Exception) {
//                Log.e("API_SAVE_WORD", "❌ [예외] ${e.localizedMessage}", e)
//                Result.failure(e)
//            }
//        }
//
//    /** ✅ 단어 목록 조회 */
//    override suspend fun getVocabularyList(token: String, studyId: Int): Result<List<WordItem>> =
//        withContext(Dispatchers.IO) {
//            Log.d("API_VOCAB_LIST", "📡 [요청] GET /api/vocabulary/$studyId")
//            Log.d("API_VOCAB_LIST", "👉 Header.Authorization = Bearer ${token.take(10)}...")
//
//            try {
//                val res = api.getVocabularyList("Bearer $token", studyId)
//                Log.d(
//                    "API_VOCAB_LIST",
//                    "📥 [응답] success=${res.success}, message=${res.message}, resultCount=${res.result?.size ?: 0}"
//                )
//
//                if (res.success && res.result != null) {
//                    Result.success(res.result)
//                } else {
//                    Log.e("API_VOCAB_LIST", "❌ [실패] ${res.message}")
//                    Result.failure(Exception(res.message ?: "단어 목록 조회 실패"))
//                }
//            } catch (e: Exception) {
//                Log.e("API_VOCAB_LIST", "❌ [예외] ${e.localizedMessage}", e)
//                Result.failure(e)
//            }
//        }
//
//    //필사한 내용 저장하기!
//    override suspend fun saveHandwriting(
//        token: String,
//        studyId: Int,
//        content: String
//    ): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            try {
//                val res = api.saveHandwriting(
//                    "Bearer $token",
//                    TodayStudyApi.HandwritingRequest(studyId, content)
//                )
//                if (res.success) Result.success(Unit)
//                else Result.failure(Exception(res.message ?: "필사 저장 실패"))
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
//        }
//
//
//    override suspend fun getHandwriting(token: String, studyId: Int): Result<String> =
//        withContext(Dispatchers.IO) {
//            try {
//                val res = api.getHandwriting("Bearer $token", studyId) // ✅ Retrofit API 호출
//                if (res.success && res.result != null) {
//                    Result.success(res.result)
//                } else {
//                    Result.failure(Exception(res.message ?: "필사 로드 실패"))
//                }
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
//        }
//
//    override suspend fun generateQuiz(token: String, studyId: Int, text: String): Result<List<QuizItem>> =
//        withContext(Dispatchers.IO) {
//            try {
//                val res = api.generateQuiz("Bearer $token", QuizGenerationRequest(text, studyId))
//                if (res.success && res.result != null) Result.success(res.result)
//                else Result.failure(Exception(res.message ?: "퀴즈 생성 실패"))
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
//        }
//
//    override suspend fun getQuizList(token: String, studyId: Int): Result<List<QuizItem>> =
//        withContext(Dispatchers.IO) {
//            try {
//                val res = api.getQuizList("Bearer $token", studyId)
//                if (res.success && res.result != null) Result.success(res.result)
//                else Result.failure(Exception(res.message ?: "퀴즈 조회 실패"))
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
//        }
//
//    override suspend fun saveQuizAnswer(token: String, request: QuizAnswerRequest): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            try {
//                val res = api.saveQuizAnswer("Bearer $token", request)
//                if (res.success) Result.success(Unit)
//                else Result.failure(Exception(res.message ?: "퀴즈 저장 실패"))
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
//        }
//}


//class TodayStudyRepositoryImpl(
//    private val api: TodayStudyApi
//) : TodayStudyRepository {
//
//    override suspend fun generateTodayQuote(token: String): Result<TodayQuote> = withContext(Dispatchers.IO) {
//        try {
//            val res = api.generateQuote("Bearer $token")
//            if (res.success && res.result != null && res.studyId != null) {
//                Result.success(TodayQuote(content = res.result, studyId = res.studyId))
//            } else {
//                Result.failure(Exception(res.message ?: "글감 생성 실패"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun searchWordDefinition(token: String, word: String): Result<WordItem> = withContext(Dispatchers.IO) {
//        try {
//            val res = api.searchWord("Bearer $token", WordRequest(word))
//            if (res.success && res.result != null) Result.success(res.result)
//            else Result.failure(Exception(res.message ?: "단어 검색 실패"))
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun saveWord(token: String, studyId: Int, wordItem: WordItem): Result<Unit> = withContext(Dispatchers.IO) {
//        try {
//            val res = api.saveWord("Bearer $token", WordSaveRequest(studyId, wordItem.word, wordItem.meaning, wordItem.example))
//            if (res.success) Result.success(Unit)
//            else Result.failure(Exception(res.message ?: "단어 저장 실패"))
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun getVocabularyList(token: String, studyId: Int): Result<List<WordItem>> = withContext(Dispatchers.IO) {
//        try {
//            val res = api.getVocabularyList("Bearer $token", studyId)
//            if (res.success && res.result != null) Result.success(res.result)
//            else Result.failure(Exception(res.message ?: "단어 목록 조회 실패"))
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}