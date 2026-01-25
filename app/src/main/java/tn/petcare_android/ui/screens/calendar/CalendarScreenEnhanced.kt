package tn.petcare_android.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.CalendarEvent
import tn.petcare_android.util.CalendarManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced CalendarScreen with Real Device Calendar Integration
 * iOS Reference: CalendarManager.swift with EventKit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenEnhanced(navController: NavHostController) {
    val context = LocalContext.current
    val calendarManager = remember { CalendarManager(context) }
    
    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission) {
            loadCalendarEvents(calendarManager) { events = it; isLoading = false }
        }
    }
    
    // Check permissions on launch
    LaunchedEffect(Unit) {
        hasPermission = calendarManager.hasCalendarPermission()
        if (hasPermission) {
            isLoading = true
            loadCalendarEvents(calendarManager) { events = it; isLoading = false }
        }
    }

    Scaffold(
        topBar = { CalendarTopBar(navController = navController) },
        containerColor = PageBackground,
        floatingActionButton = {
            if (hasPermission) {
                FloatingActionButton(
                    onClick = { navController.navigate("add_calendar_event") },
                    containerColor = VetCanyon,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add Event")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !hasPermission -> {
                    // Permission request UI
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            "Calendar",
                            modifier = Modifier.size(80.dp),
                            tint = TextSecondary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Calendar Permission Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Allow access to sync your bookings with your device calendar.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CALENDAR,
                                        Manifest.permission.WRITE_CALENDAR
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
                
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OrangeAccent
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Upcoming Events",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                TextButton(
                                    onClick = {
                                        isLoading = true
                                        loadCalendarEvents(calendarManager) { events = it; isLoading = false }
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, "Refresh", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Refresh")
                                }
                            }
                        }

                        if (events.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            "No events",
                                            modifier = Modifier.size(48.dp),
                                            tint = TextSecondary.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "No upcoming bookings",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Your accepted bookings will appear here automatically",
                                            fontSize = 14.sp,
                                            color = TextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(events) { event ->
                                EventCardEnhanced(event) {
                                    // Navigate to booking detail if bookingId exists
                                    event.bookingId?.let { bookingId ->
                                        navController.navigate("booking_detail/$bookingId")
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = OrangeAccent.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        "Info",
                                        tint = OrangeAccent
                                    )
                                    Text(
                                        text = "Accepted bookings are automatically synced to your device calendar with reminders.",
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                "Calendar",
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PageBackground
        )
    )
}

@Composable
private fun EventCardEnhanced(event: CalendarEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OrangeAccent)
            )
            
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Info,
                    "Event",
                    tint = OrangeAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = formatEventTime(event.startTime),
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (event.location.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            "Location",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = event.location,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Arrow
            Icon(
                Icons.Default.KeyboardArrowRight,
                "View",
                tint = TextSecondary
            )
        }
    }
}

private fun loadCalendarEvents(
    calendarManager: CalendarManager,
    onEventsLoaded: (List<CalendarEvent>) -> Unit
) {
    try {
        val events = calendarManager.getPetcareCalendarEvents()
        onEventsLoaded(events)
    } catch (e: Exception) {
        android.util.Log.e("CalendarScreen", "Failed to load events: ${e.message}")
        onEventsLoaded(emptyList())
    }
}

private fun formatEventTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US)
    return formatter.format(Date(timestamp))
}

