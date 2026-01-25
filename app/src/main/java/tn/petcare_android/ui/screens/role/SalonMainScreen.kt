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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.util.JwtDecoder
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.components.NavigationDrawerOverlay
import tn.petcare_android.ui.components.StatCard
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonMainScreen(
    navController: androidx.navigation.NavHostController,
    themePreference: ThemePreference,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navControllerLocal = remember { navController }
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val profileUiState by profileViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    
    // Salon profile data (loaded separately from API)
    var salonProfile by remember { mutableStateOf<tn.petcare_android.data.model.auth.AppUser?>(null) }
    var isLoadingSalonProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        // Load salon profile data
        isLoadingSalonProfile = true
        coroutineScope.launch {
            try {
                val tokenManager = TokenManager(context)
                val token = tokenManager.getAccessToken().firstOrNull()
                if (!token.isNullOrEmpty()) {
                    val userId = JwtDecoder.getUserIdFromToken(token)
                    if (!userId.isNullOrEmpty()) {
                        val response = RetrofitInstance.salonApi.getSalon(userId)
                        if (response.isSuccessful) {
                            salonProfile = response.body()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SalonMainScreen", "Error loading salon profile: ${e.message}", e)
            } finally {
                isLoadingSalonProfile = false
            }
        }
    }

    val userName = (profileUiState as? ProfileUiState.Success)?.user?.name
    val userImage = (profileUiState as? ProfileUiState.Success)?.user?.profileImage
    val userRole = (profileUiState as? ProfileUiState.Success)?.user?.role ?: "salon"
    val salonServices: List<String> = salonProfile?.salonServices ?: emptyList()
    val salonPricing: Map<String, Double> = salonProfile?.salonPricing ?: emptyMap()

    // Observe drawer state to conditionally render overlay
    val isDrawerOpen by tn.petcare_android.ui.components.DrawerState.isExpanded

    // Callback to handle sidebar menu item clicks for tab switching
    val onSidebarItemClick: (String) -> Unit = { menuItem ->
        when (menuItem) {
            "Dashboard" -> selectedTab = 0
            "Appointments" -> selectedTab = 0
            "Services" -> selectedTab = 1
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopNavBar(
                    title = "Pet Salon Dashboard",
                    navController = navControllerLocal,
                    showBackButton = false,
                    showMenuButton = true,
                    onSettingsClick = {
                        showSettingsMenu = true
                    }
                )
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
                        title = "Today's Appointments",
                        value = "0", // TODO: Load from bookings
                        icon = Icons.Default.DateRange,
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Bookings",
                        value = "0", // TODO: Load from bookings
                        icon = Icons.Default.Favorite,
                        color = Color(0xFF7B1FA2),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Appointments") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Services") }
                    )
                }

                // Content
                when (selectedTab) {
                    0 -> {
                        // Appointments Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "Your Appointments",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            item {
                                Text(
                                    text = "No appointments yet. Bookings will appear here.",
                                    color = TextSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    1 -> {
                        // Services Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Services",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    TextButton(
                                        onClick = { navController.navigate("salon_profile") }
                                    ) {
                                        Text("Edit Profile", fontSize = 14.sp)
                                    }
                                }
                            }
                            if (salonServices.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Text(
                                                text = "No services added yet",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Edit your profile to add services",
                                                fontSize = 14.sp,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { navController.navigate("salon_profile") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF9C27B0)
                                                )
                                            ) {
                                                Text("Add Services")
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(salonServices) { service: String ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (service.isNotEmpty()) {
                                                        service[0].toString().uppercase() + service.substring(1).lowercase()
                                                    } else {
                                                        service
                                                    },
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                if (salonPricing.containsKey(service)) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "$${String.format("%.2f", salonPricing[service])}",
                                                        fontSize = 14.sp,
                                                        color = AccentSoftGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = AccentSoftGreen,
                                                modifier = Modifier.size(24.dp)
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

        // Navigation Drawer
        if (isDrawerOpen) {
            NavigationDrawerOverlay(
                navController = navControllerLocal,
                userName = userName,
                userImage = userImage,
                role = userRole,
                onMenuItemClick = onSidebarItemClick
            )
        }

        // Settings Menu
        if (showSettingsMenu) {
            AlertDialog(
                onDismissRequest = { showSettingsMenu = false },
                title = { Text("Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            showSettingsMenu = false
                            navController.navigate("salon_profile")
                        }) {
                            Text("Profile")
                        }
                        TextButton(onClick = {
                            showSettingsMenu = false
                            onNavigateToChangePassword()
                        }) {
                            Text("Change Password")
                        }
                        TextButton(onClick = {
                            showSettingsMenu = false
                            onNavigateToChangeEmail()
                        }) {
                            Text("Change Email")
                        }
                        HorizontalDivider()
                        TextButton(onClick = {
                            showSettingsMenu = false
                            onLogout()
                        }) {
                            Text("Logout", color = Color.Red)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSettingsMenu = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

