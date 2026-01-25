package tn.petcare_android.data.model.ai

import java.util.UUID

/**
 * AI Chat Message Data Class
 * Represents a message in the AI chat conversation
 */
data class AIChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null
)
