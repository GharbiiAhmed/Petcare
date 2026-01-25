package tn.petcare_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.ui.navigation.AppNavGraph
import tn.petcare_android.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    
    // Store notification navigation data
    private var notificationNavData by mutableStateOf<NotificationNavData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitInstance.initialize(applicationContext)
        val themePreference = ThemePreference(applicationContext)
        
        // Handle notification intent
        handleNotificationIntent(intent)

        setContent {
            val isDarkMode by themePreference.isDarkMode.collectAsState(initial = false)

            AppTheme(darkTheme = isDarkMode) {
                AppNavGraph(
                    context = applicationContext,
                    themePreference = themePreference,
                    notificationNavData = notificationNavData,
                    onNotificationHandled = { notificationNavData = null }
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }
    
    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            val navigateTo = it.getStringExtra("navigate_to")
            val conversationId = it.getStringExtra("conversation_id")
            val messageId = it.getStringExtra("message_id")
            val bookingId = it.getStringExtra("booking_id")
            
            if (navigateTo != null) {
                notificationNavData = NotificationNavData(
                    destination = navigateTo,
                    conversationId = conversationId,
                    messageId = messageId,
                    bookingId = bookingId
                )
            }
        }
    }
}

data class NotificationNavData(
    val destination: String,
    val conversationId: String? = null,
    val messageId: String? = null,
    val bookingId: String? = null
)
