package tn.petcare_android.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.petcare_android.data.model.booking.CreateBookingRequest
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.booking.BookingViewModel
import tn.petcare_android.viewmodel.pet.PetViewModel
import tn.petcare_android.viewmodel.pet.PetViewModelFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Enhanced Booking Create Screen
 * iOS Reference: PetSitterProfileView.swift booking sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCreateScreen(
    viewModel: BookingViewModel,
    onBookingCreated: () -> Unit,
    providerId: String = "",
    providerName: String = "",
    providerType: String = "vet", // "vet", "sitter", or "salon"
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val petViewModel: PetViewModel = viewModel(
        factory = PetViewModelFactory(context)
    )
    
    val pets by petViewModel.pets.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Form state
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var selectedServiceType by remember { mutableStateOf("Consultation") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60") }
    var price by remember { mutableStateOf("") }
    
    // Dropdowns
    var showPetDropdown by remember { mutableStateOf(false) }
    var showServiceDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Service types based on provider type
    val serviceTypes = when (providerType) {
        "vet" -> listOf("Consultation", "Vaccination", "Surgery", "Dental Care", "Emergency", "Check-up", "Other")
        "salon" -> listOf("Grooming", "Nail Trimming", "Bathing", "Styling", "Brushing", "De-shedding", "Teeth Cleaning", "Ear Cleaning", "Other")
        else -> listOf("Pet Sitting", "Dog Walking", "Pet Grooming", "Pet Training", "Overnight Care", "Other")
    }
    
    // Load pets
    LaunchedEffect(Unit) {
        petViewModel.loadPets()
    }
    
    // Date picker dialog
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        selectedDate = LocalDate.of(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Book ${when (providerType) {
                            "vet" -> "Appointment"
                            "salon" -> "Grooming Service"
                            else -> "Service"
                        }}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Provider Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (providerType) {
                            "vet" -> Icons.Default.Star
                            "salon" -> Icons.Default.Favorite
                            else -> Icons.Default.Favorite
                        },
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = providerName.ifEmpty { "Provider" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = when (providerType) {
                                "vet" -> "Veterinarian"
                                "salon" -> "Pet Salon"
                                else -> "Pet Sitter"
                            },
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Pet Selection
            Text(
                text = "Select Pet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            ExposedDropdownMenuBox(
                expanded = showPetDropdown,
                onExpandedChange = { showPetDropdown = !showPetDropdown }
            ) {
                OutlinedTextField(
                    value = selectedPet?.name ?: "Select a pet",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPetDropdown) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = showPetDropdown,
                    onDismissRequest = { showPetDropdown = false }
                ) {
                    pets.forEach { pet ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(pet.name)
                                    Text(
                                        text = "• ${pet.species}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            },
                            onClick = {
                                selectedPet = pet
                                showPetDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Service Type
            Text(
                text = "Service Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            ExposedDropdownMenuBox(
                expanded = showServiceDropdown,
                onExpandedChange = { showServiceDropdown = !showServiceDropdown }
            ) {
                OutlinedTextField(
                    value = selectedServiceType,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showServiceDropdown) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = showServiceDropdown,
                    onDismissRequest = { showServiceDropdown = false }
                ) {
                    serviceTypes.forEach { service ->
                        DropdownMenuItem(
                            text = { Text(service) },
                            onClick = {
                                selectedServiceType = service
                                showServiceDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Date and Time
            Text(
                text = "Date & Time",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date picker
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = CardBackground
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Date",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date",
                            tint = OrangeAccent
                        )
                    }
                }
                
                // Time picker (simplified)
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = CardBackground
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Time",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Select time",
                            tint = OrangeAccent
                        )
                    }
                }
            }
            
            // Duration
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Info, "Duration")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            // Price (optional)
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (optional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Info, "Price")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            // Notes
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            // Error message
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }
            
            // Submit button
            Button(
                onClick = {
                    if (selectedPet == null) {
                        // Show error
                        return@Button
                    }
                    
                    // Format date-time to ISO 8601
                    val dateTimeString = "${selectedDate}T${selectedTime}:00.000Z"
                    
                    val request = CreateBookingRequest(
                        providerId = providerId,
                        providerType = providerType,
                        petId = selectedPet?.id,
                        serviceType = selectedServiceType,
                        description = if (description.isNotBlank()) description else null,
                        dateTime = dateTimeString,
                        duration = duration.toIntOrNull(),
                        price = price.toDoubleOrNull()
                    )
                    
                    viewModel.createBooking(request)
                    onBookingCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent
                ),
                enabled = !loading && selectedPet != null
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Book ${if (providerType == "vet") "Appointment" else "Service"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
