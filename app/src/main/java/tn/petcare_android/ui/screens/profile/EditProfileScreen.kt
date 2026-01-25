package tn.petcare_android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileAction
import tn.petcare_android.viewmodel.profile.ProfileUiState
import java.io.File

/**
 * Edit Profile Screen for completing user profile
 * iOS Reference: EditProfileView.swift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var hasPets by remember { mutableStateOf(false) }
    var hasLoaded by remember { mutableStateOf(false) }

    // Image picker
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }

    // UI states
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            // Convert URI to File for upload
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { stream ->
                file.outputStream().use { output ->
                    stream.copyTo(output)
                }
            }
            selectedImageFile = file
        }
    }

    // Load initial values from user profile
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success && !hasLoaded) {
            val user = (uiState as ProfileUiState.Success).user
            name = user.name ?: ""
            phone = user.phone ?: user.phoneNumber ?: ""
            country = user.country ?: ""
            city = user.city ?: ""
            hasPets = (uiState as ProfileUiState.Success).pets.isNotEmpty()
            hasLoaded = true
        }
    }

    // Handle save action result
    LaunchedEffect(actionState) {
        when (actionState) {
            is ProfileAction.Success -> {
                isSaving = false
                saveError = null
                // Navigate back
                navController.popBackStack()
            }
            is ProfileAction.Error -> {
                isSaving = false
                saveError = (actionState as ProfileAction.Error).message
            }
            is ProfileAction.Loading -> {
                isSaving = true
            }
            else -> {}
        }
    }

    // Validation
    val canSave = name.trim().isNotEmpty() &&
            phone.trim().isNotEmpty() &&
            country.trim().isNotEmpty() &&
            city.trim().isNotEmpty()

    // Animation
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "contentFade"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Complete Profile",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController,
                actions = {
                    TextButton(
                        onClick = {
                            saveError = null
                            coroutineScope.launch {
                                viewModel.updateProfileWithImage(
                                    name = name.trim(),
                                    phoneNumber = phone.trim(),
                                    country = country.trim(),
                                    city = city.trim(),
                                    hasPhoto = selectedImageFile != null || selectedImageUri != null,
                                    photoFile = selectedImageFile
                                )
                            }
                        },
                        enabled = canSave && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text("Save", color = if (canSave) VetCanyon else TextSecondary)
                        }
                    }
                }
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .graphicsLayer { this.alpha = alpha },
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error banner
            if (saveError != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = saveError ?: "",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Profile Details Section
            item {
                ProfileDetailsSection(
                    name = name,
                    onNameChange = { name = it },
                    selectedImageUri = selectedImageUri,
                    currentAvatarUrl = (uiState as? ProfileUiState.Success)?.user?.profileImage,
                    onImageClick = { imagePickerLauncher.launch("image/*") },
                    onRemoveImage = {
                        selectedImageUri = null
                        selectedImageFile = null
                    },
                    hasImage = selectedImageUri != null || (uiState as? ProfileUiState.Success)?.user?.profileImage != null
                )
            }

            // Contact Information Section
            item {
                ContactInformationSection(
                    phone = phone,
                    onPhoneChange = { phone = it },
                    country = country,
                    onCountryChange = { country = it },
                    city = city,
                    onCityChange = { city = it }
                )
            }

            // Pet Information Section
            item {
                PetInformationSection(
                    hasPets = hasPets,
                    onHasPetsChange = { hasPets = it }
                )
            }
        }
    }

    // Loading overlay
    if (isSaving) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = VetCanyon)
                    Text("Saving…", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailsSection(
    name: String,
    onNameChange: (String) -> Unit,
    selectedImageUri: Uri?,
    currentAvatarUrl: String?,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    hasImage: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Avatar picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar image
                    when {
                        selectedImageUri != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        !currentAvatarUrl.isNullOrBlank() -> {
                            Image(
                                painter = rememberAsyncImagePainter(currentAvatarUrl),
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "No photo",
                                modifier = Modifier.size(36.dp),
                                tint = VetCanyon
                            )
                        }
                    }

                    // Circle border
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .border(1.dp, VetStroke, CircleShape)
                    )
                }

                // Image action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onImageClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = VetCanyon
                        ),
                        border = BorderStroke(1.dp, VetCanyon),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Photo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (hasImage) {
                        OutlinedButton(
                            onClick = onRemoveImage,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFD32F2F),
                                containerColor = Color(0xFFD32F2F).copy(alpha = 0.12f)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VetInputBackground,
                    unfocusedContainerColor = VetInputBackground,
                    focusedBorderColor = VetStroke,
                    unfocusedBorderColor = VetStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = VetCanyon
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun ContactInformationSection(
    phone: String,
    onPhoneChange: (String) -> Unit,
    country: String,
    onCountryChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Contact Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VetInputBackground,
                    unfocusedContainerColor = VetInputBackground,
                    focusedBorderColor = VetStroke,
                    unfocusedBorderColor = VetStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = VetCanyon
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = country,
                onValueChange = onCountryChange,
                label = { Text("Country") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VetInputBackground,
                    unfocusedContainerColor = VetInputBackground,
                    focusedBorderColor = VetStroke,
                    unfocusedBorderColor = VetStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = VetCanyon
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VetInputBackground,
                    unfocusedContainerColor = VetInputBackground,
                    focusedBorderColor = VetStroke,
                    unfocusedBorderColor = VetStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = VetCanyon
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun PetInformationSection(
    hasPets: Boolean,
    onHasPetsChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pet Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "I've added my pets",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Let us know if you already listed your pets in the app.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = hasPets,
                    onCheckedChange = onHasPetsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VetCanyon,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = TextSecondary.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
