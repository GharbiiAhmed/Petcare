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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.storage.UserManager
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.profile.ProfileAction
import tn.petcare_android.viewmodel.profile.ProfileUiState
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.util.SubscriptionManager
import tn.petcare_android.data.model.subscription.SubscriptionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: androidx.navigation.NavHostController,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val themePreference = remember { ThemePreference(context) }
    
    // Get subscription status to conditionally show subscription management button
    val subscription by SubscriptionManager.subscription.collectAsState()
    // Also check user's hasActiveSubscription flag as fallback
    val userHasActiveSubscription = (uiState as? ProfileUiState.Success)?.user?.hasActiveSubscription == true
    // Show "Manage Subscription" if subscription exists and has an active status (active, expires_soon, or pending)
    // OR if user's hasActiveSubscription flag is true (fallback check)
    // Show "Subscribe Now" if subscription is null, none, canceled, or expired
    val hasSubscription = subscription?.let { sub ->
        val status = sub.subscriptionStatus
        status == SubscriptionStatus.ACTIVE || 
        status == SubscriptionStatus.EXPIRES_SOON || 
        status == SubscriptionStatus.PENDING
    } ?: false || userHasActiveSubscription

    // Dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    // Tab refresh notification (iOS Reference: MainTabView.swift lines 37-45)
    // Refresh when switching to Profile tab
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        // Load subscription status to show/hide subscription button
        SubscriptionManager.checkSubscriptionStatus()
    }
    
    // Profile completion check (iOS Reference: ProfileView.swift lines 200-250)
    // Navigate to edit profile if profile is incomplete
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val user = (uiState as ProfileUiState.Success).user
            if (tn.petcare_android.util.ProfileCompletionUtil.requiresProfileCompletion(user)) {
                // Navigate to edit profile screen
                navController.navigate("edit_profile")
            }
        }
        
        if (uiState is ProfileUiState.UserDeleted) {
            onLogout()
        }
    }

    // Handle action results
    LaunchedEffect(actionState) {
        when (actionState) {
            is ProfileAction.Success -> {
                android.widget.Toast.makeText(
                    context,
                    (actionState as ProfileAction.Success).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                showEditDialog = false
                showDeleteDialog = false
                viewModel.resetActionState()
            }
            is ProfileAction.Error -> {
                android.widget.Toast.makeText(
                    context,
                    (actionState as ProfileAction.Error).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

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
                title = "Profile",
                showBackButton = false,
                showMenuButton = true,
                onSettingsClick = { showSettingsSheet = true },
                onMessagesClick = { navController.navigate("conversations") },
                onNotificationsClick = { navController.navigate("notifications") },
                messageCount = messageCount,
                notificationCount = notificationCount
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadProfile() }) {
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
                            .background(PetAvatarBrown),
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
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp),
                                tint = BlueAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name with Verified Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            state.user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = TextPrimary
                        )
                        if (state.user.hasActiveSubscription == true) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = BlueAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        state.user.role.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            number = "${state.pets.size}",
                            label = "Pets",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            number = "0",
                            label = "Appointments",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Account Info Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Account Info",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard(label = "Email", value = state.user.email)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoCard(label = "Phone", value = state.user.phoneNumber ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoCard(label = "Country", value = state.user.country ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoCard(label = "City", value = state.user.city ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoCard(label = "Balance", value = "${state.user.balance ?: 0} DT")
                    }

                    Spacer(modifier = Modifier.height(32.dp))


                    // Settings Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Edit Profile Button
                        SettingsCard(
                            icon = "✏️",
                            label = "Edit Profile",
                            onClick = { showEditDialog = true }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Password - Only show for local users (not Google)
                        if (state.user.provider != "google") {
                            SettingsCard(
                                icon = "🔑",
                                label = "Change Password",
                                onClick = onNavigateToChangePassword
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Change Email - Only show for local users (not Google)
                        if (state.user.provider != "google") {
                            SettingsCard(
                                icon = "✉️",
                                label = "Change Email",
                                onClick = onNavigateToChangeEmail
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Delete Account Button
                        SettingsCard(
                            icon = "🗑️",
                            label = "Delete Account",
                            onClick = { showDeleteDialog = true },
                            isDestructive = true
                        )

                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Subscribe Now / Manage Subscription Button
                    if (hasSubscription) {
                        Button(
                            onClick = { navController.navigate("subscription_management") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlueAccent
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Manage Subscription",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { navController.navigate("subscription_benefits") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeAccent
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Subscribe Now",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Log Out Button
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val tokenManager = TokenManager(context)
                                val userManager = UserManager(context)
                                tokenManager.clearTokens()
                                userManager.clearUserId()
                                onLogout()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OrangeAccent
                        ),
                        border = BorderStroke(2.dp, OrangeAccent)
                    ) {
                        Text(
                            "LOG OUT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            ProfileUiState.Idle -> {}

            ProfileUiState.UserDeleted -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Edit Profile Dialog
        if (showEditDialog && uiState is ProfileUiState.Success) {
            EditProfileDialog(
                user = (uiState as ProfileUiState.Success).user,
                onDismiss = { showEditDialog = false },
                onSave = { name, phoneNumber, country, city, photoFile ->
                    viewModel.updateProfileWithImage(
                        name = name,
                        phoneNumber = phoneNumber,
                        country = country,
                        city = city,
                        photoFile = photoFile
                    )
                }
            )
        }

        // Delete Account Dialog
        if (showDeleteDialog) {
            DeleteAccountDialog(
                onConfirm = {
                    viewModel.deleteAccount()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
        
        // Settings Sheet (iOS Reference: ProfileView.swift settings sheet)
        if (showSettingsSheet && uiState is ProfileUiState.Success) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                containerColor = CardBackground,
                dragHandle = {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(42.dp)
                            .padding(vertical = 12.dp),
                        color = VetStroke.copy(alpha = 0.4f),
                        thickness = 4.dp
                    )
                }
            ) {
                SettingsSheetContent(
                    user = (uiState as ProfileUiState.Success).user,
                    themePreference = themePreference,
                    hasSubscription = hasSubscription,
                    onDismiss = { showSettingsSheet = false },
                    onNavigateToChangePassword = {
                        showSettingsSheet = false
                        onNavigateToChangePassword()
                    },
                    onNavigateToChangeEmail = {
                        showSettingsSheet = false
                        onNavigateToChangeEmail()
                    },
                    onNavigateToHelp = {
                        showSettingsSheet = false
                        // Navigate to help screen
                    },
                    onLogout = {
                        showSettingsSheet = false
                        showLogoutConfirmation = true
                    }
                )
            }
        }
        
        // Logout Confirmation Dialog
        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutConfirmation = false
                            coroutineScope.launch {
                                val tokenManager = TokenManager(context)
                                val userManager = UserManager(context)
                                tokenManager.clearTokens()
                                userManager.clearUserId()
                                onLogout()
                            }
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OrangeAccent
            )
        }
    }
}



@Composable
fun SettingsCard(
    icon: String,
    label: String,
    onClick: () -> Unit = {},
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) androidx.compose.ui.graphics.Color.Red else TextPrimary
            )
        }
    }
}

@Composable
fun ThemeToggleCard(isDarkMode: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDarkMode) "🌙" else "☀️",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isDarkMode) "Dark Mode" else "Light Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OrangeAccent,
                    checkedTrackColor = OrangeAccent.copy(alpha = 0.5f),
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = TextSecondary.copy(alpha = 0.3f)
                )
            )
        }
    }
}




/**
 * Settings Sheet Content matching iOS ProfileView settings
 * iOS Reference: ProfileView.swift lines 349-443
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheetContent(
    user: tn.petcare_android.data.model.auth.User,
    themePreference: ThemePreference,
    hasSubscription: Boolean,
    onDismiss: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onLogout: () -> Unit
) {
    val isDarkMode by themePreference.isDarkMode.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        // Theme Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎨", fontSize = 16.sp)
                    Text(
                        text = "Appearance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                
                // Theme Picker
                var showThemePicker by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { showThemePicker = true }) {
                        Text(
                            text = when {
                                isDarkMode -> "Dark"
                                else -> "Light"
                            },
                            color = VetCanyon
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showThemePicker,
                        onDismissRequest = { showThemePicker = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("System") },
                            onClick = {
                                // TODO: Implement system theme
                                showThemePicker = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Light") },
                            onClick = {
                                coroutineScope.launch {
                                    themePreference.setDarkMode(false)
                                }
                                showThemePicker = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dark") },
                            onClick = {
                                coroutineScope.launch {
                                    themePreference.setDarkMode(true)
                                }
                                showThemePicker = false
                            }
                        )
                    }
                }
            }
        }
        
        // Settings Options
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsRow(
                icon = "🔑",
                label = "Change Password",
                onClick = {
                    if (user.provider != "google") {
                        onNavigateToChangePassword()
                    }
                },
                enabled = user.provider != "google"
            )
            
            SettingsRow(
                icon = "✉️",
                label = "Change Email",
                onClick = {
                    if (user.provider != "google") {
                        onNavigateToChangeEmail()
                    }
                },
                enabled = user.provider != "google"
            )
            
            SettingsRow(
                icon = "❓",
                label = "Help",
                onClick = onNavigateToHelp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, VetCanyon),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = "LOG OUT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VetCanyon,
                letterSpacing = 0.5.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 16.sp)
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
