package tn.petcare_android.ui.screens.mypets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.ui.components.*
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.CalendarEvent
import tn.petcare_android.util.CalendarManager
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPetsScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Tab refresh notification (iOS Reference: MainTabView.swift lines 37-45)
    // Refresh when switching to MyPets tab
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    
    // Also refresh when screen becomes visible (tab switch)
    DisposableEffect(Unit) {
        viewModel.loadProfile()
        onDispose { }
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            LoadingScreen()
        }
        is ProfileUiState.Success -> {
            MyPetsContent(
                navController = navController,
                pets = state.pets,
                themePreference = themePreference,
                onRefresh = { viewModel.loadProfile() }
            )
        }
        is ProfileUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadProfile() }
            )
        }
        else -> {

        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = VetCanyon)
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                color = TextPrimary
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPetsContent(
    navController: NavHostController,
    pets: List<tn.petcare_android.data.model.pet.Pet>,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    onRefresh: () -> Unit
) {
    // Notification badge manager
    val badgeViewModel: tn.petcare_android.util.NotificationBadgeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
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
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "My Pets",
                navController = navController,
                showBackButton = false,
                showMenuButton = true,
                onMessagesClick = { navController.navigate("conversations") },
                onNotificationsClick = { navController.navigate("notifications") },
                messageCount = messageCount,
                notificationCount = notificationCount
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            if (pets.isNotEmpty()) {
                item {
                    CalendarSection(
                        navController = navController,
                        pets = pets,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }


            if (pets.isEmpty()) {
                item {
                    EmptyPetsView(
                        onAddPetClick = { navController.navigate("add_pet") }
                    )
                }
            } else {

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Pets",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        TextButton(
                            onClick = { navController.navigate("add_pet") },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = VetCanyon,
                                containerColor = VetCanyon.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add pet",
                                modifier = Modifier.size(16.dp),
                                tint = VetCanyon
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Pet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }


                items(pets) { pet ->
                    PetRow(
                        name = pet.name,
                        breed = pet.breed ?: pet.species.replaceFirstChar { it.uppercase() },
                        age = pet.age?.let { formatAge(it) } ?: "Unknown age",
                        color = VetCanyon,
                        photoUrl = pet.photo,
                        species = pet.species,
                        modifier = Modifier.padding(horizontal = 18.dp),
                        onClick = { navController.navigate("pet_detail/${pet.id}") }
                    )
                }
            }


            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun CalendarSection(
    navController: NavHostController,
    pets: List<tn.petcare_android.data.model.pet.Pet>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendarManager = remember { CalendarManager(context) }
    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Reload trigger to refresh events when screen becomes visible
    var reloadTrigger by remember { mutableStateOf(0) }
    
    // Reload when screen becomes visible (after navigation back from calendar)
    LaunchedEffect(Unit) {
        // Small delay to ensure calendar is ready
        kotlinx.coroutines.delay(200)
        reloadTrigger++
    }
    
    // Load events for all pets coming this week
    LaunchedEffect(pets.map { it.id }.joinToString(), reloadTrigger) {
        if (calendarManager.hasCalendarPermission() && pets.isNotEmpty()) {
            isLoading = true
            try {
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = now
                
                // Set to start of week (Monday)
                // First, get the current day of week (1=Sunday, 2=Monday, etc.)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val weekStart = calendar.timeInMillis
                
                // Set to end of week (Sunday 23:59:59)
                calendar.add(Calendar.DAY_OF_MONTH, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val weekEnd = calendar.timeInMillis
                
                android.util.Log.d("MyPetsScreen", "Week range: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(weekStart))} to ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(weekEnd))}")
                
                // Load events for all pets
                val allEvents = mutableListOf<CalendarEvent>()
                pets.forEach { pet ->
                    pet.id?.let { petId ->
                        val petEvents = calendarManager.loadEventsForPet(petId)
                        android.util.Log.d("MyPetsScreen", "Loaded ${petEvents.size} events for pet $petId")
                        allEvents.addAll(petEvents)
                    }
                }
                
                android.util.Log.d("MyPetsScreen", "Total events loaded: ${allEvents.size}")
                
                // Filter to this week only
                val weekEvents = allEvents.filter { 
                    it.startTime >= weekStart && it.startTime <= weekEnd 
                }
                
                android.util.Log.d("MyPetsScreen", "Events this week: ${weekEvents.size}")
                weekEvents.forEach { event ->
                    android.util.Log.d("MyPetsScreen", "Event: ${event.title} at ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(event.startTime))}")
                }
                
                events = weekEvents.sortedBy { it.startTime }
            } catch (e: Exception) {
                android.util.Log.e("MyPetsScreen", "Failed to load events: ${e.message}", e)
                events = emptyList()
            }
            isLoading = false
        } else {
            android.util.Log.d("MyPetsScreen", "No calendar permission or no pets. Permission: ${calendarManager.hasCalendarPermission()}, Pets: ${pets.size}")
        }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "THIS WEEK",
            actionLabel = "View All",
            onActionClick = { navController.navigate("calendar") }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = VetCanyon,
                            strokeWidth = 2.dp
                        )
                    }
                }
                events.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Calendar",
                            modifier = Modifier.size(24.dp),
                            tint = TextSecondary
                        )
                        Text(
                            text = "No events this week",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        events.take(3).forEach { event ->
                            EventItemCompact(event, pets, navController)
                        }
                        if (events.size > 3) {
                            TextButton(
                                onClick = { navController.navigate("calendar") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "View ${events.size - 3} more",
                                    fontSize = 12.sp,
                                    color = VetCanyon
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
private fun EventItemCompact(
    event: CalendarEvent,
    pets: List<tn.petcare_android.data.model.pet.Pet>,
    navController: NavHostController
) {
    // Extract pet ID from event description
    val petId = extractPetIdFromEvent(event)
    val pet = pets.find { it.id == petId }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                petId?.let { navController.navigate("calendar/$it") }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VetCanyon.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = VetCanyon,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = event.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatEventTimeCompact(event.startTime),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                if (pet != null) {
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = pet.name,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

private fun extractPetIdFromEvent(event: CalendarEvent): String? {
    val lines = event.description.split("\n")
    for (line in lines) {
        if (line.startsWith("Pet ID: ")) {
            return line.substringAfter("Pet ID: ").trim()
        }
    }
    return null
}

private fun formatEventTimeCompact(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val dayOfWeek = SimpleDateFormat("EEE", Locale.US).format(Date(timestamp))
    val time = SimpleDateFormat("hh:mm a", Locale.US).format(Date(timestamp))
    return "$dayOfWeek, $time"
}

private fun formatAge(age: Double?): String {
    return when {
        age == null -> "Unknown age"
        age < 1.0 -> {
            val months = (age * 12).toInt()
            when (months) {
                0 -> "Less than 1 month"
                1 -> "1 month old"
                else -> "$months months old"
            }
        }
        age == 1.0 -> "1 year old"
        age < 2.0 -> "${String.format("%.1f", age)} years old"
        else -> "${age.toInt()} years old"
    }
}
