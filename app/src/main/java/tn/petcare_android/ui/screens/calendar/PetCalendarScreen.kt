package tn.petcare_android.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.CalendarEvent
import tn.petcare_android.util.CalendarManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pet-specific Calendar Screen
 * Shows calendar view and Quick Add buttons for: Reminder, Appointment, Vaccination, Medication
 * iOS Reference: PetCalendarView.swift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCalendarScreen(
    navController: NavHostController,
    petId: String?
) {
    val context = LocalContext.current
    val calendarManager = remember { CalendarManager(context) }
    val profileViewModel: tn.petcare_android.viewmodel.profile.ProfileViewModel = viewModel(
        factory = tn.petcare_android.viewmodel.profile.ProfileViewModelFactory(context)
    )
    
    var pet by remember { mutableStateOf<Pet?>(null) }
    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    
    // Load pet data from profile
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    
    val profileState by profileViewModel.uiState.collectAsState()
    LaunchedEffect(petId, profileState) {
        if (petId != null && profileState is tn.petcare_android.viewmodel.profile.ProfileUiState.Success) {
            val pets = (profileState as tn.petcare_android.viewmodel.profile.ProfileUiState.Success).pets
            pet = pets.find { it.id == petId }
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission && petId != null) {
            loadPetCalendarEvents(calendarManager, petId) { events = it; isLoading = false }
        }
    }
    
    // Reload trigger - increments when we want to force a reload
    // This will trigger a reload when returning from add event screen
    var reloadTrigger by remember { mutableStateOf(0) }
    
    // Check permissions on launch and reload events when petId changes or reload is triggered
    LaunchedEffect(petId, reloadTrigger) {
        hasPermission = calendarManager.hasCalendarPermission()
        if (hasPermission && petId != null) {
            isLoading = true
            loadPetCalendarEvents(calendarManager, petId) { events = it; isLoading = false }
        }
    }
    
    // Reload events when returning from add event screen
    // Check savedStateHandle for a flag that indicates an event was added
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle?.get<Boolean>("eventAdded")) {
        val eventAdded = savedStateHandle?.get<Boolean>("eventAdded")
        if (eventAdded == true && petId != null) {
            // Clear the flag and reload events
            savedStateHandle?.set("eventAdded", false)
            reloadTrigger++
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "${pet?.name ?: "Pet"}'s Calendar",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = PageBackground,
        floatingActionButton = {
            if (hasPermission) {
                FloatingActionButton(
                    onClick = { 
                        navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                        navController.navigate("add_calendar_event")
                    },
                    containerColor = VetCanyon,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add Event")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Calendar Widget Section
            item {
                CalendarWidgetSection()
            }
            
            // Quick Add Section
            item {
                QuickAddSection(
                    petId = petId,
                    navController = navController
                )
            }
            
            // Upcoming Events Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPCOMING EVENTS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            
            when {
                !hasPermission -> {
                    item {
                        PermissionRequestCard(
                            onRequestPermission = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CALENDAR,
                                        Manifest.permission.WRITE_CALENDAR
                                    )
                                )
                            }
                        )
                    }
                }
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VetCanyon)
                        }
                    }
                }
                events.isEmpty() -> {
                    item {
                        EmptyEventsCard()
                    }
                }
                else -> {
                    items(events) { event ->
                        EventCardEnhanced(event) {
                            // Navigate to event details or edit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarWidgetSection() {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, "Previous Month")
                }
                
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.US).format(
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentYear)
                            set(Calendar.MONTH, currentMonth)
                        }.time
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                IconButton(onClick = { 
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, "Next Month")
                }
            }
            
            // Calendar Grid (simplified - showing current date)
            Text(
                text = "Calendar view - ${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)}",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuickAddSection(
    petId: String?,
    navController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "QUICK ADD",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAddButton(
                title = "Medication",
                icon = "💊",
                color = Color(0xFFFF9500),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "MEDICATION")
                navController.navigate("add_calendar_event")
            }
            
            QuickAddButton(
                title = "Vaccination",
                icon = "💉",
                color = Color(0xFF34C759),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "VACCINATION")
                navController.navigate("add_calendar_event")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAddButton(
                title = "Appointment",
                icon = "🏥",
                color = Color(0xFF007AFF),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "APPOINTMENT")
                navController.navigate("add_calendar_event")
            }
            
            QuickAddButton(
                title = "Reminder",
                icon = "🔔",
                color = Color(0xFFAF52DE),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "REMINDER")
                navController.navigate("add_calendar_event")
            }
        }
    }
}

@Composable
private fun QuickAddButton(
    title: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun PermissionRequestCard(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = VetCanyon
            )
            Text(
                text = "Calendar Permission Required",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Allow access to sync your pet's events with your device calendar.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun EmptyEventsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "No events",
                modifier = Modifier.size(48.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            Text(
                text = "No upcoming events",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EventCardEnhanced(event: CalendarEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VetCanyon.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = VetCanyon,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = formatEventTime(event.startTime),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun loadPetCalendarEvents(
    calendarManager: CalendarManager,
    petId: String,
    onEventsLoaded: (List<CalendarEvent>) -> Unit
) {
    try {
        val events = calendarManager.loadEventsForPet(petId)
        onEventsLoaded(events)
    } catch (e: Exception) {
        android.util.Log.e("PetCalendarScreen", "Failed to load events: ${e.message}")
        onEventsLoaded(emptyList())
    }
}

private fun formatEventTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US)
    return formatter.format(Date(timestamp))
}

