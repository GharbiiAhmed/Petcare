package tn.petcare_android.ui.screens.join

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.ui.components.MapPickerSheet
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*


enum class SitterService(val title: String) {
    WALKING("Dog Walking"),
    HOME_VISITS("Home Visits"),
    DAYCARE("Daycare"),
    OVERNIGHT("Overnight"),
    GROOMING("Grooming"),
    TRAINING("Training"),
    VET_ASSIST("Vet Visit Assist")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinPetSitterScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    fromSubscription: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { tn.petcare_android.data.storage.TokenManager(context) }
    
    val viewModel: tn.petcare_android.viewmodel.vetsitter.JoinVetSitterViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return tn.petcare_android.viewmodel.vetsitter.JoinVetSitterViewModel(tokenManager) as T
            }
        }
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggedIn = currentUser != null
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var yearsOfExperience by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var selectedServices by remember { mutableStateOf(setOf<SitterService>()) }
    var availableWeekends by remember { mutableStateOf(true) }
    var canHostPets by remember { mutableStateOf(false) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Map picker state
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }


    val isNameValid = fullName.trim().length >= 2
    val isEmailValid = email.trim().contains("@") && email.trim().contains(".")
    val isPhoneValid = phone.filter { it.isDigit() }.length >= 6
    val isAddressValid = address.trim().length >= 3
    val isYearsValid = yearsOfExperience.toIntOrNull()?.let { it >= 0 } ?: false
    val isRateValid = hourlyRate.toDoubleOrNull()?.let { it > 0 } ?: false
    val hasServices = selectedServices.isNotEmpty()
    val isPasswordValid = password.length >= 6
    val isConfirmValid = confirmPassword.isNotEmpty() && confirmPassword == password

    val canSubmit = isNameValid && isEmailValid && isPhoneValid && isAddressValid &&
            isYearsValid && isRateValid && hasServices &&
            (isLoggedIn || (isPasswordValid && isConfirmValid))
    
    // Pre-fill form if user is logged in
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null && fullName.isEmpty()) {
            fullName = user.name ?: ""
            email = user.email
            phone = user.phoneNumber ?: user.phone ?: ""
        }
    }
    
    // Handle form submission success
    LaunchedEffect(uiState) {
        when (uiState) {
            is tn.petcare_android.viewmodel.vetsitter.VetSitterUiState.Success -> {
                val successState = uiState as tn.petcare_android.viewmodel.vetsitter.VetSitterUiState.Success
                // Profile created successfully - navigate to home
                navController.navigate("home") {
                    popUpTo("join_sitter") { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> {}
        }
    }


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
                title = "Join as Pet Sitter",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .graphicsLayer { this.alpha = alpha },
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Care for pets, earn with flexibility.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Join our community of trusted pet sitters",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }


            item {
                SectionTitle("PROFILE")
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InputField(
                        icon = Icons.Default.Person,
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Full Name",
                        isValid = isNameValid,
                        isTouched = fullName.isNotEmpty()
                    )

                    InputField(
                        icon = Icons.Default.Email,
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        keyboardType = KeyboardType.Email,
                        isValid = isEmailValid,
                        isTouched = email.isNotEmpty()
                    )

                    InputField(
                        icon = Icons.Default.Phone,
                        value = phone,
                        onValueChange = { phone = it.filter { c -> c.isDigit() } },
                        placeholder = "Phone",
                        keyboardType = KeyboardType.Phone,
                        isValid = isPhoneValid,
                        isTouched = phone.isNotEmpty()
                    )

                    // Address with map picker button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.weight(1f).height(52.dp),
                            placeholder = {
                                Text(
                                    text = "Address / City",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (address.isNotEmpty() && isAddressValid) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardBackground,
                                unfocusedContainerColor = CardBackground,
                                focusedBorderColor = if (address.isNotEmpty() && !isAddressValid) Color(0xFFF59E0B) else VetCanyon,
                                unfocusedBorderColor = VetStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = VetCanyon
                            )
                        )
                        Button(
                            onClick = { showMapPicker = true },
                            modifier = Modifier.height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, VetStroke)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Set on map",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }


            item {
                SectionTitle("EXPERIENCE & RATE")
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InputField(
                        icon = Icons.Default.Info,
                        value = yearsOfExperience,
                        onValueChange = { yearsOfExperience = it.filter { c -> c.isDigit() } },
                        placeholder = "Years of Experience",
                        keyboardType = KeyboardType.Number,
                        isValid = isYearsValid,
                        isTouched = yearsOfExperience.isNotEmpty()
                    )

                    InputField(
                        icon = Icons.Default.Star,
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = "Hourly Rate (€)",
                        keyboardType = KeyboardType.Decimal,
                        isValid = isRateValid,
                        isTouched = hourlyRate.isNotEmpty()
                    )
                }
            }

            item {
                SectionTitle("SERVICES")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val services = SitterService.values().toList()
                    // Create rows of 2 items each
                    services.chunked(2).forEach { rowServices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowServices.forEach { service ->
                                ServiceChip(
                                    service = service,
                                    isSelected = selectedServices.contains(service),
                                    onClick = {
                                        selectedServices = if (selectedServices.contains(service)) {
                                            selectedServices - service
                                        } else {
                                            selectedServices + service
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Add spacer if odd number of items
                            if (rowServices.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("PREFERENCES")
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available on Weekends",
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Switch(
                            checked = availableWeekends,
                            onCheckedChange = { availableWeekends = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Can Host Pets",
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Switch(
                            checked = canHostPets,
                            onCheckedChange = { canHostPets = it }
                        )
                    }
                }
            }

            item {
                SectionTitle("BIO")
            }

            item {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Tell us about yourself...", color = TextSecondary) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VetInputBackground,
                        unfocusedContainerColor = VetInputBackground,
                        focusedBorderColor = VetCanyon,
                        unfocusedBorderColor = VetStroke
                    )
                )
            }

            // Password fields (only for new registrations)
            if (!isLoggedIn) {
                item {
                    SectionTitle("ACCOUNT SECURITY")
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PasswordField(
                            icon = Icons.Default.Lock,
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Password",
                            showPassword = showPassword,
                            onToggleVisibility = { showPassword = !showPassword },
                            isValid = isPasswordValid,
                            isTouched = password.isNotEmpty()
                        )

                        PasswordField(
                            icon = Icons.Default.CheckCircle,
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Confirm Password",
                            showPassword = showConfirmPassword,
                            onToggleVisibility = { showConfirmPassword = !showConfirmPassword },
                            isValid = isConfirmValid,
                            isTouched = confirmPassword.isNotEmpty()
                        )
                    }
                }
            }
            
            // Error message
            if (uiState is tn.petcare_android.viewmodel.vetsitter.VetSitterUiState.Error) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = (uiState as tn.petcare_android.viewmodel.vetsitter.VetSitterUiState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }


            item {
                Button(
                    onClick = {
                        val servicesList = selectedServices.map { it.name.lowercase() }
                        val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                        val years = yearsOfExperience.toIntOrNull()
                        
                        viewModel.submitSitter(
                            fullName = fullName,
                            email = email,
                            phoneNumber = phone.ifEmpty { null },
                            hourlyRate = rate,
                            sitterAddress = address,
                            services = servicesList,
                            yearsOfExperience = years,
                            availableWeekends = availableWeekends,
                            canHostPets = canHostPets,
                            availability = null, // TODO: Add availability calendar
                            latitude = selectedLatitude,
                            longitude = selectedLongitude,
                            bio = bio.ifEmpty { null },
                            password = if (isLoggedIn) null else password
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    enabled = canSubmit && uiState !is tn.petcare_android.viewmodel.vetsitter.VetSitterUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmit && uiState !is tn.petcare_android.viewmodel.vetsitter.VetSitterUiState.Loading) VetCanyon else VetCanyon.copy(alpha = 0.4f)
                    )
                ) {
                    if (uiState is tn.petcare_android.viewmodel.vetsitter.VetSitterUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (isLoggedIn) "CONVERT TO PET SITTER" else "CREATE SITTER ACCOUNT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Map picker sheet
    if (showMapPicker) {
        MapPickerSheet(
            initialCoordinate = Pair(
                selectedLongitude ?: 10.1815,
                selectedLatitude ?: 36.8065
            ),
            onConfirm = { coordinate, addressText ->
                selectedLongitude = coordinate.first
                selectedLatitude = coordinate.second
                if (addressText != null) {
                    address = addressText
                }
            },
            onDismiss = { showMapPicker = false }
        )
    }
}

// MARK: - Components

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun InputField(
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isValid: Boolean = true,
    isTouched: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (isTouched) {
                Icon(
                    imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isValid) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            focusedBorderColor = if (isTouched && !isValid) Color(0xFFF59E0B) else VetCanyon,
            unfocusedBorderColor = VetStroke,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = VetCanyon
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        )
    )
}

@Composable
private fun PasswordField(
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit,
    isValid: Boolean = true,
    isTouched: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Row {
                if (isTouched) {
                    Icon(
                        imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isValid) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = TextSecondary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            focusedBorderColor = if (isTouched && !isValid) Color(0xFFF59E0B) else VetCanyon,
            unfocusedBorderColor = VetStroke,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = VetCanyon
        )
    )
}

@Composable
private fun ServiceChip(
    service: SitterService,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) VetCanyon.copy(alpha = 0.1f) else CardBackground,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) VetCanyon else VetStroke
        )
    ) {
        Text(
            text = service.title,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) VetCanyon else TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
