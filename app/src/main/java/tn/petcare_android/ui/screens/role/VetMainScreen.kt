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
import tn.petcare_android.viewmodel.vet.VetMainViewModel
import tn.petcare_android.viewmodel.vet.VetMainViewModelFactory
import tn.petcare_android.viewmodel.vet.VetMainUiState
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetMainScreen(
    navController: androidx.navigation.NavHostController,
    themePreference: ThemePreference,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navControllerLocal = remember { navController }
    val viewModel: VetMainViewModel = viewModel(factory = VetMainViewModelFactory(context))
    val profileViewModel: tn.petcare_android.viewmodel.profile.ProfileViewModel = viewModel(
        factory = tn.petcare_android.viewmodel.profile.ProfileViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPatients()
        viewModel.loadAppointments()
        profileViewModel.loadProfile()
    }

    val userName = (profileUiState as? tn.petcare_android.viewmodel.profile.ProfileUiState.Success)?.user?.name
    val userImage = (profileUiState as? tn.petcare_android.viewmodel.profile.ProfileUiState.Success)?.user?.profileImage
    val userRole = (profileUiState as? tn.petcare_android.viewmodel.profile.ProfileUiState.Success)?.user?.role ?: "vet"

    // Observe drawer state to conditionally render overlay
    val isDrawerOpen by tn.petcare_android.ui.components.DrawerState.isExpanded

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopNavBar(
                    title = "Veterinarian Dashboard",
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
                    title = "Total Patients",
                    value = patients.size.toString(),
                    icon = Icons.Default.Favorite,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Today's Appointments",
                    value = appointments.filter { it.isToday }.size.toString(),
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
                    text = { Text("Patients (${patients.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Appointments") }
                )
            }

            // Content
            when (selectedTab) {
                0 -> PatientsTab(
                    patients = patients,
                    isLoading = uiState is VetMainUiState.Loading,
                    navController = navControllerLocal
                )
                1 -> AppointmentsTab(
                    appointments = appointments,
                    isLoading = uiState is VetMainUiState.Loading
                )
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
                            navControllerLocal.navigate("edit_vet_profile")
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
}

@Composable
private fun PatientsTab(
    patients: List<VetPatient>,
    isLoading: Boolean,
    navController: androidx.navigation.NavHostController
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (patients.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No patients yet", fontSize = 16.sp, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(patients) { patient ->
            PatientCard(patient = patient, navController = navController)
        }
    }
}

@Composable
private fun PatientCard(
    patient: VetPatient,
    navController: androidx.navigation.NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("pet_detail/${patient.id}")
            },
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
                    text = patient.petName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Owner: ${patient.ownerName}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (patient.needsAttention.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = patient.needsAttention,
                            fontSize = 14.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
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

@Composable
private fun AppointmentsTab(
    appointments: List<VetAppointment>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (appointments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No appointments scheduled", fontSize = 16.sp, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appointments) { appointment ->
            AppointmentCard(appointment = appointment)
        }
    }
}

@Composable
private fun AppointmentCard(appointment: VetAppointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (appointment.isToday) Color(0xFFFFF3CD) else Color.White
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
                    text = appointment.petName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Owner: ${appointment.ownerName}",
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
                        text = appointment.dateTime,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                if (appointment.reason.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reason: ${appointment.reason}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Data classes
data class VetPatient(
    val id: String,
    val petName: String,
    val ownerName: String,
    val ownerId: String,
    val needsAttention: String = "",
    val lastVisit: String? = null
)

data class VetAppointment(
    val id: String,
    val petName: String,
    val ownerName: String,
    val ownerId: String,
    val dateTime: String,
    val reason: String = "",
    val isToday: Boolean = false
)

