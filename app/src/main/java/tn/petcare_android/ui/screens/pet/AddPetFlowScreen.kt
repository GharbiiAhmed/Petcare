package tn.petcare_android.ui.screens.pet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.pet.PetViewModel
import tn.petcare_android.viewmodel.pet.PetViewModelFactory
import tn.petcare_android.viewmodel.pet.PetUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetFlowScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: PetViewModel = viewModel(factory = PetViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    var currentStep by remember { mutableStateOf(AddPetStep.PET_INFO) }
    var isSaving by remember { mutableStateOf(false) }


    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Dog") }
    var petBreed by remember { mutableStateOf("") }
    var petGender by remember { mutableStateOf("Male") }
    var petBirthDate by remember { mutableStateOf(Date()) }
    var petWeight by remember { mutableStateOf("") }
    var petHeight by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<java.io.File?>(null) }


    // Medical info states - using sets and lists for better management
    var selectedVaccinations by remember { mutableStateOf<Set<String>>(emptySet()) }
    var customVaccination by remember { mutableStateOf("") }
    var selectedAllergies by remember { mutableStateOf<Set<String>>(emptySet()) }
    var customAllergy by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf<List<MedicationEntry>>(emptyList()) }
    
    // Computed string values for saving
    var vaccinations by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var medicationsString by remember { mutableStateOf("") }


    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        uri?.let {
            val file = tn.petcare_android.util.ImageFileHelper.uriToFile(context, it)
            selectedImageFile = file
        }
    }


    LaunchedEffect(uiState) {
        if (uiState is PetUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopNavBar(
                title = "Add New Pet",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            StepIndicator(
                currentStep = currentStep,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )


            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "stepTransition"
                ) { step ->
                    when (step) {
                        AddPetStep.PET_INFO -> PetInfoStep(
                            name = petName,
                            onNameChange = { petName = it },
                            type = petType,
                            onTypeChange = { petType = it },
                            breed = petBreed,
                            onBreedChange = { petBreed = it },
                            gender = petGender,
                            onGenderChange = { petGender = it },
                            birthDate = petBirthDate,
                            onBirthDateChange = { petBirthDate = it },
                            weight = petWeight,
                            onWeightChange = { petWeight = it },
                            height = petHeight,
                            onHeightChange = { petHeight = it },
                            imageUri = selectedImageUri,
                            onImagePick = {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        AddPetStep.MEDICAL_INFO -> MedicalInfoStep(
                            species = petType,
                            selectedVaccinations = selectedVaccinations,
                            onSelectedVaccinationsChange = { selectedVaccinations = it },
                            customVaccination = customVaccination,
                            onCustomVaccinationChange = { customVaccination = it },
                            selectedAllergies = selectedAllergies,
                            onSelectedAllergiesChange = { selectedAllergies = it },
                            customAllergy = customAllergy,
                            onCustomAllergyChange = { customAllergy = it },
                            conditions = conditions,
                            onConditionsChange = { conditions = it },
                            medications = medications,
                            onMedicationsChange = { medications = it },
                            onUpdateDraft = { vacc, allerg, meds ->
                                vaccinations = vacc
                                allergies = allerg
                                medicationsString = meds
                            }
                        )
                        AddPetStep.REVIEW -> ReviewStep(
                            name = petName,
                            type = petType,
                            breed = petBreed,
                            gender = petGender,
                            birthDate = petBirthDate,
                            weight = petWeight,
                            height = petHeight,
                            vaccinations = vaccinations,
                            allergies = allergies,
                            conditions = conditions,
                            medications = medicationsString,
                            imageUri = selectedImageUri
                        )
                    }
                }
            }


            BottomNavigation(
                currentStep = currentStep,
                canGoNext = canProceed(currentStep, petName, petType),
                isSaving = isSaving,
                onBack = {
                    currentStep = when (currentStep) {
                        AddPetStep.MEDICAL_INFO -> AddPetStep.PET_INFO
                        AddPetStep.REVIEW -> AddPetStep.MEDICAL_INFO
                        else -> currentStep
                    }
                },
                onNext = {
                    currentStep = when (currentStep) {
                        AddPetStep.PET_INFO -> AddPetStep.MEDICAL_INFO
                        AddPetStep.MEDICAL_INFO -> AddPetStep.REVIEW
                        else -> currentStep
                    }
                },
                onSave = {
                    isSaving = true
                    viewModel.addPet(
                        name = petName,
                        species = petType,
                        breed = petBreed.ifBlank { null },
                        age = calculateAge(petBirthDate),
                        gender = petGender,
                        color = null,
                        weight = petWeight.toDoubleOrNull(),
                        height = petHeight.toDoubleOrNull(),
                        microchipId = null,
                        photoFile = selectedImageFile
                    )
                }
            )
        }
    }
}

enum class AddPetStep {
    PET_INFO, MEDICAL_INFO, REVIEW
}

// Medication Entry Data Class
data class MedicationEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val dosage: String = ""
)

@Composable
private fun StepIndicator(currentStep: AddPetStep, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AddPetStep.entries.forEach { step ->
            val isActive = step.ordinal <= currentStep.ordinal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        if (isActive) VetCanyon else VetStroke.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun PetInfoStep(
    name: String,
    onNameChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    birthDate: Date,
    onBirthDateChange: (Date) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    imageUri: Uri?,
    onImagePick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }


        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Pet photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(1.dp, VetStroke, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(VetCanyon.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add photo",
                            tint = VetCanyon,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Button(
                    onClick = onImagePick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VetCanyon,
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, VetCanyon),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import Photo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }


        item {
            SectionCard(title = "Pet Information") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PetTextField(
                        icon = "🐾",
                        placeholder = "Name",
                        value = name,
                        onValueChange = onNameChange
                    )

                    PetDropdownField(
                        title = "Type",
                        value = type,
                        options = listOf("Dog", "Cat", "Bird", "Rabbit", "Sheep", "Goat", "Horse", "Pig", "Chicken", "Duck", "Hamster", "Guinea Pig", "Ferret", "Turtle", "Fish", "Other"),
                        onValueChange = onTypeChange
                    )

                    PetTextField(
                        icon = "🌿",
                        placeholder = "Breed",
                        value = breed,
                        onValueChange = onBreedChange
                    )

                    PetSegmentedControl(
                        title = "Gender",
                        selectedOption = gender,
                        options = listOf("Male", "Female"),
                        onOptionSelected = onGenderChange
                    )

                    PetTextField(
                        icon = "⚖️",
                        placeholder = "Weight (kg)",
                        value = weight,
                        onValueChange = onWeightChange,
                        keyboardType = KeyboardType.Decimal
                    )

                    PetTextField(
                        icon = "📏",
                        placeholder = "Height (cm)",
                        value = height,
                        onValueChange = onHeightChange,
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun MedicalInfoStep(
    species: String,
    selectedVaccinations: Set<String>,
    onSelectedVaccinationsChange: (Set<String>) -> Unit,
    customVaccination: String,
    onCustomVaccinationChange: (String) -> Unit,
    selectedAllergies: Set<String>,
    onSelectedAllergiesChange: (Set<String>) -> Unit,
    customAllergy: String,
    onCustomAllergyChange: (String) -> Unit,
    conditions: String,
    onConditionsChange: (String) -> Unit,
    medications: List<MedicationEntry>,
    onMedicationsChange: (List<MedicationEntry>) -> Unit,
    onUpdateDraft: (String, String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    
    // Update draft whenever any medical info changes
    LaunchedEffect(selectedVaccinations, customVaccination, selectedAllergies, customAllergy, medications) {
        // Update vaccinations
        var allVaccinations = selectedVaccinations.toMutableList()
        if (customVaccination.trim().isNotEmpty()) {
            allVaccinations.add(customVaccination.trim())
        }
        val vaccinationsString = allVaccinations.joinToString(", ")
        
        // Update allergies
        var allAllergies = selectedAllergies.toMutableList()
        if (customAllergy.trim().isNotEmpty()) {
            allAllergies.add(customAllergy.trim())
        }
        val allergiesString = allAllergies.joinToString(", ")
        
        // Update medications
        val validMeds = medications.filter { it.name.isNotEmpty() && it.dosage.isNotEmpty() }
        val medicationsString = validMeds.joinToString(", ") { "${it.name}: ${it.dosage}" }
        
        onUpdateDraft(vaccinationsString, allergiesString, medicationsString)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header with expand/collapse
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Medical Info",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = VetCanyon
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = TextSecondary
                        )
                    }
                    
                    // Expandable content
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Vaccinations Section
                            VaccinationsSection(
                                species = species,
                                selectedVaccinations = selectedVaccinations,
                                onSelectedVaccinationsChange = onSelectedVaccinationsChange,
                                customVaccination = customVaccination,
                                onCustomVaccinationChange = onCustomVaccinationChange
                            )
                            
                            // Allergies Section
                            AllergiesSection(
                                selectedAllergies = selectedAllergies,
                                onSelectedAllergiesChange = onSelectedAllergiesChange,
                                customAllergy = customAllergy,
                                onCustomAllergyChange = onCustomAllergyChange
                            )
                            
                            // Chronic Conditions
                            PetTextField(
                                icon = "🏥",
                                placeholder = "Chronic conditions (comma-separated)",
                                value = conditions,
                                onValueChange = onConditionsChange,
                                singleLine = false
                            )
                            
                            // Medications Section
                            MedicationsSection(
                                medications = medications,
                                onMedicationsChange = onMedicationsChange
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Optional — you can fill this later",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// MARK: - Vaccinations Section
@Composable
private fun VaccinationsSection(
    species: String,
    selectedVaccinations: Set<String>,
    onSelectedVaccinationsChange: (Set<String>) -> Unit,
    customVaccination: String,
    onCustomVaccinationChange: (String) -> Unit
) {
    val suggestedVaccinations = remember(species) {
        when (species.lowercase()) {
            "dog" -> listOf(
                "Rabies",
                "DHPP (Distemper, Hepatitis, Parvovirus, Parainfluenza)",
                "Bordetella",
                "Leptospirosis",
                "Lyme Disease",
                "Canine Influenza"
            )
            "cat" -> listOf(
                "Rabies",
                "FVRCP (Feline Viral Rhinotracheitis, Calicivirus, Panleukopenia)",
                "FeLV (Feline Leukemia)",
                "FIP (Feline Infectious Peritonitis)"
            )
            "bird" -> listOf(
                "Polyomavirus",
                "Pacheco's Disease",
                "Psittacosis"
            )
            else -> listOf("Rabies", "Core Vaccinations")
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💉",
                fontSize = 18.sp
            )
            Text(
                text = "Vaccinations",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VetCanyon
            )
        }
        
        // Suggested vaccinations with checkboxes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = VetInputBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestedVaccinations.forEach { vaccine ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newSet = selectedVaccinations.toMutableSet()
                            if (newSet.contains(vaccine)) {
                                newSet.remove(vaccine)
                            } else {
                                newSet.add(vaccine)
                            }
                            onSelectedVaccinationsChange(newSet)
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                if (selectedVaccinations.contains(vaccine)) 
                                    VetCanyon 
                                else 
                                    Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.5.dp,
                                if (selectedVaccinations.contains(vaccine)) 
                                    VetCanyon 
                                else 
                                    TextSecondary,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedVaccinations.contains(vaccine)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = vaccine,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Custom vaccination input
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Or add custom:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        
        PetTextField(
            icon = "➕",
            placeholder = "Add custom vaccination",
            value = customVaccination,
            onValueChange = onCustomVaccinationChange
        )
    }
}

// MARK: - Allergies Section
@Composable
private fun AllergiesSection(
    selectedAllergies: Set<String>,
    onSelectedAllergiesChange: (Set<String>) -> Unit,
    customAllergy: String,
    onCustomAllergyChange: (String) -> Unit
) {
    val commonAllergies = remember {
        listOf("Chicken", "Beef", "Dairy", "Wheat", "Soy", "Eggs", "Fish", "Corn")
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                fontSize = 18.sp
            )
            Text(
                text = "Allergies",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VetCanyon
            )
        }
        
        // Suggested allergies with checkboxes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = VetInputBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "None" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newSet = mutableSetOf<String>()
                        if (!selectedAllergies.contains("None")) {
                            newSet.add("None")
                        }
                        onSelectedAllergiesChange(newSet)
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (selectedAllergies.contains("None")) 
                                VetCanyon 
                            else 
                                Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.5.dp,
                            if (selectedAllergies.contains("None")) 
                                VetCanyon 
                            else 
                                TextSecondary,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedAllergies.contains("None")) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "None",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = VetStroke.copy(alpha = 0.5f)
            )
            
            // Common allergies
            commonAllergies.forEach { allergy ->
                val isDisabled = selectedAllergies.contains("None")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isDisabled) {
                            val newSet = selectedAllergies.toMutableSet()
                            if (newSet.contains("None")) {
                                newSet.remove("None")
                            }
                            if (newSet.contains(allergy)) {
                                newSet.remove(allergy)
                            } else {
                                newSet.add(allergy)
                            }
                            onSelectedAllergiesChange(newSet)
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                if (selectedAllergies.contains(allergy)) 
                                    VetCanyon 
                                else 
                                    Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.5.dp,
                                if (selectedAllergies.contains(allergy)) 
                                    VetCanyon 
                                else 
                                    TextSecondary.copy(alpha = if (isDisabled) 0.5f else 1f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAllergies.contains(allergy)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = allergy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDisabled) 
                            TextSecondary.copy(alpha = 0.5f) 
                        else 
                            TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Custom allergy input
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Or add custom:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        
        PetTextField(
            icon = "➕",
            placeholder = "Add custom allergy",
            value = customAllergy,
            onValueChange = onCustomAllergyChange,
            enabled = !selectedAllergies.contains("None")
        )
    }
}

// MARK: - Medications Section
@Composable
private fun MedicationsSection(
    medications: List<MedicationEntry>,
    onMedicationsChange: (List<MedicationEntry>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💊",
                    fontSize = 18.sp
                )
                Text(
                    text = "Current Medications",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VetCanyon
                )
            }
            
            IconButton(
                onClick = {
                    onMedicationsChange(medications + MedicationEntry())
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add medication",
                    tint = VetCanyon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        if (medications.isEmpty()) {
            Text(
                text = "No medications added. Tap + to add one.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                medications.forEachIndexed { index, medication ->
                    MedicationEntryRow(
                        medication = medication,
                        onNameChange = { newName ->
                            val updated = medications.toMutableList()
                            updated[index] = medication.copy(name = newName)
                            onMedicationsChange(updated)
                        },
                        onDosageChange = { newDosage ->
                            val updated = medications.toMutableList()
                            updated[index] = medication.copy(dosage = newDosage)
                            onMedicationsChange(updated)
                        },
                        onDelete = {
                            onMedicationsChange(medications.filterIndexed { i, _ -> i != index })
                        }
                    )
                }
            }
        }
    }
}

// MARK: - Medication Entry Row
@Composable
private fun MedicationEntryRow(
    medication: MedicationEntry,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VetInputBackground),
        border = BorderStroke(1.dp, VetStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Medication ${medication.id.take(4)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete medication",
                        tint = ErrorRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            PetTextField(
                icon = "💊",
                placeholder = "Medication name",
                value = medication.name,
                onValueChange = onNameChange
            )
            
            PetTextField(
                icon = "⚖️",
                placeholder = "Dosage (e.g., 500mg twice daily)",
                value = medication.dosage,
                onValueChange = onDosageChange
            )
        }
    }
}

@Composable
private fun ReviewStep(
    name: String,
    type: String,
    breed: String,
    gender: String,
    birthDate: Date,
    weight: String,
    height: String,
    vaccinations: String,
    allergies: String,
    conditions: String,
    medications: String,
    imageUri: Uri?
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }


        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Pet photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(VetCanyon.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐾", fontSize = 48.sp)
                    }
                }

                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "$type${if (breed.isNotBlank()) " • $breed" else ""}",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
        }


        item {
            SectionCard(title = "Basic Information") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReviewRow(label = "Gender", value = gender)
                    ReviewRow(label = "Birth Date", value = dateFormatter.format(birthDate))
                    if (weight.isNotBlank()) ReviewRow(label = "Weight", value = "$weight kg")
                    if (height.isNotBlank()) ReviewRow(label = "Height", value = "$height cm")
                }
            }
        }


        if (vaccinations.isNotBlank() || allergies.isNotBlank() ||
            conditions.isNotBlank() || medications.isNotBlank()) {
            item {
                SectionCard(title = "Medical Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (vaccinations.isNotBlank())
                            ReviewRow(label = "Vaccinations", value = vaccinations)
                        if (allergies.isNotBlank())
                            ReviewRow(label = "Allergies", value = allergies)
                        if (conditions.isNotBlank())
                            ReviewRow(label = "Conditions", value = conditions)
                        if (medications.isNotBlank())
                            ReviewRow(label = "Medications", value = medications)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VetCanyon
            )
            content()
        }
    }
}

@Composable
private fun PetTextField(
    icon: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary) },
        leadingIcon = { Text(text = icon, fontSize = 18.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VetCanyon,
            unfocusedBorderColor = VetStroke,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            disabledBorderColor = VetStroke.copy(alpha = 0.5f),
            disabledContainerColor = CardBackground.copy(alpha = 0.5f)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetDropdownField(
    title: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(title) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VetCanyon,
                unfocusedBorderColor = VetStroke,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PetSegmentedControl(
    title: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Button(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) VetCanyon else CardBackground,
                        contentColor = if (isSelected) Color.White else TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!isSelected) BorderStroke(1.dp, VetStroke) else null
                ) {
                    Text(option, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun BottomNavigation(
    currentStep: AddPetStep,
    canGoNext: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (currentStep != AddPetStep.PET_INFO) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, VetCanyon),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VetCanyon),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Back",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (currentStep != AddPetStep.REVIEW) {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = canGoNext,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VetCanyon,
                    disabledContainerColor = VetCanyon.copy(alpha = 0.35f)
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Next",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VetCanyon,
                    disabledContainerColor = VetCanyon.copy(alpha = 0.6f)
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save Pet Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun canProceed(step: AddPetStep, name: String, type: String): Boolean {
    return when (step) {
        AddPetStep.PET_INFO -> name.isNotBlank() && type.isNotBlank()
        else -> true
    }
}

private fun calculateAge(birthDate: Date): Double {
    val calendar = Calendar.getInstance()
    val birthCalendar = Calendar.getInstance().apply { time = birthDate }

    val years = calendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
    val months = calendar.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
    val days = calendar.get(Calendar.DAY_OF_MONTH) - birthCalendar.get(Calendar.DAY_OF_MONTH)
    
    // Calculate more precise age including months and days
    val totalAge = years + (months / 12.0) + (days / 365.0)
    return if (totalAge < 0) 0.0 else totalAge
}
