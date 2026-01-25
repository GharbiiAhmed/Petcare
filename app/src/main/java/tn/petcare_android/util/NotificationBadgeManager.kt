package tn.petcare_android.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance

/**
 * Notification Badge Manager
 * Manages unread notification and message counts for display in TopNavBar
 * iOS Reference: Similar to NotificationManager badge counts
 */
class NotificationBadgeManager private constructor() {
    
    companion object {
        @Volatile
        private var instance: NotificationBadgeManager? = null
        
        fun getInstance(): NotificationBadgeManager {
            return instance ?: synchronized(this) {
                instance ?: NotificationBadgeManager().also { instance = it }
            }
        }
    }
    
    private val _notificationCount = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = _notificationCount.asStateFlow()
    
    private val _messageCount = MutableStateFlow(0)
    val messageCount: StateFlow<Int> = _messageCount.asStateFlow()
    
    /**
     * Update notification count from backend
     */
    suspend fun updateNotificationCount() {
        try {
            // Fetch all notifications and count unread ones
            val notifications = RetrofitInstance.notificationApi.getNotifications()
            val unreadCount = notifications.count { !it.read }
            _notificationCount.value = unreadCount
        } catch (e: Exception) {
            // Silently fail - don't disrupt user experience
            android.util.Log.e("NotificationBadgeManager", "Failed to update notification count: ${e.message}")
        }
    }
    
    /**
     * Update message count (unread conversations)
     */
    suspend fun updateMessageCount() {
        try {
            val conversations = RetrofitInstance.chatApi.getConversations()
            val unreadCount = conversations.sumOf { it.unreadCount ?: 0 }
            _messageCount.value = unreadCount
        } catch (e: Exception) {
            // Silently fail - don't disrupt user experience
        }
    }
    
    /**
     * Update all counts
     */
    suspend fun updateAll() {
        updateNotificationCount()
        updateMessageCount()
    }
    
    /**
     * Clear notification count
     */
    fun clearNotificationCount() {
        _notificationCount.value = 0
    }
    
    /**
     * Clear message count
     */
    fun clearMessageCount() {
        _messageCount.value = 0
    }
    
    /**
     * Increment notification count (for real-time updates)
     */
    fun incrementNotificationCount() {
        _notificationCount.value += 1
    }
    
    /**
     * Increment message count (for real-time updates)
     */
    fun incrementMessageCount() {
        _messageCount.value += 1
    }
}

/**
 * ViewModel wrapper for NotificationBadgeManager
 * Makes it easy to use in Composables
 */
class NotificationBadgeViewModel : ViewModel() {
    
    private val badgeManager = NotificationBadgeManager.getInstance()
    
    val notificationCount: StateFlow<Int> = badgeManager.notificationCount
    val messageCount: StateFlow<Int> = badgeManager.messageCount
    
    init {
        // Load counts on initialization
        refresh()
    }
    
    fun refresh() {
        viewModelScope.launch {
            badgeManager.updateAll()
        }
    }
    
    fun updateNotifications() {
        viewModelScope.launch {
            badgeManager.updateNotificationCount()
        }
    }
    
    fun updateMessages() {
        viewModelScope.launch {
            badgeManager.updateMessageCount()
        }
    }
}

