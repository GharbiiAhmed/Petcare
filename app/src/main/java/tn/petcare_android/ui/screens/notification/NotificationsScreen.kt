package tn.petcare_android.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.notification.AppNotification
import tn.petcare_android.data.model.notification.NotificationType
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.notification.NotificationViewModel
import tn.petcare_android.viewmodel.notification.NotificationViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Notifications Screen
 * iOS Reference: NotificationsView.swift lines 1-372
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference? = null
) {
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(RetrofitInstance.notificationApi)
    )
    
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Load notifications on first composition
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Notifications",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController,
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with "Mark all read" button
            if (unreadCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$unreadCount unread",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    
                    TextButton(
                        onClick = { viewModel.markAllAsRead() }
                    ) {
                        Text(
                            text = "Mark all read",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangeAccent
                        )
                    }
                }
                
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
            }
            
            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && notifications.isEmpty() -> {
                        // Loading state
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = OrangeAccent
                        )
                    }
                    
                    error != null && notifications.isEmpty() -> {
                        // Error state
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Red.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error ?: "Unknown error",
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.loadNotifications() },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    
                    notifications.isEmpty() -> {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "No notifications",
                                modifier = Modifier.size(48.dp),
                                tint = TextSecondary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No notifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You'll see booking requests and updates here",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    else -> {
                        // Notifications list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(notifications, key = { it.normalizedId }) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onClick = {
                                        // Mark as read if unread
                                        if (!notification.read) {
                                            viewModel.markAsRead(notification.normalizedId)
                                        }
                                        
                                        // Navigate based on notification type
                                        when (NotificationType.fromString(notification.type)) {
                                            NotificationType.MESSAGE -> {
                                                // Navigate to conversation if we have messageRefId (conversation ID)
                                                if (!notification.messageRefId.isNullOrEmpty()) {
                                                    navController.navigate("conversation/${notification.messageRefId}")
                                                } else {
                                                    // Fallback to conversations list
                                                    navController.navigate("conversations")
                                                }
                                            }
                                            NotificationType.BOOKING_REQUEST,
                                            NotificationType.BOOKING_ACCEPTED,
                                            NotificationType.BOOKING_REJECTED,
                                            NotificationType.BOOKING_COMPLETED,
                                            NotificationType.BOOKING_CANCELLED -> {
                                                // Navigate to booking detail
                                                notification.normalizedBookingId?.let { bookingId ->
                                                    navController.navigate("booking_detail/$bookingId")
                                                }
                                            }
                                            else -> {
                                                // For other notifications, do nothing or show details
                                            }
                                        }
                                    },
                                    onDelete = {
                                        viewModel.deleteNotification(notification.normalizedId)
                                    }
                                )
                                
                                Divider(
                                    color = Color.LightGray.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (!notification.read) CardBackground.copy(alpha = 0.3f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon based on notification type
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(getIconBackgroundColor(notification.type)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getNotificationIcon(notification.type),
                contentDescription = null,
                tint = getIconColor(notification.type),
                modifier = Modifier.size(22.dp)
            )
        }
        
        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = notification.displayTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                // Unread indicator
                if (!notification.read) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notification.displayMessage,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Time ago
            Text(
                text = formatTimeAgo(notification.normalizedCreatedAt),
                fontSize = 12.sp,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

private fun getNotificationIcon(type: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (NotificationType.fromString(type)) {
        NotificationType.BOOKING_REQUEST -> Icons.Default.DateRange
        NotificationType.BOOKING_ACCEPTED -> Icons.Default.CheckCircle
        NotificationType.BOOKING_REJECTED -> Icons.Default.Close
        NotificationType.BOOKING_COMPLETED -> Icons.Default.CheckCircle
        NotificationType.BOOKING_CANCELLED -> Icons.Default.Close
        NotificationType.MESSAGE -> Icons.Default.Email
        NotificationType.SYSTEM -> Icons.Default.Notifications
        null -> Icons.Default.Notifications
    }
}

private fun getIconColor(type: String?): Color {
    return when (NotificationType.fromString(type)) {
        NotificationType.BOOKING_REQUEST -> OrangeAccent
        NotificationType.BOOKING_ACCEPTED -> Color(0xFF4CAF50)
        NotificationType.BOOKING_REJECTED -> Color(0xFFF44336)
        NotificationType.BOOKING_COMPLETED -> Color(0xFF2196F3)
        NotificationType.BOOKING_CANCELLED -> Color(0xFF9E9E9E)
        NotificationType.MESSAGE -> Color(0xFF2196F3)
        NotificationType.SYSTEM -> Color(0xFF9E9E9E)
        null -> Color(0xFF9E9E9E)
    }
}

private fun getIconBackgroundColor(type: String?): Color {
    return when (NotificationType.fromString(type)) {
        NotificationType.BOOKING_REQUEST -> OrangeAccent.copy(alpha = 0.1f)
        NotificationType.BOOKING_ACCEPTED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        NotificationType.BOOKING_REJECTED -> Color(0xFFF44336).copy(alpha = 0.1f)
        NotificationType.BOOKING_COMPLETED -> Color(0xFF2196F3).copy(alpha = 0.1f)
        NotificationType.BOOKING_CANCELLED -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
        NotificationType.MESSAGE -> Color(0xFF2196F3).copy(alpha = 0.1f)
        NotificationType.SYSTEM -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
        null -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
    }
}

private fun formatTimeAgo(dateString: String?): String {
    if (dateString == null) return ""
    
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val date = formatter.parse(dateString) ?: return ""
        
        val now = Date()
        val diff = now.time - date.time
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.US).format(date)
        }
    } catch (e: Exception) {
        ""
    }
}

