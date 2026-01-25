package tn.petcare_android.viewmodel.ai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.ai.AIChatMessage


class AIChatViewModel(@Suppress("UNUSED_PARAMETER") context: Context) : ViewModel() {

    private val aiApi = RetrofitInstance.aiApi

    private val _messages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val messages: StateFlow<List<AIChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
// Add welcome message
        _messages.value = listOf(
            AIChatMessage(
                content = "👋 Hi! I'm your AI vet assistant. I can help answer questions about your pet's health, nutrition, behavior, and general care. How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Send message to AI and get response
     * iOS Reference: GeminiService.swift generateText
     */

    fun sendMessage(userMessage: String, imageBase64: String? = null) {
        viewModelScope.launch {
            var conversationHistory: String? = null
// Compute userContent outside try so it's visible in catch/retry logic
            val userContent = if (!imageBase64.isNullOrEmpty()) {
                if (userMessage.isBlank()) "analyse this image"
                else "$userMessage\n[photo attached]"
            } else userMessage

            try {
                _error.value = null

// Add user message to the list
                val userMsg = AIChatMessage(
                    content = userContent,
                    isFromUser = true
                )
                _messages.value = _messages.value + userMsg

                _isLoading.value = true

// Build conversation history BEFORE calling API
                conversationHistory = _messages.value
                    .takeLast(10)
                    .joinToString("\n") { msg ->
                        if (msg.isFromUser) "User: ${msg.content}"
                        else "Assistant: ${msg.content}"
                    }
                    .takeIf { it.isNotEmpty() }

// Call the conversational endpoint `/chatbot/message` directly.
// If an image is present, prefer multipart upload (file part) to avoid large JSON bodies.
// Ensure message field sent to server is non-empty; prefer the userContent we displayed above
                val messageForRequest = if (userContent.isBlank()) "analyse this image" else userContent

                val response = if (!imageBase64.isNullOrEmpty()) {
                    // For now, send as JSON with base64 image
                    val request = tn.petcare_android.data.model.ai.ChatbotMessageRequest(
                        message = messageForRequest,
                        context = conversationHistory,
                        image = imageBase64
                    )
                    aiApi.sendChatMessage(request)
                } else {
                    val request = tn.petcare_android.data.model.ai.ChatbotMessageRequest(
                        message = messageForRequest,
                        context = conversationHistory,
                        image = null
                    )
                    aiApi.sendChatMessage(request)
                }

                // Add AI response
                val aiMsg = AIChatMessage(
                    content = response.response,
                    isFromUser = false
                )
                _messages.value = _messages.value + aiMsg
                // Refresh messages from server so saved imageUrl and message ordering are reflected
                fetchHistory()

            } catch (e: Exception) {

// Handle 413 retry logic
                if (e is HttpException && e.code() == 413 && !imageBase64.isNullOrEmpty()) {
                    android.util.Log.w("AIChatViewModel", "Image too large (413). Retrying without image.")
                    try {
                        val retryRequest = tn.petcare_android.data.model.ai.ChatbotMessageRequest(
                            message = if (userContent.isBlank()) "analyse this image" else userContent,
                            context = conversationHistory,
                            image = null
                        )

                        val retryResponse = aiApi.sendChatMessage(retryRequest)

                        val infoMsg = AIChatMessage(
                            content = "Note: the image was too large and was not sent. Responding to text only.",
                            isFromUser = false
                        )
                        val aiMsg = AIChatMessage(
                            content = retryResponse.response,
                            isFromUser = false
                        )

                        _messages.value = _messages.value + infoMsg + aiMsg
                        // On retry success, refresh messages from server
                        fetchHistory()
                        _error.value = null

                    } catch (ex2: Exception) {
                        _error.value = "Failed to get AI response: ${ex2.message}"
                        _messages.value = _messages.value + AIChatMessage(
                            content = "I'm sorry, I'm having trouble connecting right now.",
                            isFromUser = false
                        )
                    }

                } else {
// Generic error
                    _error.value = "Failed to get AI response: ${e.message}"
                    _messages.value = _messages.value + AIChatMessage(
                        content = "I'm sorry, I'm having trouble connecting right now.",
                        isFromUser = false
                    )
                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch conversation history from backend and populate messages
     */
    fun fetchHistory(limit: Int = 50, offset: Int = 0) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val resp = aiApi.getHistory(limit, offset)
                val mapped = resp.messages.map { item ->
                    val ts = try {
                        java.time.Instant.parse(item.createdAt).toEpochMilli()
                    } catch (t: Throwable) {
                        System.currentTimeMillis()
                    }
                    AIChatMessage(
                        content = item.content,
                        isFromUser = item.role == "user",
                        timestamp = ts,
                        imageUrl = item.imageUrl
                    )
                }
                if (mapped.isNotEmpty()) {
                    _messages.value = mapped
                }
            } catch (e: Exception) {
                android.util.Log.w("AIChatViewModel", "Failed fetching history: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete conversation history on server for current user and clear local messages
     */
    fun clearHistoryServer() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val resp = aiApi.deleteHistory()
                if (resp.isSuccessful) {
// Clear local messages to a default welcome
                    _messages.value = listOf(
                        AIChatMessage(
                            content = "Chat cleared. How can I help you today?",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    _error.value = null
                } else {
                    _error.value = "Failed to clear history: ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to clear history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
     * Clear conversation history
     */
    fun clearChat() {
        _messages.value = listOf(
            AIChatMessage(
                content = "Chat cleared. How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
        _error.value = null
    }

    /**
     * Get quick response for specific pet health topics
     */
    fun getQuickAdvice(topic: String) {
        val quickPrompts = mapOf(
            "vaccines" to "What vaccines does a typical dog/cat need and when?",
            "nutrition" to "What are the nutritional requirements for a healthy pet?",
            "symptoms" to "What are common pet health symptoms I should watch for?",
            "emergencies" to "What are veterinary emergencies that require immediate attention?",
            "grooming" to "How often should I groom my pet and what does it involve?"
        )

        quickPrompts[topic]?.let { prompt ->
            sendMessage(prompt)
        }
    }
}
