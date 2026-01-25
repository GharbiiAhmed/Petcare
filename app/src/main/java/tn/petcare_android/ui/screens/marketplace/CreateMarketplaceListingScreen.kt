package tn.petcare_android.ui.screens.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.data.api.CreateMarketplaceListingRequest
import tn.petcare_android.presentation.viewmodel.MarketplaceViewModel
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMarketplaceListingScreen(
    navController: NavHostController,
    viewModel: MarketplaceViewModel = viewModel()
) {
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isLoading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    // Handle error messages
    LaunchedEffect(error) {
        if (error != null && error!!.contains("success", ignoreCase = true)) {
            showSuccessDialog = true
        }
    }

    // Show success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = { Text("Success!") },
            text = { Text("Your listing has been created successfully.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground,
                    titleContentColor = TextPrimary
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
            Text(
                text = "Pet Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Pet Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VetCanyon,
                    focusedLabelColor = VetCanyon
                )
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species (e.g., Dog, Cat)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VetCanyon,
                    focusedLabelColor = VetCanyon
                )
            )

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VetCanyon,
                    focusedLabelColor = VetCanyon
                )
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VetCanyon,
                    focusedLabelColor = VetCanyon
                )
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (TND)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VetCanyon,
                    focusedLabelColor = VetCanyon
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VetCanyon,
                    focusedLabelColor = VetCanyon
                )
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VetCanyon,
                    focusedLabelColor = VetCanyon
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show error if exists and it's not a success message
            error?.let { errorMsg ->
                if (!errorMsg.contains("success", ignoreCase = true)) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (!isSubmitting && !isLoading) {
                        val request = CreateMarketplaceListingRequest(
                            petName = petName,
                            species = species,
                            breed = if (breed.isNotBlank()) breed else null,
                            age = age.toIntOrNull(),
                            color = null,
                            weight = null,
                            gender = null,
                            description = if (description.isNotBlank()) description else null,
                            price = price.toDoubleOrNull() ?: 0.0,
                            images = if (imageUrl.isNotBlank()) listOf(imageUrl) else null,
                            location = null,
                            latitude = null,
                            longitude = null,
                            vaccinated = null,
                            neutered = null,
                            medicalHistory = null,
                            traits = null
                        )
                        viewModel.createListing(request)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VetCanyon),
                enabled = !isLoading && petName.isNotBlank() && species.isNotBlank() && price.isNotBlank()
            ) {
                Text(
                    text = if (isLoading) "Creating..." else "Create Listing",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
