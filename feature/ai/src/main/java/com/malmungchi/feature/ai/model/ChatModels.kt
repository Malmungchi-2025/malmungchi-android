package com.malmungchi.feature.ai.model

enum class Role { User, Bot }
enum class BubbleStyle { Normal, UserRetryNeeded, BotFeedback }

data class ChatMessage(
    val role: Role,
    val text: String,
    val style: BubbleStyle = BubbleStyle.Normal
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isRecording: Boolean = false,
    val isTranscribing: Boolean = false,
    val isLoading: Boolean = false,
    val botReplyCount: Int = 0
)