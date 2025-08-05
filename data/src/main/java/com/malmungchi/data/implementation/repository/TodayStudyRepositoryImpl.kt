package com.malmungchi.data.implementation.repository

import com.malmungchi.data.api.ServerApi
import com.malmungchi.data.preference.AuthPreference
import javax.inject.Inject

import android.util.Log
import com.malmungchi.core.model.TodayQuote
import com.malmungchi.core.model.WordItem
import com.malmungchi.core.repository.TodayStudyRepository
import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.api.WordRequest
import com.malmungchi.data.api.WordSaveRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodayStudyRepositoryImpl(
    private val api: TodayStudyApi
) : TodayStudyRepository {

    override suspend fun generateTodayQuote(token: String): Result<TodayQuote> = withContext(Dispatchers.IO) {
        Log.d("API_GENERATE_QUOTE", "📡 [요청] POST /api/gpt/generate-quote")
        Log.d("API_GENERATE_QUOTE", "👉 Header.Authorization = Bearer ${token.take(10)}...") // 토큰 일부만 로그

        try {
            val res = api.generateQuote("Bearer $token")
            Log.d("API_GENERATE_QUOTE", "📥 [응답] success=${res.success}, message=${res.message}, result=${res.result}, studyId=${res.studyId}")

            if (res.success && res.result != null && res.studyId != null) {
                Result.success(TodayQuote(content = res.result, studyId = res.studyId))
            } else {
                Log.e("API_GENERATE_QUOTE", "❌ [실패] ${res.message}")
                Result.failure(Exception(res.message ?: "글감 생성 실패"))
            }
        } catch (e: Exception) {
            Log.e("API_GENERATE_QUOTE", "❌ [예외] ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchWordDefinition(token: String, word: String): Result<WordItem> = withContext(Dispatchers.IO) {
        Log.d("API_SEARCH_WORD", "📡 [요청] POST /api/word/search")
        Log.d("API_SEARCH_WORD", "👉 Header.Authorization = Bearer ${token.take(10)}...")
        Log.d("API_SEARCH_WORD", "👉 Body = WordRequest(word=$word)")

        try {
            val res = api.searchWord("Bearer $token", WordRequest(word))
            Log.d("API_SEARCH_WORD", "📥 [응답] success=${res.success}, message=${res.message}, result=${res.result}")

            if (res.success && res.result != null) {
                Result.success(res.result)
            } else {
                Log.e("API_SEARCH_WORD", "❌ [실패] ${res.message}")
                Result.failure(Exception(res.message ?: "단어 검색 실패"))
            }
        } catch (e: Exception) {
            Log.e("API_SEARCH_WORD", "❌ [예외] ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    override suspend fun saveWord(token: String, studyId: Int, wordItem: WordItem): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d("API_SAVE_WORD", "📡 [요청] POST /api/word/save")
        Log.d("API_SAVE_WORD", "👉 Header.Authorization = Bearer ${token.take(10)}...")
        Log.d("API_SAVE_WORD", "👉 Body = WordSaveRequest(studyId=$studyId, word=${wordItem.word}, meaning=${wordItem.meaning}, example=${wordItem.example})")

        try {
            val res = api.saveWord("Bearer $token", WordSaveRequest(studyId, wordItem.word, wordItem.meaning, wordItem.example))
            Log.d("API_SAVE_WORD", "📥 [응답] success=${res.success}, message=${res.message}")

            if (res.success) {
                Result.success(Unit)
            } else {
                Log.e("API_SAVE_WORD", "❌ [실패] ${res.message}")
                Result.failure(Exception(res.message ?: "단어 저장 실패"))
            }
        } catch (e: Exception) {
            Log.e("API_SAVE_WORD", "❌ [예외] ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    override suspend fun getVocabularyList(token: String, studyId: Int): Result<List<WordItem>> = withContext(Dispatchers.IO) {
        Log.d("API_VOCAB_LIST", "📡 [요청] GET /api/word/list?studyId=$studyId")
        Log.d("API_VOCAB_LIST", "👉 Header.Authorization = Bearer ${token.take(10)}...")

        try {
            val res = api.getVocabularyList("Bearer $token", studyId)
            Log.d("API_VOCAB_LIST", "📥 [응답] success=${res.success}, message=${res.message}, resultCount=${res.result?.size ?: 0}")

            if (res.success && res.result != null) {
                Result.success(res.result)
            } else {
                Log.e("API_VOCAB_LIST", "❌ [실패] ${res.message}")
                Result.failure(Exception(res.message ?: "단어 목록 조회 실패"))
            }
        } catch (e: Exception) {
            Log.e("API_VOCAB_LIST", "❌ [예외] ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}

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