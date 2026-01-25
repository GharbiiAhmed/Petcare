package tn.petcare_android.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MediaType.Companion.get
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.chat.*
import tn.petcare_android.util.MessageRefreshManager

/**
 * Chat ViewModel with FCM-only Support (matching iOS implementation)
 * iOS Reference: ChatViewModel.swift
 * 
 * Configuration matching iOS:
 * - useFCM = true          ✅ FCM for real-time updates
 * - useSockets = false     ❌ No Socket.IO
 * - pollingEnabled = false ❌ No polling
 * 
 * Real-time updates handled via FCM notifications:
 * - When FCM received → refreshMessages() is called
 * - When conversation open → messages auto-refresh
 * - When in background → Push notification shown
 */
class ChatViewModel : ViewModel() {
    
    private val chatApi = RetrofitInstance.chatApi
    
    // Configuration matching iOS
    private val useFCM = true
    private val useSockets = false
    private val pollingEnabled = false
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentConversationId: String? = null
    
    init {
        // Setup FCM message listener (iOS Reference: ChatViewModel.swift lines 487-504)
        viewModelScope.launch {
            MessageRefreshManager.messageReceived.collect { conversationId ->
                // Only refresh if this message is for the currently open conversation
                if (conversationId == currentConversationId) {
                    refreshMessages()
                }
            }
        }
    }
    
    /**
     * Load all conversations for the current user
     * iOS Reference: ChatViewModel.swift loadConversations()
     */
    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                android.util.Log.d("ChatViewModel", "Loading conversations...")
                val result = chatApi.getConversations()
                android.util.Log.d("ChatViewModel", "Loaded ${result.size} conversations")
                _conversations.value = result
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error loading conversations: ${e.message}", e)
                _error.value = e.message ?: "Failed to load conversations"
                _conversations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load messages for a specific conversation
     * iOS Reference: ChatViewModel.swift loadMessages()
     */
    fun loadMessages(conversationId: String) {
        currentConversationId = conversationId
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                android.util.Log.d("ChatViewModel", "Loading messages for conversation: $conversationId")
                val messages = chatApi.getMessages(conversationId)
                android.util.Log.d("ChatViewModel", "Loaded ${messages.size} messages")
                _messages.value = messages
                
                // Mark as read when viewing conversation (matching iOS behavior)
                markConversationAsRead(conversationId)
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error loading messages: ${e.message}", e)
                _error.value = e.message ?: "Failed to load messages"
                _messages.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh messages for current conversation
     * Called when FCM notification is received
     * iOS Reference: ChatViewModel.swift - FCM notification handler
     */
    fun refreshMessages() {
        currentConversationId?.let { conversationId ->
            viewModelScope.launch {
                try {
                    val messages = chatApi.getMessages(conversationId)
                    _messages.value = messages
                    
                    // Also refresh conversations to update unread counts
                    loadConversations()
                } catch (e: Exception) {
                    // Silent fail - don't disrupt user experience
                }
            }
        }
    }
    
    /**
     * Clear messages (for new empty conversations)
     */
    fun clearMessages() {
        _messages.value = emptyList()
    }
    
    /**
     * Leave current conversation (cleanup when navigating away)
     * iOS Reference: ChatViewModel.swift stopPolling()
     */
    fun leaveCurrentConversation() {
        currentConversationId = null
    }
    
    /**
     * Get or create conversation with a participant
     * iOS Reference: ChatViewModel.swift getOrCreateConversation()
     * iOS: POST /messages/conversations with participantId
     */
    suspend fun getOrCreateConversation(participantId: String): Conversation? {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val request = CreateConversationRequest(participantId = participantId)
            val conversation = chatApi.getOrCreateConversation(request)
            
            // Refresh conversations list
            loadConversations()
            
            _isLoading.value = false
            conversation
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to get/create conversation"
            _isLoading.value = false
            null
        }
    }
    
    /**
     * Send a message
     * iOS Reference: ChatViewModel.swift sendMessage()
     * iOS uses POST /messages with recipientId (creates conversation if needed)
     */
    suspend fun sendMessage(recipientId: String, content: String, conversationId: String? = null): Boolean {
        return try {
            val request = SendMessageRequest(
                recipientId = recipientId,
                content = content,
                conversationId = conversationId
            )
            val newMessage = chatApi.sendMessage(request)
            
            // Add message to local list immediately
            _messages.value = _messages.value + newMessage
            
            // Update current conversation ID if we didn't have one
            if (currentConversationId == null && newMessage.normalizedConversationId.isNotEmpty()) {
                currentConversationId = newMessage.normalizedConversationId
            }
            
            // Backend automatically sends FCM notification to recipient
            // Refresh conversations to update last message
            loadConversations()
            
            true
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to send message"
            false
        }
    }
    
    /**
     * Edit a message
     * iOS Reference: ChatViewModel.swift updateMessage()
     */
    suspend fun editMessage(messageId: String, newContent: String): Boolean {
        return try {
            val request = EditMessageRequest(newContent)
            val updatedMessage = chatApi.editMessage(messageId, request)
            
            // Update message in local list
            _messages.value = _messages.value.map { 
                if (it.id == messageId) updatedMessage else it 
            }
            
            // Refresh conversations
            loadConversations()
            
            true
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to edit message"
            false
        }
    }
    
    /**
     * Delete a message
     * iOS Reference: ChatViewModel.swift deleteMessage()
     */
    suspend fun deleteMessage(messageId: String): Boolean {
        return try {
            chatApi.deleteMessage(messageId)
            
            // Remove from local list
            _messages.value = _messages.value.filterNot { it.id == messageId }
            
            // Refresh conversations
            loadConversations()
            
            true
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to delete message"
            false
        }
    }
    
    /**
     * Upload and send audio message
     * Backend: POST /messages with multipart/form-data
     * iOS Reference: ChatView.swift audio message handling
     */
    suspend fun uploadAudioMessage(
        recipientId: String,
        conversationId: String?,
        audioFile: java.io.File
    ): Message? {
        return try {
            _isLoading.value = true
            _error.value = null
            
            // Create multipart body parts
            val recipientIdPart = okhttp3.MultipartBody.Part.createFormData(
                "recipientId",
                recipientId
            )
            val contentPart = okhttp3.MultipartBody.Part.createFormData(
                "content",
                "🎤 Audio message" // Placeholder text
            )
            val conversationIdPart = conversationId?.let {
                okhttp3.MultipartBody.Part.createFormData("conversationId", it)
            }
            val audioPart = okhttp3.MultipartBody.Part.createFormData(
                "audio",
                audioFile.name,
                okhttp3.RequestBody.create(
                    "audio/m4a".toMediaTypeOrNull() ?: "audio/*".toMediaTypeOrNull()!!,
                    audioFile
                )
            )
            
            // Send audio message
            val newMessage = chatApi.sendAudioMessage(
                recipientId = recipientIdPart,
                content = contentPart,
                conversationId = conversationIdPart,
                audio = audioPart
            )
            
            // Add message to local list immediately
            _messages.value = _messages.value + newMessage
            
            // Update current conversation ID if we didn't have one
            if (currentConversationId == null && newMessage.normalizedConversationId.isNotEmpty()) {
                currentConversationId = newMessage.normalizedConversationId
            }
            
            // Backend automatically sends FCM notification to recipient
            // Refresh conversations to update last message
            loadConversations()
            
            _isLoading.value = false
            newMessage
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Error uploading audio message: ${e.message}", e)
            _error.value = e.message ?: "Failed to upload audio message"
            _isLoading.value = false
            null
        }
    }
    
    /**
     * Delete a conversation
     * iOS Reference: ChatViewModel.swift deleteConversation()
     */
    suspend fun deleteConversation(conversationId: String): Boolean {
        return try {
            chatApi.deleteConversation(conversationId)
            
            // Remove from local list
            _conversations.value = _conversations.value.filterNot { it.normalizedId == conversationId }
            
            true
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to delete conversation"
            false
        }
    }
    
    /**
     * Mark conversation messages as read
     * iOS Reference: ChatViewModel.swift markConversationAsRead()
     */
    suspend fun markConversationAsRead(conversationId: String) {
        try {
            chatApi.markConversationAsRead(conversationId)
            
            // Update unread counts locally
            _conversations.value = _conversations.value.map {
                if (it.id == conversationId) it.copy(unreadCount = 0) else it
            }
        } catch (e: Exception) {
            // Silently fail for read receipts
        }
    }
}
