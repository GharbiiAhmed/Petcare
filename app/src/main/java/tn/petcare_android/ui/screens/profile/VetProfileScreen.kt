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
fun VetProfileScreen(
    navController: androidx.navigation.NavHostController,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    
    // Fetch vet profile data (this includes populated user data)
    var vetProfile by remember { mutableStateOf<tn.petcare_android.data.model.auth.AppUser?>(null) }
    var isLoadingVetProfile by remember { mutableStateOf(false) }
    var vetProfileError by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    
    // Function to load vet profile
    val loadVetProfile: () -> Unit = {
        android.util.Log.d("VetProfileScreen", "Loading vet profile...")
        isLoadingVetProfile = true
        coroutineScope.launch {
            try {
                val tokenManager = TokenManager(context)
                val token = tokenManager.getAccessToken().firstOrNull()
                android.util.Log.d("VetProfileScreen", "Token exists: ${!token.isNullOrEmpty()}")
                if (!token.isNullOrEmpty()) {
                    val userId = JwtDecoder.getUserIdFromToken(token)
                    android.util.Log.d("VetProfileScreen", "User ID: $userId")
                    if (!userId.isNullOrEmpty()) {
                        android.util.Log.d("VetProfileScreen", "Calling API: GET /veterinarians/$userId")
                        val response = RetrofitInstance.vetSitterApi.getVet(userId)
                        android.util.Log.d("VetProfileScreen", "API response code: ${response.code()}, success: ${response.isSuccessful}")
                        if (response.isSuccessful) {
                            val body = response.body()
                            vetProfile = body
                            // Log the parsed response to see what fields are actually present
                            android.util.Log.d("VetProfileScreen", "Vet profile response body: $body")
                            android.util.Log.d("VetProfileScreen", "Vet profile loaded: phoneNumber=${vetProfile?.phoneNumber}, country=${vetProfile?.country}, city=${vetProfile?.city}, name=${vetProfile?.name}")
                            android.util.Log.d("VetProfileScreen", "Vet clinic: ${vetProfile?.vetClinicName}, address: ${vetProfile?.vetAddress}, license: ${vetProfile?.vetLicenseNumber}")
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMsg = "Failed to load vet profile: ${response.code()} - ${response.message()}"
                            android.util.Log.e("VetProfileScreen", "$errorMsg - $errorBody")
                            vetProfileError = errorMsg
                        }
                    } else {
                        android.util.Log.e("VetProfileScreen", "User ID is empty")
                    }
                } else {
                    android.util.Log.e("VetProfileScreen", "Token is empty")
                }
            } catch (e: Exception) {
                val errorMsg = "Error loading vet profile: ${e.message}"
                android.util.Log.e("VetProfileScreen", errorMsg, e)
                e.printStackTrace()
                vetProfileError = errorMsg
            } finally {
                isLoadingVetProfile = false
            }
        }
    }
    
    // Load vet profile when screen becomes visible (this includes all user data)
    LaunchedEffect(Unit) {
        loadVetProfile()
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Veterinarian Profile",
                navController = navController,
                showBackButton = true
            )
        }
    ) { paddingValues ->
        when {
            isLoadingVetProfile -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            vetProfileError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = vetProfileError ?: "Error loading profile",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = { 
                            vetProfileError = null
                            loadVetProfile() 
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }

            vetProfile != null -> {
                val vet = vetProfile!!
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
                            .background(BlueAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (vet.avatarUrl != null) {
                            val imageUrl = vet.avatarUrl
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(imageUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name with Vet Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = vet.name ?: "Veterinarian",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Veterinarian",
                            tint = BlueAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Veterinarian",
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

                        val displayPhone = vet.phoneNumber?.takeIf { it.isNotBlank() } ?: "Not set"
                        val displayCountry = vet.country?.takeIf { it.isNotBlank() }
                            ?: "Not set"
                        val displayCity = vet.city?.takeIf { it.isNotBlank() }
                            ?: "Not set"
                        
                        VetInfoCard(label = "Email", value = vet.email)
                        Spacer(modifier = Modifier.height(12.dp))
                        VetInfoCard(label = "Phone Number", value = displayPhone)
                        Spacer(modifier = Modifier.height(12.dp))
                        VetInfoCard(label = "Country", value = displayCountry)
                        Spacer(modifier = Modifier.height(12.dp))
                        VetInfoCard(label = "City", value = displayCity)
                        
                        // Show vet-specific information
                        Spacer(modifier = Modifier.height(12.dp))
                        VetInfoCard(label = "Clinic Name", value = vet.vetClinicName?.takeIf { it.isNotBlank() } ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        VetInfoCard(label = "Clinic Address", value = vet.vetAddress?.takeIf { it.isNotBlank() } ?: "Not set")
                        val licenseNumber = vet.vetLicenseNumber?.takeIf { it.isNotBlank() }
                        if (licenseNumber != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            VetInfoCard(label = "License Number", value = licenseNumber)
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
                        VetSettingsCard(
                            icon = Icons.Default.Edit,
                            label = "Edit Profile",
                            onClick = { navController.navigate("edit_vet_profile") }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Password
                        VetSettingsCard(
                            icon = Icons.Default.Lock,
                            label = "Change Password",
                            onClick = onNavigateToChangePassword
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Email
                        VetSettingsCard(
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
                        VetSettingsCard(
                            icon = Icons.Default.ExitToApp,
                            label = "Logout",
                            onClick = { showLogoutConfirmation = true },
                            textColor = Color.Red
                        )
                    }
                }
            }

            else -> {
                // No data loaded yet
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
private fun VetInfoCard(label: String, value: String) {
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
private fun VetSettingsCard(
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

