package tn.petcare_android.ui.screens.role

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.components.NavigationDrawerOverlay
import tn.petcare_android.ui.components.StatCard
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.JwtDecoder
import tn.petcare_android.viewmodel.trainer.TrainerMainViewModel
import tn.petcare_android.viewmodel.trainer.TrainerMainViewModelFactory
import tn.petcare_android.viewmodel.trainer.TrainerMainUiState
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState
import tn.petcare_android.ui.screens.role.TrainingPet
import tn.petcare_android.ui.screens.role.TrainingEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerMainScreen(
    navController: androidx.navigation.NavHostController,
    themePreference: ThemePreference,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navControllerLocal = remember { navController }
    val viewModel: TrainerMainViewModel = viewModel(factory = TrainerMainViewModelFactory(context))
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val trainingPets by viewModel.trainingPets.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTrainingPets()
        viewModel.loadCalendarEvents()
        profileViewModel.loadProfile()
    }

    val userName = (profileUiState as? ProfileUiState.Success)?.user?.name
    val userImage = (profileUiState as? ProfileUiState.Success)?.user?.profileImage
    val userRole = (profileUiState as? ProfileUiState.Success)?.user?.role ?: "trainer"

    // Observe drawer state to conditionally render overlay
    val isDrawerOpen by tn.petcare_android.ui.components.DrawerState.isExpanded

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopNavBar(
                title = "Trainer Dashboard",
                navController = navControllerLocal,
                showBackButton = false,
                showMenuButton = true,
                onSettingsClick = {
                    showSettingsMenu = true
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Get current user ID and navigate to trainer profile
                    CoroutineScope(Dispatchers.Main).launch {
                        val token = TokenManager(context).getAccessToken().firstOrNull()
                        val userId = token?.let { JwtDecoder.getUserIdFromToken(it) }
                        userId?.let { navControllerLocal.navigate("trainer_detail/$it") }
                    }
                },
                containerColor = OrangeAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Edit Profile")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Active Trainings",
                    value = trainingPets.filter { it.status == "active" }.size.toString(),
                    icon = Icons.Default.Star,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "This Week",
                    value = calendarEvents.filter { it.isThisWeek }.size.toString(),
                    icon = Icons.Default.DateRange,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Training Pets") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Calendar") }
                )
            }

            // Content
            when (selectedTab) {
                0 -> TrainingPetsTab(
                    pets = trainingPets,
                    onMessageOwner = { pet -> viewModel.navigateToMessage(pet.ownerId) },
                    isLoading = uiState is TrainerMainUiState.Loading
                )
                1 -> CalendarTab(
                    events = calendarEvents,
                    isLoading = uiState is TrainerMainUiState.Loading
                )
            }
        }
        }
        }

        // Navigation Drawer - Only render when open to prevent blocking clicks
        if (isDrawerOpen) {
            NavigationDrawerOverlay(
                navController = navControllerLocal,
                userName = userName,
                userImage = userImage,
                role = userRole
            )
        }

        // Settings Menu - Overlay on top
        if (showSettingsMenu) {
            AlertDialog(
            onDismissRequest = { showSettingsMenu = false },
            title = { Text("Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showSettingsMenu = false
                            onNavigateToChangePassword()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Password")
                    }
                    TextButton(
                        onClick = {
                            showSettingsMenu = false
                            onNavigateToChangeEmail()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Email")
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = {
                            showSettingsMenu = false
                            CoroutineScope(Dispatchers.Main).launch {
                                val token = TokenManager(context).getAccessToken().firstOrNull()
                                val userId = token?.let { JwtDecoder.getUserIdFromToken(it) }
                                userId?.let { navControllerLocal.navigate("trainer_detail/$it") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = {
                            showSettingsMenu = false
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TrainingPetsTab(
    pets: List<TrainingPet>,
    onMessageOwner: (TrainingPet) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (pets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No pets in training", fontSize = 16.sp, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pets) { pet ->
            TrainingPetCard(
                pet = pet,
                onMessageOwner = { onMessageOwner(pet) }
            )
        }
    }
}

@Composable
fun TrainingPetCard(
    pet: TrainingPet,
    onMessageOwner: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pet.petName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Owner: ${pet.ownerName}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = when (pet.status) {
                            "active" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "completed" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            else -> Color.Gray.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = pet.status.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (pet.status) {
                                "active" -> Color(0xFF4CAF50)
                                "completed" -> Color(0xFF2196F3)
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onMessageOwner,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Message Owner")
                }
            }
        }
    }
}

@Composable
fun CalendarTab(
    events: List<TrainingEvent>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No training sessions scheduled", fontSize = 16.sp, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events) { event ->
            TrainingEventCard(event = event)
        }
    }
}

@Composable
fun TrainingEventCard(event: TrainingEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isToday) Color(0xFFFFF3CD) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.petName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Owner: ${event.ownerName}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.dateTime,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                if (event.trainingType.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Type: ${event.trainingType}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


