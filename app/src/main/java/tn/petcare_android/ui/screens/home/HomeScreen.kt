package tn.petcare_android.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.PetUtils
import tn.petcare_android.viewmodel.ai.PetAIViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.api.RetrofitInstance
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // AI ViewModel
    val tokenManager = remember { TokenManager(context) }
    val aiViewModel = remember { PetAIViewModel(tokenManager) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            LoadingScreen()
        }
        is ProfileUiState.Success -> {
            // Calendar Manager for AI integration (iOS Reference: HomeView.swift lines 415-435)
            val calendarManager = remember { tn.petcare_android.util.CalendarManager(context) }
            
            // Load AI content for all pets with calendar events
            LaunchedEffect(state.pets) {
                if (state.pets.isNotEmpty()) {
                    // Request calendar permission if needed
                    if (calendarManager.hasCalendarPermission()) {
                        // Load calendar events for each pet before generating AI content
                        state.pets.forEach { pet ->
                            pet.id?.let { petId ->
                                calendarManager.loadEventsForPet(petId)
                            }
                        }
                    }
                    
                    val petIds = state.pets.mapNotNull { it.id }
                    if (petIds.isNotEmpty()) {
                        aiViewModel.generateContentForPets(petIds, silent = true)
                    }
                }
            }
            
            HomeScreenContent(
                navController = navController,
                pets = state.pets,
                userName = state.user.name,
                themePreference = themePreference,
                aiViewModel = aiViewModel
            )
        }
        is ProfileUiState.Error -> {
            ErrorScreen(message = state.message, onRetry = { viewModel.loadProfile() })
        }
        is ProfileUiState.UserDeleted -> {
            // User deleted, should navigate to login
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        else -> {
            // Idle state
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = OrangeAccent)
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    navController: NavHostController,
    pets: List<tn.petcare_android.data.model.pet.Pet>,
    userName: String,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    aiViewModel: PetAIViewModel
) {
    // Notification badge manager
    val badgeViewModel: tn.petcare_android.util.NotificationBadgeViewModel = viewModel()
    val notificationCount by badgeViewModel.notificationCount.collectAsState()
    val messageCount by badgeViewModel.messageCount.collectAsState()
    
    // Refresh badge counts periodically
    LaunchedEffect(Unit) {
        badgeViewModel.refresh()
        // Refresh every 30 seconds
        while (true) {
            kotlinx.coroutines.delay(30000)
            badgeViewModel.refresh()
        }
    }
    
    // Fade in animation when screen loads
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "contentFade"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val petTips by aiViewModel.petTips.collectAsState()
    val petStatuses by aiViewModel.petStatuses.collectAsState()
    val petReminders by aiViewModel.petReminders.collectAsState()
    val isAILoading by aiViewModel.isLoading.collectAsState()
    
    // Auto-refresh timer for AI content (1 hour) - iOS Reference: HomeView.swift lines 365-395
    LaunchedEffect(pets) {
        if (pets.isNotEmpty()) {
            val petIds = pets.mapNotNull { it.id }
            if (petIds.isNotEmpty()) {
                // Initial load
                aiViewModel.generateContentForPets(petIds, silent = true)
                
                // Auto-refresh every 1 hour
                while (true) {
                    kotlinx.coroutines.delay(60 * 60 * 1000L) // 1 hour
                    if (pets.isNotEmpty()) {
                        val currentPetIds = pets.mapNotNull { it.id }
                        if (currentPetIds.isNotEmpty()) {
                            aiViewModel.generateContentForPets(currentPetIds, silent = true)
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = { 
                TopNavBar(
                    title = "Home",
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
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .graphicsLayer { this.alpha = alpha },
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Daily Tips section
            item {
                DailyTipsSection(
                    navController = navController,
                    petTips = petTips,
                    pets = pets,
                    isLoading = isAILoading
                )
            }

            // Featured Content Slideshow
            item {
                FeaturedContentSlideshow(
                    navController = navController
                )
            }

            // Pet Health Snapshot
            item {
                PetHealthSnapshotSection(
                    pets = pets,
                    navController = navController,
                    petStatuses = petStatuses,
                    aiViewModel = aiViewModel
                )
            }

            // Upcoming Reminders
            item {
                UpcomingRemindersSection(
                    petReminders = petReminders,
                    pets = pets,
                    isLoading = isAILoading
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
        }
        
        // Floating AI Chat Button
        FloatingActionButton(
            onClick = { navController.navigate("chat_ai") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp),
            containerColor = OrangeAccent,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "AI Chat",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "AI Chat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// MARK: - Sections

@Composable
private fun DailyTipsSection(
    navController: NavHostController,
    petTips: Map<String, List<tn.petcare_android.data.model.ai.PetTip>>,
    pets: List<tn.petcare_android.data.model.pet.Pet>,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Tips",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            OutlinedButton(
                onClick = { navController.navigate("add_pet") },
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, VetCanyon),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VetCanyon,
                    containerColor = CardBackground
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = VetCanyon
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add a pet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Collect all tips from all pets
        val allTips = petTips.values.flatten()

        when {
            isLoading && allTips.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = VetCanyon, modifier = Modifier.size(32.dp))
                        Text(
                            text = "Generating AI tips...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            allTips.isNotEmpty() -> {
                // Tips carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(allTips.size) { index ->
                        DailyTipCard(tip = allTips[index])
                    }
                }
            }
            pets.isEmpty() -> {
                // No pets - show empty state (iOS Reference: HomeView.swift lines 234-246)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No tips or recommendations",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Add a pet to get personalized tips",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
            else -> {
                // Show mock tips as fallback
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(dailyTipsMock.size) { index ->
                        DailyTipCard(tip = dailyTipsMock[index])
                    }
                }
            }
        }
    }
}

@Composable
private fun PetHealthSnapshotSection(
    pets: List<tn.petcare_android.data.model.pet.Pet>,
    navController: NavHostController,
    petStatuses: Map<String, tn.petcare_android.data.model.ai.PetStatus>,
    aiViewModel: tn.petcare_android.viewmodel.ai.PetAIViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Pet Health Snapshot",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (pets.isEmpty()) {
            Text(
                text = "No pets yet",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pets.forEach { pet ->
                    PetStatusRow(
                        pet = pet,
                        status = petStatuses[pet.id],
                        onClick = { navController.navigate("pet_detail/${pet.id}") }
                    )
                }
                
                // Refresh AI Status button (iOS Reference: HomeView.swift lines 288-308)
                Button(
                    onClick = {
                    // Refresh AI content for all pets with haptic feedback
                    tn.petcare_android.util.HapticFeedbackUtil.hapticTap(context)
                    val petIds = pets.mapNotNull { it.id }
                    if (petIds.isNotEmpty()) {
                        aiViewModel.generateContentForPets(petIds, silent = false)
                    }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VetCanyon.copy(alpha = 0.1f),
                        contentColor = VetCanyon
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = VetCanyon
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Refresh AI Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = VetCanyon
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingRemindersSection(
    petReminders: Map<String, List<tn.petcare_android.data.model.ai.PetReminder>>,
    pets: List<tn.petcare_android.data.model.pet.Pet>,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Upcoming Reminders",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Collect all reminders from all pets
        val allReminders = petReminders.values.flatten()

        when {
            isLoading && allReminders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = VetCanyon, modifier = Modifier.size(32.dp))
                        Text(
                            text = "Generating reminders...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            allReminders.isNotEmpty() -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allReminders.take(5).forEach { reminder ->
                        AIReminderRow(reminder = reminder)
                    }
                }
            }
            pets.isEmpty() -> {
                // No pets - show empty state
                Text(
                    text = "No reminders",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            else -> {
                // No reminders generated - show empty state (iOS Reference: HomeView.swift lines 346-357)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No reminders coming soon",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = "All scheduled care is up to date",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// MARK: - Cards

data class DailyTip(
    val emoji: String,
    val title: String,
    val detail: String
)

@Composable
private fun DailyTipCard(tip: Any) {
    val (emoji, title, detail) = when (tip) {
        is tn.petcare_android.data.model.ai.PetTip -> Triple(tip.emoji, tip.title, tip.detail)
        is DailyTip -> Triple(tip.emoji, tip.title, tip.detail)
        else -> Triple("🐾", "Tip", "")
    }
    
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 36.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = detail,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PetStatusRow(
    pet: tn.petcare_android.data.model.pet.Pet,
    status: tn.petcare_android.data.model.ai.PetStatus?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pet avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                if (pet.photo != null) {
                    Image(
                        painter = rememberAsyncImagePainter(pet.photo),
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = PetUtils.getPetEmoji(pet.species),
                        fontSize = 28.sp
                    )
                }
            }

            // Pet info and status
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = pet.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // AI-generated status badge
                    if (status != null && status.status.isNotBlank()) {
                        AIStatusBadge(status = status.status)
                    }
                    
                    // Status pills
                    if (status != null && status.pills.isNotEmpty()) {
                        status.pills.forEach { pill ->
                            StatusPillChip(pill = pill)
                        }
                    }
                }

                // Status summary or breed
                Text(
                    text = status?.summary ?: (pet.breed ?: pet.species.replaceFirstChar { it.uppercase() }),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatusPillChip(pill: tn.petcare_android.data.model.ai.StatusPill) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(pill.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = pill.text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = pill.textColor
        )
    }
}

@Composable
private fun AIStatusBadge(status: String) {
    // Determine badge color based on status text
    val badgeColor = when {
        status.contains("Medication", ignoreCase = true) -> Color(0xFFFF9500) // Orange for medication
        status.contains("Healthy", ignoreCase = true) -> Color(0xFF34C759) // Green for healthy
        status.contains("Warning", ignoreCase = true) || status.contains("Alert", ignoreCase = true) -> Color(0xFFFF3B30) // Red for warnings
        status.contains("Recovery", ignoreCase = true) -> Color(0xFF007AFF) // Blue for recovery
        else -> VetCanyon // Default to theme color
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(badgeColor)
            )
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}

// AI Reminder Row
@Composable
private fun AIReminderRow(reminder: tn.petcare_android.data.model.ai.PetReminder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(reminder.tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reminder.icon,
                    fontSize = 18.sp
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = reminder.detail,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
                Text(
                    text = reminder.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = reminder.tint
                )
            }
        }
    }
}

// Legacy Reminder Row for mock data
data class ReminderData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val detail: String,
    val date: String,
    val tint: Color
)

@Composable
private fun ReminderRow(reminder: ReminderData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(reminder.tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reminder.icon,
                    contentDescription = null,
                    tint = reminder.tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = reminder.detail,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = reminder.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = reminder.tint
                )
            }
        }
    }
}

// MARK: - Mock Data

private val dailyTipsMock = listOf(
    DailyTip(
        emoji = "🥕",
        title = "Fresh Nutrition",
        detail = "Rotate crunchy vegetables with high-protein treats to keep meals balanced."
    ),
    DailyTip(
        emoji = "🚶‍♀️",
        title = "Stay Active",
        detail = "Short walks twice a day help maintain healthy joints and reduce anxiety."
    ),
    DailyTip(
        emoji = "🪥",
        title = "Dental Care",
        detail = "Brush teeth 2-3 times per week to prevent plaque build-up and gum issues."
    )
)

private val remindersMock = listOf(
    ReminderData(
        icon = Icons.Default.Notifications,
        title = "Luna • Vaccination Booster",
        detail = "Feline FVCRP booster due soon.",
        date = "Dec 24 • 9:00 AM",
        tint = VetCanyon
    ),
    ReminderData(
        icon = Icons.Default.Warning,
        title = "Max • Heartworm Prevention",
        detail = "Monthly chewable due this weekend.",
        date = "Dec 26 • 10:00 AM",
        tint = Color(0xFF3B82F6)
    ),
    ReminderData(
        icon = Icons.Default.Favorite,
        title = "Grooming Session",
        detail = "Schedule grooming and nail trim.",
        date = "Dec 29 • 2:00 PM",
        tint = Color(0xFF9333EA)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPetsTopBar() {
    // Using the new TopNavBar component for consistency
    TopNavBar(
        title = "Home",
        showBackButton = false
    )
}

@Composable
private fun SmallPetCard(
    pet: tn.petcare_android.data.model.pet.Pet, 
    onClick: () -> Unit,
    itemIndex: Int = 0
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "petCardScale"
    )
    
    // Staggered fade-in animation
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "petCardFade_$itemIndex"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 20f,
        animationSpec = tween(durationMillis = 400),
        label = "petCardOffset_$itemIndex"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(itemIndex * 50L)
        isVisible = true
    }

    Card(
        modifier = Modifier
            .width(130.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationY = offsetY
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PetUtils.getPetColor(pet.species).copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                if (!pet.photo.isNullOrBlank()) {
                    // Display photo from Cloudinary
                    Image(
                        painter = rememberAsyncImagePainter(pet.photo),
                        contentDescription = "${pet.name}'s photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display emoji fallback
                    Text(
                        text = PetUtils.getPetEmoji(pet.species),
                        fontSize = 40.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = pet.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Text(
                text = pet.breed ?: pet.species.replaceFirstChar { it.uppercase() },
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}


@Composable
private fun QuickActionsGrid(navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                icon = "🤖",
                label = "Chat AI",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("chat_ai") }
            QuickActionCard(
                icon = "🩺",
                label = "Find Vet",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("clinic") }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                icon = "📅",
                label = "Calendar",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("calendar") }
            QuickActionCard(
                icon = "🧑‍🍼",
                label = "Pet Sitter",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("petsitter") }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                icon = "📖",
                label = "Bookings",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("booking_list") }
            QuickActionCard(
                icon = "➕",
                label = "New Booking",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("booking_create") }
        }
    }
}

@Composable
private fun QuickActionCard(icon: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "actionCardScale"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun FindCareButton(navController: NavHostController) {
    Card(
        onClick = { navController.navigate("find_hub") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = VetCanyon.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Find Care",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Discover trusted vets and pet sitters near you",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(VetCanyon, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Find Care",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

