package tn.petcare_android.data.repository

import tn.petcare_android.data.api.NotificationApi
import tn.petcare_android.data.model.notification.AppNotification
import tn.petcare_android.data.model.notification.NotificationCountResponse

/**
 * Repository for notification operations
 * Provides abstraction layer between API and ViewModel
 */
class NotificationRepository(private val api: NotificationApi) {
    
    /**
     * Get all notifications
     * @param unreadOnly Filter to show only unread notifications
     */
    suspend fun getNotifications(unreadOnly: Boolean? = null): List<AppNotification> {
        return try {
            api.getNotifications(unreadOnly)
        } catch (e: Exception) {
            throw Exception("Failed to load notifications: ${e.message}")
        }
    }
    
    /**
     * Get unread notification count
     */
    suspend fun getUnreadCount(): Int {
        return try {
            val response = api.getUnreadCount()
            response.count
        } catch (e: Exception) {
            0 // Return 0 on error
        }
    }
    
    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: String): AppNotification {
        return try {
            api.markAsRead(notificationId)
        } catch (e: Exception) {
            throw Exception("Failed to mark notification as read: ${e.message}")
        }
    }
    
    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead() {
        try {
            api.markAllAsRead()
        } catch (e: Exception) {
            throw Exception("Failed to mark all as read: ${e.message}")
        }
    }
    
    /**
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: String) {
        try {
            api.deleteNotification(notificationId)
        } catch (e: Exception) {
            throw Exception("Failed to delete notification: ${e.message}")
        }
    }
}

