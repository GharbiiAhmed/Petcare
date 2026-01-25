package tn.petcare_android.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Message Refresh Manager
 * 
 * iOS Reference: ChatViewModel.swift lines 487-504
 * iOS uses NotificationCenter.default.addObserver for "NewMessageReceived"
 * 
 * Android equivalent: Uses SharedFlow to emit events when FCM message received
 * ChatViewModel subscribes to this flow to refresh messages in real-time
 */
object MessageRefreshManager {
    
    /**
     * Event emitted when a new message notification is received
     * Contains conversationId to identify which conversation needs refresh
     */
    private val _messageReceived = MutableSharedFlow<String>() // conversationId
    val messageReceived: SharedFlow<String> = _messageReceived.asSharedFlow()
    
    /**
     * Called by FCMService when message notification is received
     * Matching iOS: NotificationCenter.default.post(name: "NewMessageReceived")
     */
    suspend fun onMessageReceived(conversationId: String) {
        _messageReceived.emit(conversationId)
    }
}

