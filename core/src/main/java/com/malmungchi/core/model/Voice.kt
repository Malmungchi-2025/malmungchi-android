package com.malmungchi.core.model

data class PromptResp(
    val success: Boolean,
    val mode: String,
    val title: String,
    val prompt: String
)

data class VoiceChatResp(
    val success: Boolean,
    val mode: String,
    val userText: String,
    val text: String,
    val audioBase64: String?,   // base64 MP3
    val mimeType: String?,      // "audio/mpeg"
    val hint: String?
)