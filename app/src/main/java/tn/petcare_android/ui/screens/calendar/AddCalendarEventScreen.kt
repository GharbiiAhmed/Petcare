package tn.petcare_android.ui.screens.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.CalendarManager
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Add Calendar Event Screen - Manual Pet Reminder Creation
 * iOS Reference: AddCalendarEventView.swift
 * Allows users to create custom calendar events for pet care
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCalendarEventScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    petId: String? = null,
    eventType: String? = null
) {
    val context = LocalContext.current
    val calendarManager = remember { CalendarManager(context) }
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Parse event type from parameter
    val initialEventType = remember(eventType) {
        eventType?.let {
            try {
                EventType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                EventType.MEDICATION
            }
        } ?: EventType.MEDICATION
    }
    
    // Form state
    var selectedEventType by remember { mutableStateOf(initialEventType) }
    var title by remember { mutableStateOf("") }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    var notes by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.DAILY) }
    var showPetPicker by remember { mutableStateOf(false) }
    var showEventTypePicker by remember { mutableStateOf(false) }
    var showRecurrencePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    
    // Set selected pet if petId is provided (from pet calendar navigation)
    LaunchedEffect(petId, uiState) {
        if (petId != null && petId.isNotBlank() && uiState is ProfileUiState.Success) {
            val pets = (uiState as ProfileUiState.Success).pets
            val foundPet = pets.find { it.id == petId }
            if (foundPet != null) {
                selectedPet = foundPet
                // Pre-fill title with pet name if available
                if (title.isEmpty()) {
                    title = "${foundPet.name} - ${selectedEventType.displayName}"
                }
            }
        }
    }
    
    // Date Picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
        },
        selectedDate.get(Calendar.YEAR),
        selectedDate.get(Calendar.MONTH),
        selectedDate.get(Calendar.DAY_OF_MONTH)
    )
    
    // Time Picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, minute)
        },
        selectedTime.get(Calendar.HOUR_OF_DAY),
        selectedTime.get(Calendar.MINUTE),
        false
    )
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Add Reminder",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController,
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Event Type
                    item {
                        FormSection(title = "Event Type") {
                            SelectionCard(
                                label = selectedEventType.displayName,
                                icon = selectedEventType.icon,
                                onClick = { showEventTypePicker = true }
                            )
                        }
                    }
                    
                    // Title
                    item {
                        FormSection(title = "Title") {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter event title") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = VetInputBackground,
                                    unfocusedContainerColor = VetInputBackground,
                                    focusedBorderColor = VetCanyon,
                                    unfocusedBorderColor = VetStroke
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    
                    // Pet Selection
                    item {
                        FormSection(title = "Pet") {
                            if (petId != null) {
                                // Show selected pet as read-only when coming from pet calendar
                                SelectionCard(
                                    label = selectedPet?.name ?: "Loading...",
                                    icon = "🐾",
                                    onClick = { /* Disabled - pet is pre-selected */ },
                                    enabled = false
                                )
                            } else {
                                // Allow pet selection when not coming from pet calendar
                                SelectionCard(
                                    label = selectedPet?.name ?: "Select pet",
                                    icon = "🐾",
                                    onClick = { showPetPicker = true }
                                )
                            }
                        }
                    }
                    
                    // Date & Time
                    item {
                        FormSection(title = "Date & Time") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SelectionCard(
                                    label = formatDate(selectedDate),
                                    icon = "📅",
                                    onClick = { datePickerDialog.show() },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                SelectionCard(
                                    label = formatTime(selectedTime),
                                    icon = "🕐",
                                    onClick = { timePickerDialog.show() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Notes
                    item {
                        FormSection(title = "Notes (Optional)") {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                placeholder = { Text("Add any additional notes...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = VetInputBackground,
                                    unfocusedContainerColor = VetInputBackground,
                                    focusedBorderColor = VetCanyon,
                                    unfocusedBorderColor = VetStroke
                                ),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 5
                            )
                        }
                    }
                    
                    // Recurring
                    item {
                        FormSection(title = "Recurring") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Repeat event",
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    
                                    Switch(
                                        checked = isRecurring,
                                        onCheckedChange = { isRecurring = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = VetCanyon
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    // Recurrence Type (if recurring)
                    if (isRecurring) {
                        item {
                            FormSection(title = "Repeat Frequency") {
                                SelectionCard(
                                    label = recurrenceType.displayName,
                                    icon = "🔄",
                                    onClick = { showRecurrencePicker = true }
                                )
                            }
                        }
                    }
                    
                    // Save Button
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && selectedPet != null) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        try {
                                            // Combine date and time
                                            val eventDateTime = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                                                set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                                                set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                                                set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                                                set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                                                set(Calendar.SECOND, 0)
                                            }
                                            
                                            // Add to calendar using CalendarManager
                                            // Format description to match iOS: "Pet ID: {petId}\nType: {type}\n{notes}"
                                            val eventId = calendarManager.addCustomEvent(
                                                title = "${selectedEventType.icon} $title - ${selectedPet?.name}",
                                                description = buildString {
                                                    if (notes.isNotBlank()) {
                                                        append(notes)
                                                    }
                                                    if (isRecurring) {
                                                        if (notes.isNotBlank()) append("\n")
                                                        append("Repeats: ${recurrenceType.displayName}")
                                                    }
                                                },
                                                startTime = eventDateTime.timeInMillis,
                                                durationMinutes = 30,
                                                location = "",
                                                recurrence = if (isRecurring) recurrenceType.rule else null,
                                                petId = selectedPet?.id,
                                                eventType = selectedEventType.name.lowercase() // medication, vaccination, etc.
                                            )
                                            
                                            isSaving = false
                                            if (eventId != null) {
                                                // Wait a moment for calendar to persist (like iOS does)
                                                kotlinx.coroutines.delay(100)
                                                
                                                // Reload events after creating (iOS does this automatically)
                                                // This ensures the event appears immediately in the app
                                                val petIdToReload = selectedPet?.id
                                                if (petIdToReload != null) {
                                                    try {
                                                        val updatedEvents = calendarManager.loadEventsForPet(petIdToReload)
                                                        android.util.Log.d("AddCalendarEvent", "Reloaded ${updatedEvents.size} events after creation")
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("AddCalendarEvent", "Failed to reload events: ${e.message}")
                                                    }
                                                }
                                                
                                                // Set flag to trigger reload in PetCalendarScreen
                                                navController.previousBackStackEntry?.savedStateHandle?.set("eventAdded", true)
                                                
                                                showSuccessDialog = true
                                            } else {
                                                // Show error - calendar might not be available
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Failed to save event. Please ensure a calendar account is set up on your device.",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            isSaving = false
                                            android.widget.Toast.makeText(
                                                context,
                                                "Error saving event: ${e.message}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VetCanyon),
                            enabled = title.isNotBlank() && selectedPet != null && !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Default.Check, "Save", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Save Reminder",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
                
                // Dialogs
                if (showEventTypePicker) {
                    EventTypePickerDialog(
                        selectedType = selectedEventType,
                        onTypeSelected = {
                            selectedEventType = it
                            showEventTypePicker = false
                        },
                        onDismiss = { showEventTypePicker = false }
                    )
                }
                
                // Only show pet picker if no pet is pre-selected (petId is null)
                if (showPetPicker && state.pets.isNotEmpty() && petId == null) {
                    PetPickerDialog(
                        pets = state.pets,
                        selectedPet = selectedPet,
                        onPetSelected = {
                            selectedPet = it
                            showPetPicker = false
                        },
                        onDismiss = { showPetPicker = false }
                    )
                }
                
                if (showRecurrencePicker) {
                    RecurrencePickerDialog(
                        selectedRecurrence = recurrenceType,
                        onRecurrenceSelected = {
                            recurrenceType = it
                            showRecurrencePicker = false
                        },
                        onDismiss = { showRecurrencePicker = false }
                    )
                }
                
                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showSuccessDialog = false
                            navController.popBackStack()
                        },
                        icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
                        title = { Text("Reminder Added") },
                        text = { Text("Your pet care reminder has been added to your calendar successfully.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showSuccessDialog = false
                                navController.popBackStack()
                            }) {
                                Text("Done", color = VetCanyon)
                            }
                        }
                    )
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VetCanyon)
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        content()
    }
}

@Composable
private fun SelectionCard(
    label: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (enabled) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = label,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.KeyboardArrowDown, null, tint = TextSecondary)
            }
        }
    } else {
        // Disabled state - non-clickable card
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = CardBackground.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = label,
                    fontSize = 15.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                // No arrow icon for disabled state
            }
        }
    }
}

@Composable
private fun EventTypePickerDialog(
    selectedType: EventType,
    onTypeSelected: (EventType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Event Type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EventType.values().forEach { type ->
                    Card(
                        onClick = { onTypeSelected(type) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (type == selectedType) VetCanyon.copy(alpha = 0.1f) else CardBackground
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = type.icon, fontSize = 20.sp)
                            Text(text = type.displayName, fontSize = 15.sp, color = TextPrimary)
                            if (type == selectedType) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, null, tint = VetCanyon)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PetPickerDialog(
    pets: List<Pet>,
    selectedPet: Pet?,
    onPetSelected: (Pet) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Pet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pets.forEach { pet ->
                    Card(
                        onClick = { onPetSelected(pet) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (pet.id == selectedPet?.id) VetCanyon.copy(alpha = 0.1f) else CardBackground
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = getSpeciesEmoji(pet.species), fontSize = 20.sp)
                            Text(text = pet.name, fontSize = 15.sp, color = TextPrimary)
                            if (pet.id == selectedPet?.id) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, null, tint = VetCanyon)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RecurrencePickerDialog(
    selectedRecurrence: RecurrenceType,
    onRecurrenceSelected: (RecurrenceType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Repeat Frequency") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceType.values().forEach { type ->
                    Card(
                        onClick = { onRecurrenceSelected(type) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (type == selectedRecurrence) VetCanyon.copy(alpha = 0.1f) else CardBackground
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = type.displayName, fontSize = 15.sp, color = TextPrimary)
                            if (type == selectedRecurrence) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, null, tint = VetCanyon)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data Classes
enum class EventType(val displayName: String, val icon: String) {
    MEDICATION("Medication", "💊"),
    VACCINATION("Vaccination", "💉"),
    APPOINTMENT("Vet Appointment", "🏥"),
    REMINDER("Reminder", "🔔"),
    GROOMING("Grooming", "✂️"),
    FEEDING("Feeding Reminder", "🍖"),
    EXERCISE("Exercise", "🏃"),
    CHECKUP("Health Checkup", "🩺"),
    OTHER("Other Reminder", "📝")
}

enum class RecurrenceType(val displayName: String, val rule: String) {
    DAILY("Every Day", "FREQ=DAILY"),
    WEEKLY("Every Week", "FREQ=WEEKLY"),
    BIWEEKLY("Every 2 Weeks", "FREQ=WEEKLY;INTERVAL=2"),
    MONTHLY("Every Month", "FREQ=MONTHLY"),
    YEARLY("Every Year", "FREQ=YEARLY")
}

private fun formatDate(calendar: Calendar): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    return formatter.format(calendar.time)
}

private fun formatTime(calendar: Calendar): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.US)
    return formatter.format(calendar.time)
}

private fun getSpeciesEmoji(species: String): String {
    return when (species.lowercase()) {
        "dog" -> "🐶"
        "cat" -> "🐱"
        "bird" -> "🐦"
        "rabbit" -> "🐰"
        "hamster" -> "🐹"
        "fish" -> "🐠"
        "horse" -> "🐴"
        else -> "🐾"
    }
}

