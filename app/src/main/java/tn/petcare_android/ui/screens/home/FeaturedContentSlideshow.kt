package tn.petcare_android.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.ui.theme.*

@Composable
fun FeaturedContentSlideshow(
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State for each category
    var marketplaceListings by remember { mutableStateOf<List<Any>>(emptyList()) }
    var adoptionListings by remember { mutableStateOf<List<Any>>(emptyList()) }
    var vets by remember { mutableStateOf<List<Any>>(emptyList()) }
    var sitters by remember { mutableStateOf<List<Any>>(emptyList()) }
    var trainers by remember { mutableStateOf<List<Any>>(emptyList()) }
    var salons by remember { mutableStateOf<List<Any>>(emptyList()) }
    
    var isLoading by remember { mutableStateOf(true) }
    
    // Fetch all data
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            try {
                // Fetch marketplace listings
                val marketplaceResponse = RetrofitInstance.marketplaceApi.getAllListings(null)
                if (marketplaceResponse.isSuccessful) {
                    marketplaceListings = marketplaceResponse.body()?.take(10) ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error fetching marketplace: ${e.message}")
            }
            
            try {
                // Fetch adoption listings
                val adoptionResponse = RetrofitInstance.adoptionApi.getAllListings(null)
                if (adoptionResponse.isSuccessful) {
                    adoptionListings = adoptionResponse.body()?.take(10) ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error fetching adoptions: ${e.message}")
            }
            
            try {
                // Fetch vets
                vets = RetrofitInstance.vetSitterApi.getAllVets().take(10)
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error fetching vets: ${e.message}")
            }
            
            try {
                // Fetch sitters
                sitters = RetrofitInstance.vetSitterApi.getAllSitters().take(10)
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error fetching sitters: ${e.message}")
            }
            
            try {
                // Fetch trainers
                val trainersResponse = RetrofitInstance.trainersApi.getAllTrainers()
                if (trainersResponse.isSuccessful) {
                    trainers = trainersResponse.body()?.take(10) ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error fetching trainers: ${e.message}")
            }
            
            try {
                // Fetch salons
                salons = RetrofitInstance.salonApi.getAllSalons().take(10)
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error fetching salons: ${e.message}")
            }
            
            isLoading = false
        }
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Marketplace Section
        if (marketplaceListings.isNotEmpty() || isLoading) {
            FeaturedSection(
                title = "Marketplace",
                icon = "🛍️",
                items = marketplaceListings,
                isLoading = isLoading,
                navController = navController,
                onItemClick = { item ->
                    // Navigate to marketplace listing detail
                    try {
                        val listingId = item.javaClass.getDeclaredField("_id").apply { isAccessible = true }.get(item)?.toString()
                        listingId?.let { navController.navigate("marketplace_listing/$it") }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error navigating to marketplace: ${e.message}")
                    }
                },
                itemCard = { item -> MarketplaceCard(item) }
            )
        }
        
        // Adoptions Section
        if (adoptionListings.isNotEmpty() || isLoading) {
            FeaturedSection(
                title = "Adoptions",
                icon = "🐾",
                items = adoptionListings,
                isLoading = isLoading,
                navController = navController,
                onItemClick = { item ->
                    try {
                        val listingId = item.javaClass.getDeclaredField("_id").apply { isAccessible = true }.get(item)?.toString()
                        listingId?.let { navController.navigate("adoption_listing/$it") }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error navigating to adoption: ${e.message}")
                    }
                },
                itemCard = { item -> AdoptionCard(item) }
            )
        }
        
        // Vets Section
        if (vets.isNotEmpty() || isLoading) {
            FeaturedSection(
                title = "Veterinarians",
                icon = "🏥",
                items = vets,
                isLoading = isLoading,
                navController = navController,
                onItemClick = { item ->
                    try {
                        val vetId = item.javaClass.getDeclaredField("_id").apply { isAccessible = true }.get(item)?.toString()
                        vetId?.let { navController.navigate("vet_detail/$it") }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error navigating to vet: ${e.message}")
                    }
                },
                itemCard = { item -> ProfessionalCard(item, "Vet") }
            )
        }
        
        // Sitters Section
        if (sitters.isNotEmpty() || isLoading) {
            FeaturedSection(
                title = "Pet Sitters",
                icon = "🏠",
                items = sitters,
                isLoading = isLoading,
                navController = navController,
                onItemClick = { item ->
                    try {
                        val sitterId = item.javaClass.getDeclaredField("_id").apply { isAccessible = true }.get(item)?.toString()
                        sitterId?.let { navController.navigate("sitter_detail/$it") }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error navigating to sitter: ${e.message}")
                    }
                },
                itemCard = { item -> ProfessionalCard(item, "Sitter") }
            )
        }
        
        // Trainers Section
        if (trainers.isNotEmpty() || isLoading) {
            FeaturedSection(
                title = "Trainers",
                icon = "🎓",
                items = trainers,
                isLoading = isLoading,
                navController = navController,
                onItemClick = { item ->
                    try {
                        val trainerId = item.javaClass.getDeclaredField("_id").apply { isAccessible = true }.get(item)?.toString()
                        trainerId?.let { navController.navigate("trainer_detail/$it") }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error navigating to trainer: ${e.message}")
                    }
                },
                itemCard = { item -> ProfessionalCard(item, "Trainer") }
            )
        }
        
        // Salons Section
        if (salons.isNotEmpty() || isLoading) {
            FeaturedSection(
                title = "Pet Salons",
                icon = "✂️",
                items = salons,
                isLoading = isLoading,
                navController = navController,
                onItemClick = { item ->
                    try {
                        val salonId = item.javaClass.getDeclaredField("_id").apply { isAccessible = true }.get(item)?.toString()
                        salonId?.let { navController.navigate("salon_detail/$it") }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error navigating to salon: ${e.message}")
                    }
                },
                itemCard = { item -> ProfessionalCard(item, "Salon") }
            )
        }
    }
}

@Composable
private fun FeaturedSection(
    title: String,
    icon: String,
    items: List<Any>,
    isLoading: Boolean,
    navController: NavHostController,
    onItemClick: (Any) -> Unit,
    itemCard: @Composable (Any) -> Unit
) {
    val navControllerLocal = navController
    Column(
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
                    text = icon,
                    fontSize = 20.sp
                )
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            TextButton(
                onClick = {
                    // Navigate to full list based on title
                    when (title) {
                        "Marketplace" -> navControllerLocal.navigate("marketplace_listings")
                        "Adoptions" -> navControllerLocal.navigate("adoption_listings")
                        "Veterinarians" -> navControllerLocal.navigate("vets")
                        "Pet Sitters" -> navControllerLocal.navigate("sitters")
                        "Trainers" -> navControllerLocal.navigate("trainers")
                        "Pet Salons" -> navControllerLocal.navigate("salons")
                    }
                }
            ) {
                Text(
                    text = "See All",
                    fontSize = 14.sp,
                    color = VetCanyon
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = VetCanyon
                )
            }
        }
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = VetCanyon,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            items.isEmpty() -> {
                // Empty state - don't show section
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items) { item ->
                        itemCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceCard(item: Any) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛍️",
                    fontSize = 40.sp
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Marketplace Item",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "View Details",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AdoptionCard(item: Any) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐾",
                    fontSize = 40.sp
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Adoption Pet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Find a Home",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ProfessionalCard(item: Any, type: String) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile image placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(VetCanyon.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (type) {
                        "Vet" -> "🏥"
                        "Sitter" -> "🏠"
                        "Trainer" -> "🎓"
                        "Salon" -> "✂️"
                        else -> "👤"
                    },
                    fontSize = 30.sp
                )
            }
            
            Text(
                text = "Professional",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = type,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
