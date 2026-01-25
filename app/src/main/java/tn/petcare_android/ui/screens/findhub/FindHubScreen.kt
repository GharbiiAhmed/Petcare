package tn.petcare_android.ui.screens.findhub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

/**
 * Find Hub Screen - Unified Navigation to Care Services
 * iOS Reference: FindHubView.swift
 * Landing page for finding vets and pet sitters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindHubScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference
) {
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Find Care",
                showBackButton = false,
                navController = navController,
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Hero Card
            item {
                HeroCard()
            }
            
            // Destination Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DestinationCard(
                        title = "Find a Vet",
                        subtitle = "Search trusted veterinary clinics near you, view availability and book appointments.",
                        icon = Icons.Default.Star, // Using Star instead of LocalHospital
                        tint = VetCanyon,
                        onClick = { navController.navigate("clinic") }
                    )
                    
                    DestinationCard(
                        title = "Find a Pet Sitter",
                        subtitle = "Browse verified pet sitters, review profiles and arrange stays or daily visits.",
                        icon = Icons.Default.Home,
                        tint = Color(0xFF2196F3),
                        onClick = { navController.navigate("petsitter") }
                    )
                    
                    DestinationCard(
                        title = "Find a Pet Salon",
                        subtitle = "Discover professional grooming salons, view services and book appointments for your pet.",
                        icon = Icons.Default.Star,
                        tint = Color(0xFF9C27B0),
                        onClick = { navController.navigate("salons") }
                    )
                }
            }
            
            // Care Highlights Section
            item {
                CareHighlightsSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Find trusted care for every need.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 32.sp
                )
                
                Text(
                    text = "Discover experienced veterinarians, pet sitters, and grooming salons available within your community. Compare profiles, read reviews and stay connected.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
            
            // Top-right icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(36.dp),
                tint = VetCanyon
            )
        }
    }
}

@Composable
private fun DestinationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(tint.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CareHighlightsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Care Highlights",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InsightRow(
                icon = Icons.Default.Star,
                title = "Top-rated clinics",
                detail = "Meet the best-reviewed vets near you, curated from community feedback."
            )
            
            InsightRow(
                icon = Icons.Default.Info,
                title = "Same-day visits",
                detail = "Filter for professionals available today for urgent care appointments."
            )
            
            InsightRow(
                icon = Icons.Default.Search,
                title = "Chat first",
                detail = "Reach out in advance, ask pre-visit questions and share pet histories."
            )
        }
    }
}

@Composable
private fun InsightRow(
    icon: ImageVector,
    title: String,
    detail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VetCanyon,
                modifier = Modifier.size(18.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = detail,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

