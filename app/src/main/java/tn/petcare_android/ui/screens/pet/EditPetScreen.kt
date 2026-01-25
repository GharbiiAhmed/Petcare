package tn.petcare_android.ui.screens.pet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.CardBackground
import tn.petcare_android.ui.theme.PageBackground
import tn.petcare_android.ui.theme.TextPrimary
import tn.petcare_android.ui.theme.TextSecondary
import tn.petcare_android.ui.theme.VetCanyon
import tn.petcare_android.ui.theme.VetStroke
import tn.petcare_android.util.PetUtils
import tn.petcare_android.util.ImageFileHelper
import tn.petcare_android.viewmodel.pet.PetActionState
import tn.petcare_android.viewmodel.pet.PetDetailUiState
import tn.petcare_android.viewmodel.pet.PetDetailViewModel
import tn.petcare_android.viewmodel.pet.PetDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(
    navController: NavHostController,
    petId: String
) {
    val context = LocalContext.current
    val viewModel: PetDetailViewModel = viewModel(factory = PetDetailViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var loadedPet by remember { mutableStateOf<Pet?>(null) }

    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf("Dog") }
    var breed by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("Male") }
    var color by rememberSaveable { mutableStateOf("") }
    var microchip by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }

    var vaccinations by rememberSaveable { mutableStateOf("") }
    var chronicConditions by rememberSaveable { mutableStateOf("") }
    var medications by rememberSaveable { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<java.io.File?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        selectedImageFile = uri?.let { ImageFileHelper.uriToFile(context, it) }
    }

    LaunchedEffect(petId) {
        viewModel.loadPetDetails(petId)
    }

    LaunchedEffect(uiState) {
        if (uiState is PetDetailUiState.Success) {
            val pet = (uiState as PetDetailUiState.Success).pet
            loadedPet = pet
            name = pet.name
            species = pet.species.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            breed = pet.breed.orEmpty()
            gender = pet.gender ?: "Male"
            color = pet.color.orEmpty()
            microchip = pet.microchipId.orEmpty()
            weight = pet.weight?.toString().orEmpty()
            height = pet.height?.toString().orEmpty()
            age = pet.age?.toString() ?: ""

            vaccinations = pet.medicalHistory?.vaccinations?.joinToString(", ") ?: ""
            chronicConditions = pet.medicalHistory?.chronicConditions?.joinToString(", ") ?: ""
            medications = pet.medicalHistory?.currentMedications?.joinToString(", ") { "${it.name}: ${it.dosage}" } ?: ""
        }
    }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is PetActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
                navController.popBackStack()
            }

            is PetActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }

            else -> Unit
        }
    }

    val isSaving = actionState is PetActionState.Loading
    val canSave = name.trim().isNotEmpty() && !isSaving && loadedPet != null

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopNavBar(
                title = "Edit Pet",
                navController = navController,
                actions = {
                    TextButton(
                        onClick = {
                            val currentPet = loadedPet ?: return@TextButton
                            val updatedPet = currentPet.copy(
                                name = name.trim(),
                                species = species.lowercase(),
                                breed = breed.ifBlank { null },
                                gender = gender,
                                color = color.ifBlank { null },
                                microchipId = microchip.ifBlank { null },
                                weight = weight.toDoubleOrNull(),
                                height = height.toDoubleOrNull(),
                                age = age.toDoubleOrNull()?.takeIf { it > 0.0 }
                            )
                            viewModel.updatePet(
                                petId = petId,
                                pet = updatedPet,
                                photoFile = selectedImageFile
                            )
                        },
                        enabled = canSave
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save",
                                color = if (canSave) VetCanyon else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is PetDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VetCanyon)
                }
            }

            is PetDetailUiState.Error -> {
                EditPetErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadPetDetails(petId) },
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is PetDetailUiState.Success -> {
                EditPetContent(
                    modifier = Modifier.padding(paddingValues),
                    name = name,
                    onNameChange = { name = it },
                    species = species,
                    onSpeciesChange = { species = it },
                    breed = breed,
                    onBreedChange = { breed = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    color = color,
                    onColorChange = { color = it },
                    microchip = microchip,
                    onMicrochipChange = { microchip = it },
                    weight = weight,
                    onWeightChange = { weight = it },
                    height = height,
                    onHeightChange = { height = it },
                    age = age,
                    onAgeChange = { age = it },
                    vaccinations = vaccinations,
                    onVaccinationsChange = { vaccinations = it },
                    conditions = chronicConditions,
                    onConditionsChange = { chronicConditions = it },
                    medications = medications,
                    onMedicationsChange = { medications = it },
                    currentPhoto = selectedImageUri ?: state.pet.photo,
                    fallbackEmoji = PetUtils.getPetEmoji(state.pet.species),
                    onPickPhoto = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemovePhoto = {
                        selectedImageUri = null
                        selectedImageFile = null
                    }
                )
            }

            else -> Unit
        }
    }
}

@Composable
private fun EditPetErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPetContent(
    modifier: Modifier = Modifier,
    name: String,
    onNameChange: (String) -> Unit,
    species: String,
    onSpeciesChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    color: String,
    onColorChange: (String) -> Unit,
    microchip: String,
    onMicrochipChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    vaccinations: String,
    onVaccinationsChange: (String) -> Unit,
    conditions: String,
    onConditionsChange: (String) -> Unit,
    medications: String,
    onMedicationsChange: (String) -> Unit,
    currentPhoto: Any?,
    fallbackEmoji: String,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val speciesOptions = listOf("Dog", "Cat", "Bird", "Rabbit", "Other")
    var speciesExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentPhoto != null) {
                Image(
                    painter = rememberAsyncImagePainter(currentPhoto),
                    contentDescription = "Pet photo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(VetCanyon.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(fallbackEmoji, fontSize = 42.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onPickPhoto,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, VetCanyon),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = VetCanyon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Photo", color = VetCanyon, fontWeight = FontWeight.SemiBold)
                }

                if (currentPhoto != null) {
                    OutlinedButton(
                        onClick = onRemovePhoto,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, VetStroke),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove", color = TextSecondary)
                    }
                }
            }
        }

        EditPetSectionCard(title = "Pet Information") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EditPetTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = "Pet name",
                    leadingEmoji = "🐾"
                )

                Box {
                    OutlinedTextField(
                        value = species,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Species") },
                        leadingIcon = { Text("🐶") },
                        trailingIcon = {
                            val icon = if (speciesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                            Icon(
                                imageVector = icon,
                                contentDescription = null
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VetCanyon,
                            unfocusedBorderColor = VetStroke,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { speciesExpanded = !speciesExpanded }
                    )
                    DropdownMenu(
                        expanded = speciesExpanded,
                        onDismissRequest = { speciesExpanded = false }
                    ) {
                        speciesOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onSpeciesChange(option)
                                    speciesExpanded = false
                                }
                            )
                        }
                    }
                }

                EditPetTextField(
                    value = breed,
                    onValueChange = onBreedChange,
                    placeholder = "Breed",
                    leadingEmoji = "🌿"
                )

                GenderSelector(
                    selected = gender,
                    onSelected = onGenderChange
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditPetTextField(
                        value = weight,
                        onValueChange = onWeightChange,
                        placeholder = "Weight (kg)",
                        leadingEmoji = "⚖️",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    )
                    EditPetTextField(
                        value = height,
                        onValueChange = onHeightChange,
                        placeholder = "Height (cm)",
                        leadingEmoji = "📏",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    )
                }

                Column {
                    val ageFloat = age.toFloatOrNull() ?: 0f
                    Text("Age: ${ageFloat.toInt()} yrs", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Slider(
                        value = ageFloat,
                        onValueChange = { onAgeChange(it.toString()) },
                        valueRange = 0f..25f,
                        colors = SliderDefaults.colors(
                            activeTrackColor = VetCanyon,
                            thumbColor = VetCanyon
                        )
                    )
                }

                EditPetTextField(
                    value = color,
                    onValueChange = onColorChange,
                    placeholder = "Color",
                    leadingEmoji = "🎨"
                )

                EditPetTextField(
                    value = microchip,
                    onValueChange = onMicrochipChange,
                    placeholder = "Microchip ID",
                    leadingEmoji = "🔐"
                )
            }
        }

        EditPetSectionCard(title = "Medical History") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EditPetTextField(
                    value = vaccinations,
                    onValueChange = onVaccinationsChange,
                    placeholder = "Vaccinations (comma separated)",
                    leadingEmoji = "💉",
                    singleLine = false
                )

                EditPetTextField(
                    value = conditions,
                    onValueChange = onConditionsChange,
                    placeholder = "Chronic conditions",
                    leadingEmoji = "🩺",
                    singleLine = false
                )

                EditPetTextField(
                    value = medications,
                    onValueChange = onMedicationsChange,
                    placeholder = "Medications (format: name: dosage)",
                    leadingEmoji = "💊",
                    singleLine = false
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EditPetSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            content()
        }
    }
}

@Composable
private fun EditPetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingEmoji: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(placeholder) },
        leadingIcon = { Text(leadingEmoji) },
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 4,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VetCanyon,
            unfocusedBorderColor = VetStroke,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            cursorColor = VetCanyon
        )
    )
}

@Composable
private fun GenderSelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Gender", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Male", "Female").forEach { option ->
                Button(
                    onClick = { onSelected(option) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == option) VetCanyon else CardBackground,
                        contentColor = if (selected == option) Color.White else TextPrimary
                    )
                ) {
                    if (selected == option) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(option)
                }
            }
        }
    }
}

