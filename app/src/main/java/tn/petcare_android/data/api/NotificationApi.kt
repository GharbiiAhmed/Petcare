package tn.petcare_android.data.api

import retrofit2.http.*
import tn.petcare_android.data.model.notification.AppNotification
import tn.petcare_android.data.model.notification.NotificationCountResponse

/**
 * Notification API endpoints
 * Matches iOS NotificationService.swift implementation
 */
interface NotificationApi {
    
    /**
     * Get all notifications
     * @param unreadOnly Optional filter to get only unread notifications
     * iOS Reference: NotificationService.swift line 15
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("unreadOnly") unreadOnly: Boolean? = null
    ): List<AppNotification>
    
    /**
     * Get unread notification count
     * iOS Reference: NotificationService.swift line 32
     */
    @GET("notifications/count/unread")
    suspend fun getUnreadCount(): NotificationCountResponse
    
    /**
     * Mark notification as read
     * iOS Reference: NotificationService.swift line 46
     */
    @POST("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String
    ): AppNotification
    
    /**
     * Mark all notifications as read
     * iOS Reference: NotificationService.swift line 59
     */
    @POST("notifications/read-all")
    suspend fun markAllAsRead()
    
    /**
     * Delete notification
     * iOS Reference: NotificationService.swift line 72
     */
    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String
    )
}

