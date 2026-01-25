package tn.petcare_android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tn.petcare_android.MainActivity
import tn.petcare_android.R
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.storage.TokenManager

/**
 * Firebase Cloud Messaging Service
 * iOS Reference: FCMManager.swift lines 1-229
 */
class FCMService : FirebaseMessagingService() {
    
    companion object {
        private const val CHANNEL_ID = "petcare_notifications"
        private const val CHANNEL_NAME = "Petcare Notifications"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    /**
     * Called when new FCM token is generated
     * iOS Reference: FCMManager.swift line 217
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Send token to backend
        sendTokenToBackend(token)
    }
    
    /**
     * Called when message is received
     * iOS Reference: FCMManager.swift line 152
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Extract notification data
        val data = remoteMessage.data
        val type = data["type"] ?: "system"
        val title = remoteMessage.notification?.title ?: data["title"] ?: "New Notification"
        val message = remoteMessage.notification?.body ?: data["message"] ?: ""
        val conversationId = data["conversationId"]
        val bookingId = data["bookingId"]
        val messageId = data["messageId"]
        
        // If this is a message notification, trigger refresh for open conversation
        // iOS Reference: ChatViewModel.swift lines 487-504 (FCM notification handler)
        if (type == "message" && conversationId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                MessageRefreshManager.onMessageReceived(conversationId)
            }
        }
        
        // Show notification
        showNotification(title, message, type, conversationId, bookingId, messageId)
    }
    
    /**
     * Send FCM token to backend
     * iOS Reference: FCMManager.swift line 113
     */
    private fun sendTokenToBackend(token: String) {
        val tokenManager = TokenManager(applicationContext)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get access token
                val accessToken = tokenManager.getAccessToken().collect { accessToken ->
                    if (!accessToken.isNullOrEmpty()) {
                        try {
                            // Send to backend (you'll need to create this endpoint in UserApi)
                            // POST /users/fcm-token { "fcmToken": token }
                            // For now, just log
                            println("✅ FCM token ready to send: $token")
                        } catch (e: Exception) {
                            println("❌ Failed to send FCM token: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ Error getting access token: ${e.message}")
            }
        }
    }
    
    /**
     * Create notification channel (Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Petcare app notifications for bookings and messages"
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show notification
     * iOS Reference: FCMManager.swift line 189
     */
    private fun showNotification(
        title: String,
        message: String,
        type: String,
        conversationId: String?,
        bookingId: String?,
        messageId: String?
    ) {
        // Create intent based on notification type
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            when (type) {
                "message" -> {
                    conversationId?.let { putExtra("conversation_id", it) }
                    messageId?.let { putExtra("message_id", it) }
                    putExtra("navigate_to", "chat")
                }
                "booking_request", "booking_accepted", "booking_rejected", 
                "booking_completed", "booking_cancelled" -> {
                    bookingId?.let { putExtra("booking_id", it) }
                    putExtra("navigate_to", "booking_detail")
                }
                else -> {
                    putExtra("navigate_to", "notifications")
                }
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Update with your app icon
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        
        // Show notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

