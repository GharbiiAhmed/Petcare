package tn.petcare_android.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.notification.AppNotification
import tn.petcare_android.data.repository.NotificationRepository

/**
 * ViewModel for notifications
 * iOS Reference: NotificationViewModel.swift lines 1-150
 */
class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {
    
    // Notifications list
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()
    
    // Unread count
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load all notifications
     * iOS Reference: NotificationViewModel.swift line 24
     */
    fun loadNotifications(unreadOnly: Boolean? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val notificationsList = repository.getNotifications(unreadOnly)
                _notifications.value = notificationsList
                
                // Update unread count
                updateUnreadCount()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load notifications"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update unread notification count
     * iOS Reference: NotificationViewModel.swift line 47
     */
    fun updateUnreadCount() {
        viewModelScope.launch {
            try {
                val count = repository.getUnreadCount()
                _unreadCount.value = count
            } catch (e: Exception) {
                // Silently fail, don't update error state for count updates
            }
        }
    }
    
    /**
     * Mark notification as read
     * iOS Reference: NotificationViewModel.swift line 68
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val updatedNotification = repository.markAsRead(notificationId)
                
                // Update local list
                _notifications.value = _notifications.value.map { notification ->
                    if (notification.normalizedId == notificationId) {
                        updatedNotification
                    } else {
                        notification
                    }
                }
                
                // Update unread count
                updateUnreadCount()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to mark as read"
            }
        }
    }
    
    /**
     * Mark all notifications as read
     * iOS Reference: NotificationViewModel.swift line 88
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                repository.markAllAsRead()
                
                // Update local list - mark all as read
                _notifications.value = _notifications.value.map { notification ->
                    notification.copy(read = true)
                }
                
                // Update unread count to 0
                _unreadCount.value = 0
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to mark all as read"
            }
        }
    }
    
    /**
     * Delete notification
     * iOS Reference: NotificationViewModel.swift line 104
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(notificationId)
                
                // Remove from local list
                _notifications.value = _notifications.value.filter { notification ->
                    notification.normalizedId != notificationId
                }
                
                // Update unread count
                updateUnreadCount()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete notification"
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Refresh notifications (pull-to-refresh)
     */
    fun refresh() {
        loadNotifications()
    }
}

