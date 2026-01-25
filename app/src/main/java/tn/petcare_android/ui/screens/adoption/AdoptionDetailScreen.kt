package tn.petcare_android.ui.screens.adoption

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import tn.petcare_android.data.model.adoption.AdoptionListing
import tn.petcare_android.presentation.viewmodel.AdoptionViewModel
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionDetailScreen(
    navController: NavHostController,
    listingId: String?,
    viewModel: AdoptionViewModel = viewModel()
) {
    val selectedListing by viewModel.selectedListing.observeAsState()
    val isLoading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    LaunchedEffect(listingId) {
        listingId?.let {
            viewModel.getListing(it)
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Adoption Details",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
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
                        color = VetCanyon
                    )
                }
                error != null && selectedListing == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Failed to load listing",
                            color = TextSecondary
                        )
                    }
                }
                selectedListing != null -> {
                    val listing = selectedListing!!
                    Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Images
                            if (!listing.images.isNullOrEmpty()) {
                                AsyncImage(
                                    model = listing.images.firstOrNull(),
                                    contentDescription = listing.petName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Pet Name with FREE badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = listing.petName ?: "Unknown Pet",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "FREE",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF34C759),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            // Location
                            listing.location?.let { loc ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = VetCanyon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = loc,
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            // Details Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardBackground)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DetailRow("Species", listing.species ?: "Unknown")
                                    listing.breed?.let { breed -> DetailRow("Breed", breed) }
                                    listing.age?.let { age -> DetailRow("Age", "$age years") }
                                    listing.gender?.let { gender -> DetailRow("Gender", gender) }
                                    listing.color?.let { color -> DetailRow("Color", color) }
                                    if (listing.vaccinated == true) {
                                        DetailRow("Vaccinated", "✓ Yes")
                                    }
                                    if (listing.neutered == true) {
                                        DetailRow("Neutered/Spayed", "✓ Yes")
                                    }
                                }
                            }

                            // Description
                            listing.description?.let { desc ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "About",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = desc,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            // Adoption Requirements
                            listing.adoptionRequirements?.let { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Adoption Requirements",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = req,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            // Apply Button
                            Button(
                                onClick = { /* TODO: Implement application functionality */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                            ) {
                                Text("Apply to Adopt", modifier = Modifier.padding(8.dp))
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        Text(
            text = value,
            color = TextPrimary
        )
    }
}
