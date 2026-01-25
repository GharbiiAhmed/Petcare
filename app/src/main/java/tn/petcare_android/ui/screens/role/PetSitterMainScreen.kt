package tn.petcare_android.ui.screens.role

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.components.NavigationDrawerOverlay
import tn.petcare_android.ui.components.StatCard
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.sitter.PetSitterMainViewModel
import tn.petcare_android.viewmodel.sitter.PetSitterMainViewModelFactory
import tn.petcare_android.viewmodel.sitter.PetSitterMainUiState
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState
import tn.petcare_android.ui.screens.role.SittingRequest
import tn.petcare_android.ui.screens.role.SittingBooking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSitterMainScreen(
    navController: androidx.navigation.NavHostController,
    themePreference: ThemePreference,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navControllerLocal = remember { navController }
    val viewModel: PetSitterMainViewModel = viewModel(factory = PetSitterMainViewModelFactory(context))
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val sittingRequests by viewModel.sittingRequests.collectAsState()
    val activeBookings by viewModel.activeBookings.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSittingRequests()
        viewModel.loadActiveBookings()
        profileViewModel.loadProfile()
    }

    val userName = (profileUiState as? ProfileUiState.Success)?.user?.name
    val userImage = (profileUiState as? ProfileUiState.Success)?.user?.profileImage
    val userRole = (profileUiState as? ProfileUiState.Success)?.user?.role ?: "sitter"

    // Observe drawer state to conditionally render overlay
    val isDrawerOpen by tn.petcare_android.ui.components.DrawerState.isExpanded

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopNavBar(
                title = "Pet Sitter Dashboard",
                navController = navControllerLocal,
                showBackButton = false,
                showMenuButton = true,
                onSettingsClick = {
                    showSettingsMenu = true
                }
            )
        },
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
                    title = "Active Bookings",
                    value = activeBookings.size.toString(),
                    icon = Icons.Default.DateRange,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "New Requests",
                    value = sittingRequests.filter { it.status == "pending" }.size.toString(),
                    icon = Icons.Default.Notifications,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sitting Requests") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Active Bookings") }
                )
            }

            // Content
            when (selectedTab) {
                0 -> SittingRequestsTab(
                    requests = sittingRequests,
                    onAccept = { request -> viewModel.acceptRequest(request.id) },
                    onReject = { request -> viewModel.rejectRequest(request.id) },
                    isLoading = uiState is PetSitterMainUiState.Loading
                )
                1 -> ActiveBookingsTab(
                    bookings = activeBookings,
                    isLoading = uiState is PetSitterMainUiState.Loading
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
                            navControllerLocal.navigate("edit_sitter_profile")
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
fun SittingRequestsTab(
    requests: List<SittingRequest>,
    onAccept: (SittingRequest) -> Unit,
    onReject: (SittingRequest) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No sitting requests", fontSize = 16.sp, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(requests) { request ->
            SittingRequestCard(
                request = request,
                onAccept = { onAccept(request) },
                onReject = { onReject(request) }
            )
        }
    }
}

@Composable
fun SittingRequestCard(
    request: SittingRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (request.status == "pending") Color(0xFFFFF3CD) else Color.White
        )
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
                        text = request.petName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Owner: ${request.ownerName}",
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
                            text = "${request.startDate} - ${request.endDate}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$${String.format("%.2f", request.hourlyRate)}/hour",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            if (request.status == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept")
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = when (request.status) {
                        "accepted" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        "rejected" -> Color.Red.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = request.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (request.status) {
                            "accepted" -> Color(0xFF4CAF50)
                            "rejected" -> Color.Red
                            else -> Color.Gray
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveBookingsTab(
    bookings: List<SittingBooking>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (bookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No active bookings", fontSize = 16.sp, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bookings) { booking ->
            BookingCard(booking = booking)
        }
    }
}

@Composable
fun BookingCard(booking: SittingBooking) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate to booking details */ },
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
                    text = booking.petName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Owner: ${booking.ownerName}",
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
                        text = "${booking.startDate} - ${booking.endDate}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}


