package tn.petcare_android.ui.screens.adoption

import androidx.compose.foundation.BorderStroke
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
import tn.petcare_android.data.storage.UserManager
import tn.petcare_android.presentation.viewmodel.AdoptionViewModel
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionListingsScreen(
    navController: NavHostController,
    viewModel: AdoptionViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userManager = remember { UserManager(context) }
    val currentUserId by userManager.getUserId().collectAsState(initial = null)
    
    val listings by viewModel.listings.observeAsState(emptyList())
    val isLoading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var listingToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getAllListings()
    }
    
    if (showDeleteDialog && listingToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Listing") },
            text = { Text("Are you sure you want to delete this adoption listing?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        listingToDelete?.let { viewModel.deleteListing(it) }
                        showDeleteDialog = false
                        listingToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Pet Adoption",
                showBackButton = false,
                showMenuButton = true,
                navController = navController
            )
        },
        containerColor = PageBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_adoption_listing") },
                containerColor = Color(0xFF34C759)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Listing",
                    tint = Color.White
                )
            }
        }
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
                        color = Color(0xFF34C759)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = error ?: "Unknown error",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { viewModel.getAllListings() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                        ) {
                            Text("Retry")
                        }
                    }
                }
                listings.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No pets available for adoption",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(listings) { listing ->
                            AdoptionCard(
                                listing = listing,
                                currentUserId = currentUserId,
                                onCardClick = {
                                    navController.navigate("adoption_detail/${listing.id ?: ""}")
                                },
                                onDeleteClick = {
                                    listingToDelete = listing.id
                                    showDeleteDialog = true
                                },
                                onContactClick = {
                                    // Navigate to chat with rescuer
                                    listing.rescuer?.id?.let { rescuerId ->
                                        navController.navigate("messages/$rescuerId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdoptionCard(
    listing: AdoptionListing,
    currentUserId: String?,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onContactClick: () -> Unit
) {
    val isOwner = listing.rescuer?.id == currentUserId
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image
            if (!listing.images.isNullOrEmpty()) {
                AsyncImage(
                    model = listing.images.first(),
                    contentDescription = listing.petName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.petName ?: "Unknown Pet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF34C759).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "FREE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34C759),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        
                        // Action buttons
                        if (isOwner) {
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete listing",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            IconButton(onClick = onContactClick) {
                                Icon(
                                    Icons.Filled.Email,
                                    contentDescription = "Contact rescuer",
                                    tint = Color(0xFF34C759),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (!listing.description.isNullOrEmpty()) {
                    Text(
                        text = listing.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!listing.species.isNullOrEmpty()) {
                        Text(
                            text = listing.species,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .background(
                                    color = VetCanyon.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (!listing.breed.isNullOrEmpty()) {
                        Text(
                            text = listing.breed,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF007AFF).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
