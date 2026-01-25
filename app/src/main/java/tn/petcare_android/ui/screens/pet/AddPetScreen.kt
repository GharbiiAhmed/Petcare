package tn.petcare_android.ui.screens.pet

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import tn.petcare_android.util.rememberImagePicker
import tn.petcare_android.viewmodel.pet.PetViewModel
import tn.petcare_android.viewmodel.pet.PetViewModelFactory
import tn.petcare_android.viewmodel.pet.PetUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: PetViewModel = viewModel(factory = PetViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Dog") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petGender by remember { mutableStateOf("") }
    var petColor by remember { mutableStateOf("") }
    var petWeight by remember { mutableStateOf("") }
    var petHeight by remember { mutableStateOf("") }
    var petMicrochipId by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }

    // Image upload states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<java.io.File?>(null) }

    // Image picker
    val imagePicker = rememberImagePicker { uri ->
        selectedImageUri = uri
        Log.d("AddPetScreen", "Image selected: $uri")
        // Convert to file immediately
        val file = tn.petcare_android.util.ImageFileHelper.uriToFile(context, uri)
        if (file != null) {
            selectedImageFile = file
            Toast.makeText(context, "Photo selected!", Toast.LENGTH_SHORT).show()
        } else {
            errorMessage = "Failed to process selected image"
            showError = true
            Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle successful pet addition
    LaunchedEffect(uiState) {
        if (uiState is PetUiState.Success) {
            navController.popBackStack()
        }
    }

    fun validateFields(): Boolean {
        return when {
            petName.isBlank() -> {
                errorMessage = "Please enter pet name"
                false
            }
            petAge.isNotBlank() && (petAge.toIntOrNull() == null || petAge.toInt() < 0) -> {
                errorMessage = "Please enter a valid age"
                false
            }
            petWeight.isNotBlank() && petWeight.toDoubleOrNull() == null -> {
                errorMessage = "Please enter a valid weight"
                false
            }
            petHeight.isNotBlank() && petHeight.toDoubleOrNull() == null -> {
                errorMessage = "Please enter a valid height"
                false
            }
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Add New Pet",
                navController = navController
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Photo Upload Section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8C4B4))
                            .clickable { imagePicker() },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri),
                                    contentDescription = "Selected pet photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add photo",
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (selectedImageFile != null) "Photo selected ✓"
                               else "Add Pet Photo (Optional)",
                        fontSize = 14.sp,
                        color = if (selectedImageFile != null) OrangeAccent else TextSecondary
                    )
                }
            }

            // Pet Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Pet Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Name
                        PetInfoTextField(
                            value = petName,
                            onValueChange = {
                                petName = it
                                showError = false
                            },
                            label = "Name *",
                            icon = Icons.Default.Favorite
                        )

                        // Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = showTypeDropdown,
                            onExpandedChange = { showTypeDropdown = !showTypeDropdown }
                        ) {
                            OutlinedTextField(
                                value = petType,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Species *", color = TextSecondary) },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, "Dropdown")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CardBackground,
                                    unfocusedContainerColor = CardBackground,
                                    focusedBorderColor = OrangeAccent,
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = showTypeDropdown,
                                onDismissRequest = { showTypeDropdown = false }
                            ) {
                                listOf("Dog", "Cat", "Bird", "Fish", "Rabbit", "Hamster", "Other").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            petType = type
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Breed
                        PetInfoTextField(
                            value = petBreed,
                            onValueChange = {
                                petBreed = it
                                showError = false
                            },
                            label = "Breed (Optional)",
                            icon = Icons.Default.List
                        )

                        // Age
                        PetInfoTextField(
                            value = petAge,
                            onValueChange = {
                                petAge = it
                                showError = false
                            },
                            label = "Age (years)",
                            icon = Icons.Default.DateRange,
                            keyboardType = KeyboardType.Number
                        )

                        // Gender Dropdown
                        ExposedDropdownMenuBox(
                            expanded = showGenderDropdown,
                            onExpandedChange = { showGenderDropdown = !showGenderDropdown }
                        ) {
                            OutlinedTextField(
                                value = petGender,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Gender (Optional)", color = TextSecondary) },
                                placeholder = { Text("Select gender", color = TextSecondary) },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, "Dropdown")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CardBackground,
                                    unfocusedContainerColor = CardBackground,
                                    focusedBorderColor = OrangeAccent,
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = showGenderDropdown,
                                onDismissRequest = { showGenderDropdown = false }
                            ) {
                                listOf("Male", "Female").forEach { gender ->
                                    DropdownMenuItem(
                                        text = { Text(gender) },
                                        onClick = {
                                            petGender = gender
                                            showGenderDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Color
                        PetInfoTextField(
                            value = petColor,
                            onValueChange = { petColor = it },
                            label = "Color (Optional)",
                            icon = Icons.Default.Star,
                            placeholder = "e.g., Brown, White"
                        )

                        // Weight
                        PetInfoTextField(
                            value = petWeight,
                            onValueChange = { petWeight = it },
                            label = "Weight (kg)",
                            icon = Icons.Default.Info,
                            placeholder = "e.g., 15.5",
                            keyboardType = KeyboardType.Decimal
                        )

                        // Height
                        PetInfoTextField(
                            value = petHeight,
                            onValueChange = { petHeight = it },
                            label = "Height (cm)",
                            icon = Icons.Default.Info,
                            placeholder = "e.g., 45",
                            keyboardType = KeyboardType.Decimal
                        )

                        // Microchip ID
                        PetInfoTextField(
                            value = petMicrochipId,
                            onValueChange = { petMicrochipId = it },
                            label = "Microchip ID (Must be unique)",
                            icon = Icons.Default.Check,
                            placeholder = "e.g., CHIP123456"
                        )
                    }
                }
            }

            // Error Message
            if (showError || uiState is PetUiState.Error) {
                item {
                    Text(
                        text = if (uiState is PetUiState.Error)
                            (uiState as PetUiState.Error).message
                        else
                            errorMessage,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Add Pet Button
            item {
                Button(
                    onClick = {
                        if (validateFields()) {
                            showError = false
                            coroutineScope.launch {
                                viewModel.addPet(
                                    name = petName.trim(),
                                    species = petType.lowercase(),
                                    breed = petBreed.trim().takeIf { it.isNotBlank() },
                                    age = petAge.toDoubleOrNull(),
                                    gender = petGender.takeIf { it.isNotBlank() }?.lowercase(),
                                    color = petColor.trim().takeIf { it.isNotBlank() },
                                    weight = petWeight.toDoubleOrNull(),
                                    height = petHeight.toDoubleOrNull(),
                                    photoFile = selectedImageFile, // Include selected image file
                                    microchipId = petMicrochipId.trim().takeIf { it.isNotBlank() }
                                )
                            }
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent
                    ),
                    enabled = uiState !is PetUiState.Loading
                ) {
                    if (uiState is PetUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Add Pet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetInfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder, color = TextSecondary) }) else null,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            focusedBorderColor = OrangeAccent,
            unfocusedBorderColor = Color(0xFFE0E0E0)
        ),
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType
        )
    )
}

