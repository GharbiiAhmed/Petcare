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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSitterProfileScreen(
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
    var phone by remember { mutableStateOf("") }
    var sitterAddress by remember { mutableStateOf("") }
    var yearsOfExperience by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var canHostPets by remember { mutableStateOf(false) }
    
    // Map picker state
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    
    var isLoading by remember { mutableStateOf(true) }
    
    // Fetch sitter data on screen load
    LaunchedEffect(Unit) {
        viewModel.fetchSitterProfile()
    }
    
    // Populate form when data is loaded
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is VetSitterUiState.SitterProfileLoaded -> {
                val sitter = state.sitter
                phone = sitter.phoneNumber ?: sitter.phone ?: ""
                sitterAddress = sitter.sitterAddress ?: ""
                yearsOfExperience = sitter.yearsOfExperience?.toString() ?: ""
                bio = sitter.bio ?: ""
                hourlyRate = sitter.hourlyRate?.toString() ?: ""
                canHostPets = sitter.canHostPets ?: false
                selectedLatitude = sitter.latitude
                selectedLongitude = sitter.longitude
                
                isLoading = false
            }
            is VetSitterUiState.Success -> {
                // Profile updated successfully
                navController.popBackStack()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Edit Sitter Profile",
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
                            onClick = { viewModel.fetchSitterProfile() },
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
                                value = sitterAddress,
                                onValueChange = { sitterAddress = it },
                                label = { Text("Address") },
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
                                        "Pick Your Location on Map"
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
                            OutlinedTextField(
                                value = hourlyRate,
                                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) hourlyRate = it },
                                label = { Text("Hourly Rate ($)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangeAccent,
                                    focusedLabelColor = OrangeAccent
                                ),
                                leadingIcon = { Icon(Icons.Default.Star, "Rate") }
                            )
                        }
                        
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Can host pets at your home?",
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Switch(
                                    checked = canHostPets,
                                    onCheckedChange = { canHostPets = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OrangeAccent,
                                        checkedTrackColor = OrangeAccent.copy(alpha = 0.5f)
                                    )
                                )
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
                                    viewModel.updateSitterProfile(
                                        phone = phone,
                                        sitterAddress = sitterAddress,
                                        yearsOfExperience = yearsOfExperience.toIntOrNull(),
                                        bio = bio,
                                        hourlyRate = hourlyRate.toDoubleOrNull(),
                                        canHostPets = canHostPets,
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
