package tn.petcare_android.ui.screens.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import tn.petcare_android.data.model.marketplace.MarketplaceListing
import tn.petcare_android.presentation.viewmodel.MarketplaceViewModel
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceDetailScreen(
    navController: NavHostController,
    listingId: String?,
    viewModel: MarketplaceViewModel = viewModel()
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
                title = "Listing Details",
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
                                        .height(250.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Pet Name
                            Text(
                                text = listing.petName ?: "Unknown Pet",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // Price
                            Text(
                                text = "${listing.price} TND",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VetCanyon
                            )

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
                                            text = "Description",
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

                            // Contact Button
                            Button(
                                onClick = { /* TODO: Implement contact functionality */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
                            ) {
                                Text("Contact Seller", modifier = Modifier.padding(8.dp))
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
