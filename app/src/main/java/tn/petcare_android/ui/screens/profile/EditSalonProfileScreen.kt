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
import tn.petcare_android.ui.screens.join.SalonService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSalonProfileScreen(
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
    var salonName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var salonAddress by remember { mutableStateOf("") }
    var yearsOfExperience by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedServices by remember { mutableStateOf(setOf<SalonService>()) }
    
    // Map picker state
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    
    var isLoading by remember { mutableStateOf(true) }
    
    // Fetch salon data on screen load
    LaunchedEffect(Unit) {
        viewModel.fetchSalonProfile()
    }
    
    // Populate form when data is loaded
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is VetSitterUiState.SalonProfileLoaded -> {
                val salon = state.salon
                salonName = salon.salonName ?: ""
                phone = salon.phoneNumber ?: salon.phone ?: ""
                country = salon.country ?: ""
                city = salon.city ?: ""
                salonAddress = salon.salonAddress ?: ""
                yearsOfExperience = salon.salonYearsOfExperience?.toString() ?: ""
                bio = salon.salonBio ?: ""
                selectedLatitude = salon.latitude
                selectedLongitude = salon.longitude
                
                // Parse services
                salon.salonServices?.let { services ->
                    selectedServices = services.mapNotNull { service ->
                        SalonService.values().find { 
                            it.name.equals(service, ignoreCase = true) || 
                            it.title.equals(service, ignoreCase = true)
                        }
                    }.toSet()
                }
                
                isLoading = false
            }
            is VetSitterUiState.Error -> {
                isLoading = false
            }
            is VetSitterUiState.Success -> {
                navController.popBackStack()
            }
            is VetSitterUiState.Loading -> {
                isLoading = true
            }
            else -> {
                if (isLoading) {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Edit Salon Profile",
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
                        color = Color(0xFF9C27B0)
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
                            onClick = { viewModel.fetchSalonProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
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
                                text = "Salon Information",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = salonName,
                                onValueChange = { salonName = it },
                                label = { Text("Salon Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    focusedLabelColor = Color(0xFF9C27B0)
                                ),
                                leadingIcon = { Icon(Icons.Default.Home, "Salon") }
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
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    focusedLabelColor = Color(0xFF9C27B0)
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
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    focusedLabelColor = Color(0xFF9C27B0)
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
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    focusedLabelColor = Color(0xFF9C27B0)
                                ),
                                leadingIcon = { Icon(Icons.Default.Place, "City") }
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = salonAddress,
                                onValueChange = { salonAddress = it },
                                label = { Text("Salon Address") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    focusedLabelColor = Color(0xFF9C27B0)
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
                                        "Pick Salon Location on Map"
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
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    focusedLabelColor = Color(0xFF9C27B0)
                                ),
                                leadingIcon = { Icon(Icons.Default.DateRange, "Experience") }
                            )
                        }
                        
                        item {
                            Text(
                                text = "Services",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SalonService.values().toList().chunked(2).forEach { rowServices ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowServices.forEach { service ->
                                            ServiceChip(
                                                service = service,
                                                isSelected = selectedServices.contains(service),
                                                onToggle = {
                                                    selectedServices = if (selectedServices.contains(service)) {
                                                        selectedServices - service
                                                    } else {
                                                        selectedServices + service
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
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    focusedLabelColor = Color(0xFF9C27B0)
                                ),
                                leadingIcon = { Icon(Icons.Default.Info, "Bio") }
                            )
                        }
                        
                        item {
                            Button(
                                onClick = {
                                    val servicesList = selectedServices.map { it.name.lowercase() }
                                    val years = yearsOfExperience.toIntOrNull()
                                    
                                    viewModel.updateSalonProfile(
                                        salonName = salonName,
                                        salonAddress = salonAddress,
                                        services = servicesList,
                                        yearsOfExperience = years,
                                        latitude = selectedLatitude,
                                        longitude = selectedLongitude,
                                        bio = bio.ifEmpty { null },
                                        phoneNumber = phone.ifEmpty { null },
                                        country = country.ifEmpty { null },
                                        city = city.ifEmpty { null }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState !is VetSitterUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                            ) {
                                if (uiState is VetSitterUiState.Loading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text("Save Changes", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Map picker sheet
    if (showMapPicker) {
        MapPickerSheet(
            onDismiss = { showMapPicker = false },
            onConfirm = { (lat, lng), _ ->
                selectedLatitude = lat
                selectedLongitude = lng
                showMapPicker = false
            }
        )
    }
}

@Composable
private fun ServiceChip(
    service: SalonService,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = { Text(service.title) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF9C27B0).copy(alpha = 0.2f),
            selectedLabelColor = Color(0xFF9C27B0)
        )
    )
}

