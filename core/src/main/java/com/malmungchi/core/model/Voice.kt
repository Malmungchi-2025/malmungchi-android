package com.malmungchi.core.model


data class PromptResp(
    val success: Boolean,
    val mode: String,
    val title: String,
    val prompt: String
)

// ★ 변경: 서버 스펙 반영 (situation, question 추가)
//  - text: 서버가 fullText("[상황]\n: 질문")도 줄 수 있으니 호환용으로 nullable 유지
data class VoiceHelloResp(
    val success: Boolean,
    val mode: String,            // 항상 "job"
    val situation: String,       // 예: "면접 상황"
    val question: String,        // 예: "본인의 장단점이 무엇인가요?"
    val text: String?,           // 선택: "[면접 상황]\n: 본인의 장단점이..." (호환)
    val audioBase64: String?,    // 선택: MP3(base64)
    val mimeType: String?        // 예: "audio/mpeg"
)

// 대화 응답 (변경 없음)
data class VoiceChatResponse(
    val success: Boolean,
    val mode: String,         // "job" 또는 "daily"
    val userText: String,     // STT 결과
    val text: String,         // GPT 응답(봇 말풍선)
    val audioBase64: String?, // nullable
    val mimeType: String?,    // "audio/mpeg"
    val hint: String?,        // TIP (nullable)
    val needRetry: Boolean?,  // 재시도 여부
    val critique: String?     // 간단 요약 (nullable)
)
//보상 api
data class SimpleResp(
    val success: Boolean,
    val message: String
)

// 보상 응답 (complete-reward용)
data class AiChatRewardResp(
    val success: Boolean,
    val message: String,
    val todayReward: Int?,   // 15
    val totalPoint: Int?     // 지급 후 누적 포인트
)
//data class PromptResp(
//    val success: Boolean,
//    val mode: String,
//    val title: String,
//    val prompt: String
//)
//
//data class VoiceChatResp(
//    val success: Boolean,
//    val mode: String,
//    val userText: String,
//    val text: String,
//    val audioBase64: String?,   // base64 MP3
//    val mimeType: String?,      // "audio/mpeg"
//    val hint: String?
//)
//
//data class VoiceChatResponse(
//    val success: Boolean,
//    val mode: String,              // "job"
//    val userText: String,          // STT 결과
//    val text: String,              // 봇 본문(파란 테두리)
//    val audioBase64: String?,      // nullable
//    val mimeType: String?,         // e.g., "audio/mpeg"
//    val hint: String?,             // TIP (nullable)
//    val needRetry: Boolean?,       // 재시도 여부
//    val critique: String?          // 간단 피드백 요약
//)
//
//
//data class VoiceHelloResp(
//    val success: Boolean,
//    val mode: String,
//    val text: String,
//    val audioBase64: String?, // 선택: TTS 재생용
//    val mimeType: String?     // "audio/mpeg"
//)