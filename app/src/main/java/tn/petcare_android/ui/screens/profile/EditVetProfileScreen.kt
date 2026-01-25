package tn.petcare_android.ui.screens.profile

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.components.MapPickerSheet
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.vetsitter.JoinVetSitterViewModel
import tn.petcare_android.viewmodel.vetsitter.VetSitterUiState

enum class VetSpecialty(val title: String) {
    GENERAL("General"),
    SURGERY("Surgery"),
    DERMATOLOGY("Dermatology"),
    EMERGENCY("Emergency"),
    DENTAL("Dental"),
    CARDIOLOGY("Cardiology"),
    RADIOLOGY("Radiology")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVetProfileScreen(
    navController: NavHostController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    
    val viewModel: JoinVetSitterViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return JoinVetSitterViewModel(tokenManager) as T
            }
        }
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Form state
    var licenseNumber by remember { mutableStateOf("") }
    var clinicName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var clinicAddress by remember { mutableStateOf("") }
    var yearsOfExperience by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedSpecs by remember { mutableStateOf(setOf<VetSpecialty>()) }
    
    // Map picker state
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    
    var isLoading by remember { mutableStateOf(true) }
    
    // Fetch vet data on screen load
    LaunchedEffect(Unit) {
        viewModel.fetchVetProfile()
    }
    
    // Populate form when data is loaded
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is VetSitterUiState.VetProfileLoaded -> {
                val vet = state.vet
                licenseNumber = vet.vetLicenseNumber ?: ""
                clinicName = vet.vetClinicName ?: ""
                phone = vet.phoneNumber ?: vet.phone ?: ""
                country = vet.country ?: ""
                city = vet.city ?: ""
                clinicAddress = vet.vetAddress ?: ""
                yearsOfExperience = vet.vetYearsOfExperience?.toString() ?: ""
                bio = vet.vetBio ?: ""
                selectedLatitude = vet.latitude
                selectedLongitude = vet.longitude
                
                // Parse specialties
                vet.vetSpecializations?.let { specs ->
                    selectedSpecs = specs.mapNotNull { spec ->
                        VetSpecialty.values().find { it.title.equals(spec, ignoreCase = true) }
                    }.toSet()
                }
                
                isLoading = false
            }
            is VetSitterUiState.Error -> {
                // Stop loading on error
                isLoading = false
            }
            is VetSitterUiState.Success -> {
                // Profile updated successfully
                navController.popBackStack()
            }
            is VetSitterUiState.Loading -> {
                isLoading = true
            }
            else -> {
                // For Idle state, if we're still loading, stop loading
                if (isLoading) {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Edit Vet Profile",
                navController = navController
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OrangeAccent
                    )
                }
                uiState is VetSitterUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Error",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (uiState as VetSitterUiState.Error).message,
                            color = TextPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchVetProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Professional Information",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = licenseNumber,
                                onValueChange = { licenseNumber = it },
                                label = { Text("License Number") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.Info, "License") }
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = clinicName,
                                onValueChange = { clinicName = it },
                                label = { Text("Clinic Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.LocationOn, "Clinic") }
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.Phone, "Phone") }
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = { Text("Country") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.LocationOn, "Country") }
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.Place, "City") }
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = clinicAddress,
                                onValueChange = { clinicAddress = it },
                                label = { Text("Clinic Address") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.Place, "Address") }
                            )
                        }
                        
                        item {
                            Button(
                                onClick = { showMapPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Icon(Icons.Default.LocationOn, "Pick Location")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (selectedLatitude != null && selectedLongitude != null)
                                        "Location Selected"
                                    else
                                        "Pick Clinic Location on Map"
                                )
                            }
                        }
                        
                        item {
                            OutlinedTextField(
                                value = yearsOfExperience,
                                onValueChange = { if (it.all { char -> char.isDigit() }) yearsOfExperience = it },
                                label = { Text("Years of Experience") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.DateRange, "Experience") }
                            )
                        }
                        
                        item {
                            Text(
                                text = "Specialties",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                VetSpecialty.values().toList().chunked(2).forEach { rowSpecs ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowSpecs.forEach { spec ->
                                            SpecialtyChip(
                                                specialty = spec,
                                                isSelected = selectedSpecs.contains(spec),
                                                onToggle = {
                                                    selectedSpecs = if (selectedSpecs.contains(spec)) {
                                                        selectedSpecs - spec
                                                    } else {
                                                        selectedSpecs + spec
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Bio") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                )
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    viewModel.updateVetProfile(
                                        licenseNumber = licenseNumber,
                                        clinicName = clinicName,
                                        phone = phone,
                                        country = country,
                                        city = city,
                                        clinicAddress = clinicAddress,
                                        yearsOfExperience = yearsOfExperience.toIntOrNull(),
                                        specialties = selectedSpecs.map { it.title },
                                        bio = bio,
                                        latitude = selectedLatitude,
                                        longitude = selectedLongitude
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = uiState !is VetSitterUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState is VetSitterUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
    
    if (showMapPicker) {
        MapPickerSheet(
            initialCoordinate = if (selectedLatitude != null && selectedLongitude != null) {
                Pair(selectedLongitude!!, selectedLatitude!!)
            } else {
                Pair(10.1815, 36.8065) // Default: Tunis
            },
            onConfirm = { coordinate, address ->
                selectedLongitude = coordinate.first
                selectedLatitude = coordinate.second
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}

@Composable
private fun SpecialtyChip(
    specialty: VetSpecialty,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = { Text(specialty.title, fontSize = 14.sp) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = OrangeAccent,
            selectedLabelColor = Color.White
        )
    )
}
