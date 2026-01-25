package tn.petcare_android.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.JwtDecoder
import tn.petcare_android.viewmodel.profile.ProfileUiState
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerProfileScreen(
    navController: androidx.navigation.NavHostController,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.UserDeleted) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Trainer Profile",
                navController = navController,
                showBackButton = true
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error loading profile",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = { viewModel.loadProfile() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image Section
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.user.profileImage != null) {
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(state.user.profileImage),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name with Trainer Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Trainer",
                            tint = OrangeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Trainer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Account Info Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Account Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TrainerInfoCard(label = "Email", value = state.user.email)
                        Spacer(modifier = Modifier.height(12.dp))
                        TrainerInfoCard(label = "Phone", value = state.user.phoneNumber ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        TrainerInfoCard(label = "Country", value = state.user.country ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        TrainerInfoCard(label = "City", value = state.user.city ?: "Not set")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Settings Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // View/Edit Trainer Profile
                        TrainerSettingsCard(
                            icon = Icons.Default.Edit,
                            label = "Edit Profile",
                            onClick = {
                                coroutineScope.launch {
                                    val token = TokenManager(context).getAccessToken().firstOrNull()
                                    val userId = token?.let { JwtDecoder.getUserIdFromToken(it) }
                                    userId?.let { navController.navigate("trainer_detail/$it") }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Password
                        TrainerSettingsCard(
                            icon = Icons.Default.Lock,
                            label = "Change Password",
                            onClick = onNavigateToChangePassword
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Email
                        TrainerSettingsCard(
                            icon = Icons.Default.Email,
                            label = "Change Email",
                            onClick = onNavigateToChangeEmail
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = VetStroke.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Logout
                        TrainerSettingsCard(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            label = "Logout",
                            onClick = { showLogoutConfirmation = true },
                            textColor = Color.Red
                        )
                    }
                }
            }

            else -> {}
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation = false
                        onLogout()
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TrainerInfoCard(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun TrainerSettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    textColor: Color = TextPrimary
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 16.sp,
                color = textColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}





