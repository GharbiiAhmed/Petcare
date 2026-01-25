package tn.petcare_android.ui.screens.petdetail

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.ui.theme.OrangeAccent
import tn.petcare_android.ui.theme.TextSecondary
import tn.petcare_android.util.rememberImagePicker

/**
 * Dialogs for Pet Detail Screen
 * Extracted from main screen file for better organization
 */

/**
 * Dialog for editing pet information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetDialog(
    pet: Pet,
    onDismiss: () -> Unit,
    onSave: (Pet, java.io.File?) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(pet.name) }
    var breed by remember { mutableStateOf(pet.breed ?: "") }
    var age by remember { mutableStateOf(pet.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(pet.gender ?: "") }
    var color by remember { mutableStateOf(pet.color ?: "") }
    var weight by remember { mutableStateOf(pet.weight?.toString() ?: "") }
    var height by remember { mutableStateOf(pet.height?.toString() ?: "") }
    var microchipId by remember { mutableStateOf(pet.microchipId ?: "") }

    // Image states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<java.io.File?>(null) }

    // Image picker
    val imagePicker = rememberImagePicker { uri ->
        selectedImageUri = uri
        // Convert to file immediately
        val file = tn.petcare_android.util.ImageFileHelper.uriToFile(context, uri)
        selectedImageFile = file
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit ${pet.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Photo Upload Section
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
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
                                pet.photo != null -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(pet.photo),
                                        contentDescription = "Pet photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add photo",
                                        tint = OrangeAccent,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (selectedImageFile != null) "Photo selected ✓" else "Tap to change photo",
                            fontSize = 12.sp,
                            color = if (selectedImageFile != null) OrangeAccent else TextSecondary
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg, Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                item {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm, Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                item {
                    OutlinedTextField(
                        value = microchipId,
                        onValueChange = { microchipId = it },
                        label = { Text("Microchip ID (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedPet = pet.copy(
                        name = name,
                        breed = breed.takeIf { it.isNotBlank() },
                        age = age.toDoubleOrNull(),
                        gender = gender.takeIf { it.isNotBlank() },
                        color = color.takeIf { it.isNotBlank() },
                        weight = weight.toDoubleOrNull(),
                        height = height.toDoubleOrNull(),
                        microchipId = microchipId.takeIf { it.isNotBlank() }
                    )
                    onSave(updatedPet, selectedImageFile)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

/**
 * Confirmation dialog for deleting a pet
 */
@Composable
fun DeleteConfirmationDialog(
    petName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("⚠️", fontSize = 48.sp)
        },
        title = {
            Text(
                text = "Delete $petName?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete $petName? This action cannot be undone.",
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

