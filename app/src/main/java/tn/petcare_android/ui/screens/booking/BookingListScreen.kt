package tn.petcare_android.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
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
import tn.petcare_android.data.model.booking.Booking
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.booking.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Booking List Screen
 * iOS Reference: NotificationsView.swift (booking management)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    viewModel: BookingViewModel,
    onBookingSelected: (Booking) -> Unit,
    navController: NavHostController? = null,
    role: String? = null
) {
    val bookings by viewModel.bookings.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Notification badge manager
    val badgeViewModel: tn.petcare_android.util.NotificationBadgeViewModel = viewModel()
    val notificationCount by badgeViewModel.notificationCount.collectAsState()
    val messageCount by badgeViewModel.messageCount.collectAsState()
    
    // Refresh badge counts periodically
    LaunchedEffect(Unit) {
        badgeViewModel.refresh()
        while (true) {
            kotlinx.coroutines.delay(30000)
            badgeViewModel.refresh()
        }
    }

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("As Owner", "As Provider")

    // Filter state
    var selectedStatus by remember { mutableStateOf("All") }
    var showFilterDropdown by remember { mutableStateOf(false) }
    val statusOptions = listOf("All", "Pending", "Accepted", "Completed", "Cancelled", "Rejected")

    // Load bookings based on selected tab
    LaunchedEffect(selectedTab) {
        val filterRole = if (selectedTab == 0) "owner" else "provider"
        viewModel.fetchBookings(role = filterRole)
    }

    // Apply client-side filtering
    val filteredBookings = remember(bookings, selectedStatus) {
        if (selectedStatus == "All") {
            bookings
        } else {
            bookings.filter { 
                it.displayStatus.equals(selectedStatus, ignoreCase = true) 
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopNavBar(
                    title = "My Bookings",
                    navController = navController,
                    showBackButton = false,
                    showMenuButton = true,
                    onMessagesClick = { navController?.navigate("conversations") },
                    onNotificationsClick = { navController?.navigate("notifications") },
                    messageCount = messageCount,
                    notificationCount = notificationCount
                )
                
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardBackground,
                    contentColor = OrangeAccent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
                
                // Filter bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredBookings.size} booking${if (filteredBookings.size != 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    
                    Box {
                        OutlinedButton(
                            onClick = { showFilterDropdown = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OrangeAccent
                            )
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Filter",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedStatus, fontSize = 14.sp)
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
            }
                        
                        DropdownMenu(
                            expanded = showFilterDropdown,
                            onDismissRequest = { showFilterDropdown = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        selectedStatus = status
                                        showFilterDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
            }
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                loading && bookings.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OrangeAccent
                    )
                }
                
                error != null && bookings.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red.copy(alpha = 0.5f)
                        )
                        Text(
                            text = error ?: "Failed to load bookings",
                            fontSize = 16.sp,
                            color = TextPrimary
            )
                        Button(
                            onClick = {
                                val filterRole = if (selectedTab == 0) "owner" else "provider"
                                viewModel.fetchBookings(role = filterRole)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("Retry")
            }
        }
                }
                
                filteredBookings.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No bookings",
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No bookings found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (selectedTab == 0) 
                                "Your booking requests will appear here" 
                            else 
                                "Incoming booking requests will appear here",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredBookings, key = { it.normalizedId }) { booking ->
                            BookingCard(
                                booking = booking,
                                isOwner = selectedTab == 0,
                                onClick = { onBookingSelected(booking) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    isOwner: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with service type and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = booking.serviceType ?: "Service",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isOwner) {
                                booking.provider?.name ?: "Provider"
                            } else {
                                booking.owner?.name ?: "Client"
                            },
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Status badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getStatusColor(booking.displayStatus)
                ) {
                    Text(
                        text = booking.displayStatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            
            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatDate(booking.dateTime),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                
                // Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatTime(booking.dateTime),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Pet info
            if (booking.pet != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${booking.pet.name} • ${booking.pet.species}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Price
            if (booking.price != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$${booking.price}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                }
            }
        }
    }
}

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
        val outputFormat = SimpleDateFormat("MMM dd", Locale.US)
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
