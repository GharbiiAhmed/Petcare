package tn.petcare_android.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.petcare_android.data.model.booking.Booking
import tn.petcare_android.data.model.booking.UpdateBookingRequest
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.JwtDecoder
import tn.petcare_android.viewmodel.booking.BookingViewModel
import tn.petcare_android.viewmodel.booking.BookingViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.firstOrNull

/**
 * Enhanced Booking Detail Screen
 * iOS Reference: NotificationsView.swift BookingDetailView (lines 182-372)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: Booking?,
    onBack: () -> Unit,
    viewModel: BookingViewModel? = null
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    
    // Get current user ID from token
    LaunchedEffect(Unit) {
        val token = tokenManager.getAccessToken().firstOrNull()
        if (token != null) {
            currentUserId = JwtDecoder.getUserIdFromToken(token)
        }
    }
    
    // Dialogs
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var rejectReason by remember { mutableStateOf("") }
    
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Booking") },
            text = {
                Column {
                    Text("Please provide a reason for cancellation:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (booking != null && viewModel != null) {
                            viewModel.updateBooking(
                                booking.normalizedId,
                                UpdateBookingRequest(
                                    status = "cancelled",
                                    cancellationReason = cancelReason
                                )
                            )
                        }
                        showCancelDialog = false
                        onBack()
                    }
                ) {
                    Text("Cancel Booking", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Booking")
                }
            }
        )
    }
    
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Booking") },
            text = {
                Column {
                    Text("Please provide a reason for rejection:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (booking != null && viewModel != null) {
                            viewModel.updateBooking(
                                booking.normalizedId,
                                UpdateBookingRequest(
                                    status = "rejected",
                                    rejectionReason = rejectReason
                                )
                            )
                        }
                        showRejectDialog = false
                        onBack()
                    }
                ) {
                    Text("Reject", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        if (booking == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No booking",
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Booking not found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = getStatusColor(booking.displayStatus)
                    ) {
                        Text(
                            text = booking.displayStatus.uppercase(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                // Service Type Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(OrangeAccent.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (booking.providerType) {
                                    "vet" -> Icons.Default.Star
                                    "salon" -> Icons.Default.Favorite
                                    else -> Icons.Default.Favorite
                                },
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = booking.serviceType ?: "Service",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = when (booking.providerType) {
                                    "vet" -> "Veterinary Appointment"
                                    "salon" -> "Grooming Service"
                                    else -> "Pet Care Service"
                                },
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // Provider Info
                InfoCard(
                    title = if (currentUserId == booking.normalizedProviderId) "Client" else "Provider",
                    items = listOf(
                        InfoItem(
                            icon = Icons.Default.Person,
                            label = "Name",
                            value = if (currentUserId == booking.normalizedProviderId) 
                                booking.owner?.name ?: "Unknown"
                            else 
                                booking.provider?.name ?: "Unknown"
                        ),
                        InfoItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = if (currentUserId == booking.normalizedProviderId)
                                booking.owner?.email ?: "N/A"
                            else
                                booking.provider?.email ?: "N/A"
                        )
                    )
                )
                
                // Pet Info
                if (booking.pet != null) {
                    InfoCard(
                        title = "Pet Information",
                        items = listOf(
                            InfoItem(
                                icon = Icons.Default.Favorite,
                                label = "Name",
                                value = booking.pet.name ?: "Unknown"
                            ),
                            InfoItem(
                                icon = Icons.Default.Info,
                                label = "Species & Breed",
                                value = "${booking.pet.species ?: "Unknown"} • ${booking.pet.breed ?: "Unknown"}"
                            )
                        )
                    )
                }
                
                // Date & Time
                InfoCard(
                    title = "Date & Time",
                    items = listOf(
                        InfoItem(
                            icon = Icons.Default.DateRange,
                            label = "Date",
                            value = formatDate(booking.dateTime)
                        ),
                        InfoItem(
                            icon = Icons.Default.Info,
                            label = "Time",
                            value = formatTime(booking.dateTime)
                        ),
                        InfoItem(
                            icon = Icons.Default.Info,
                            label = "Duration",
                            value = "${booking.duration ?: "N/A"} minutes"
                        )
                    )
                )
                
                // Price
                if (booking.price != null) {
                    InfoCard(
                        title = "Payment",
                        items = listOf(
                            InfoItem(
                                icon = Icons.Default.Info,
                                label = "Price",
                                value = "$${booking.price}"
                            )
                        )
                    )
                }
                
                // Description
                if (!booking.description.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Notes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = booking.description,
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // Rejection/Cancellation Reason
                if (!booking.rejectionReason.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Rejection Reason",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                text = booking.rejectionReason,
                                fontSize = 14.sp,
                                color = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                if (!booking.cancellationReason.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Cancellation Reason",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = booking.cancellationReason,
                                fontSize = 14.sp,
                                color = Color.Gray.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Action Buttons
                if (viewModel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Provider actions
                    if (currentUserId == booking.normalizedProviderId && booking.displayStatus == "pending") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateBooking(
                                        booking.normalizedId,
                                        UpdateBookingRequest(status = "accepted")
                                    )
                                    onBack()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.Check, "Accept")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Accept")
                            }
                            
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.Close, "Reject")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reject")
                            }
                        }
                    }
                    
                    // Provider mark as complete
                    if (currentUserId == booking.normalizedProviderId && booking.displayStatus == "accepted") {
                        Button(
                            onClick = {
                                viewModel.updateBooking(
                                    booking.normalizedId,
                                    UpdateBookingRequest(status = "completed")
                                )
                                onBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Icon(Icons.Default.CheckCircle, "Complete")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as Complete")
                        }
                    }
                    
                    // Owner cancel
                    if (currentUserId == booking.normalizedOwnerId && 
                        (booking.displayStatus == "pending" || booking.displayStatus == "accepted")) {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Close, "Cancel")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Booking")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, items: List<InfoItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = item.value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

private data class InfoItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val value: String
)

private fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "pending" -> Color(0xFFFF9800)
        "accepted" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        "completed" -> Color(0xFF2196F3)
        "cancelled" -> Color(0xFF9E9E9E)
        else -> Color.Gray
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return "N/A")
    } catch (e: Exception) {
        "N/A"
    }
}

private fun formatTime(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return "N/A")
    } catch (e: Exception) {
        "N/A"
    }
}
