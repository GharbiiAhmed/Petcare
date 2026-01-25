package tn.petcare_android.ui.screens.petsitter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.data.model.sitter.SitterCard
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.sitter.SitterListViewModel

/**
 * Available Sitters Screen
 * iOS Reference: AvailableSittersView.swift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableSittersScreen(
    navController: NavHostController,
    viewModel: SitterListViewModel = viewModel()
) {
    val sitters by viewModel.sitters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Refresh sitters list when subscription becomes active
    val subscriptionActivated by tn.petcare_android.util.SubscriptionManager.subscriptionActivated.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadSitters()
    }
    
    LaunchedEffect(subscriptionActivated) {
        if (subscriptionActivated) {
            viewModel.loadSitters()
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Available Sitters",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Available Sitters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                )
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VetCanyon)
                        }
                    }
                }
                error != null -> {
                    item {
                        Text(
                            text = "Error: $error",
                            fontSize = 14.sp,
                            color = ErrorRed,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                sitters.isEmpty() -> {
                    item {
                        Text(
                            text = "No pet sitters available",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
                else -> {
                    items(sitters) { sitter ->
                        SitterCardView(
                            sitter = sitter,
                            onViewProfile = {
                                navController.navigate("sitter_profile/${sitter.userId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SitterCardView(
    sitter: SitterCard,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(sitter.tint, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (sitter.emoji.isNotEmpty()) {
                        Text(
                            text = sitter.emoji,
                            fontSize = 22.sp
                        )
                    } else {
                        // Show first letter of name as fallback
                        Text(
                            text = sitter.name.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = sitter.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    if (sitter.service.isNotEmpty()) {
                        Text(
                            text = sitter.service,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    if (sitter.description.isNotEmpty()) {
                        Text(
                            text = sitter.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }

                // Rating badge - only show if rating exists
                if (sitter.rating > 0.0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF4CAF50).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = String.format("%.1f", sitter.rating),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // View Profile Button
            Button(
                onClick = onViewProfile,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VetCanyon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "VIEW PROFILE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

