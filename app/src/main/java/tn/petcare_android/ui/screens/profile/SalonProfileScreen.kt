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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.firstOrNull
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.JwtDecoder
import tn.petcare_android.data.api.RetrofitInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonProfileScreen(
    navController: androidx.navigation.NavHostController,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onLogout: () -> Unit,
    salonId: String? = null // Optional salon ID to view other salons' profiles
) {
    val context = LocalContext.current
    
    // Fetch salon profile data (this includes populated user data)
    var salonProfile by remember { mutableStateOf<tn.petcare_android.data.model.auth.AppUser?>(null) }
    var isLoadingSalonProfile by remember { mutableStateOf(false) }
    var salonProfileError by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    
    // Function to load salon profile
    val loadSalonProfile: () -> Unit = {
        android.util.Log.d("SalonProfileScreen", "Loading salon profile...")
        isLoadingSalonProfile = true
        coroutineScope.launch {
            try {
                val tokenManager = TokenManager(context)
                // Use provided salonId or get current user's ID from token
                val userId = salonId ?: run {
                    val token = tokenManager.getAccessToken().firstOrNull()
                    if (!token.isNullOrEmpty()) {
                        JwtDecoder.getUserIdFromToken(token)
                    } else {
                        null
                    }
                }
                
                if (!userId.isNullOrEmpty()) {
                    val response = RetrofitInstance.salonApi.getSalon(userId)
                    if (response.isSuccessful) {
                        val body = response.body()
                        salonProfile = body
                        android.util.Log.d("SalonProfileScreen", "Salon profile response body: $body")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = "Failed to load salon profile: ${response.code()} - ${response.message()}"
                        android.util.Log.e("SalonProfileScreen", "$errorMsg - $errorBody")
                        salonProfileError = errorMsg
                    }
                } else {
                    salonProfileError = "User ID not found"
                }
            } catch (e: Exception) {
                val errorMsg = "Error loading salon profile: ${e.message}"
                android.util.Log.e("SalonProfileScreen", errorMsg, e)
                salonProfileError = errorMsg
            } finally {
                isLoadingSalonProfile = false
            }
        }
    }
    
    // Load salon profile when screen becomes visible or salonId changes
    LaunchedEffect(salonId) {
        loadSalonProfile()
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Pet Salon Profile",
                navController = navController,
                showBackButton = true
            )
        }
    ) { paddingValues ->
        when {
            isLoadingSalonProfile -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF9C27B0))
                }
            }

            salonProfileError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = salonProfileError ?: "Error loading profile",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = { 
                            salonProfileError = null
                            loadSalonProfile() 
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }

            salonProfile != null -> {
                val salon = salonProfile!!
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
                            .background(Color(0xFF9C27B0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (salon.avatarUrl != null) {
                            val imageUrl = salon.avatarUrl
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(imageUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name with Salon Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = salon.name ?: "Pet Salon",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Pet Salon",
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Pet Salon",
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

                        val displayPhone = salon.phoneNumber?.takeIf { it.isNotBlank() } ?: "Not set"
                        val displayCountry = salon.country?.takeIf { it.isNotBlank() } ?: "Not set"
                        val displayCity = salon.city?.takeIf { it.isNotBlank() } ?: "Not set"
                        
                        SalonInfoCard(label = "Email", value = salon.email)
                        Spacer(modifier = Modifier.height(12.dp))
                        SalonInfoCard(label = "Phone Number", value = displayPhone)
                        Spacer(modifier = Modifier.height(12.dp))
                        SalonInfoCard(label = "Country", value = displayCountry)
                        Spacer(modifier = Modifier.height(12.dp))
                        SalonInfoCard(label = "City", value = displayCity)
                        
                        // Show salon-specific information
                        Spacer(modifier = Modifier.height(12.dp))
                        SalonInfoCard(label = "Salon Name", value = salon.salonName?.takeIf { it.isNotBlank() } ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        SalonInfoCard(label = "Salon Address", value = salon.salonAddress?.takeIf { it.isNotBlank() } ?: "Not set")
                        
                        // Services
                        val services = salon.salonServices
                        if (!services.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            SalonInfoCard(
                                label = "Services",
                                value = services.joinToString(", ")
                            )
                        }
                        
                        // Years of Experience
                        salon.salonYearsOfExperience?.let { years ->
                            Spacer(modifier = Modifier.height(12.dp))
                            SalonInfoCard(label = "Years of Experience", value = "$years years")
                        }
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

                        // Edit Profile
                        SalonSettingsCard(
                            icon = Icons.Default.Edit,
                            label = "Edit Profile",
                            onClick = { navController.navigate("edit_salon_profile") }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Password
                        SalonSettingsCard(
                            icon = Icons.Default.Lock,
                            label = "Change Password",
                            onClick = onNavigateToChangePassword
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Email
                        SalonSettingsCard(
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
                        SalonSettingsCard(
                            icon = Icons.Default.ExitToApp,
                            label = "Logout",
                            onClick = { showLogoutConfirmation = true },
                            textColor = Color.Red
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No profile data available",
                        color = TextSecondary
                    )
                }
            }
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
private fun SalonInfoCard(label: String, value: String) {
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
private fun SalonSettingsCard(
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

