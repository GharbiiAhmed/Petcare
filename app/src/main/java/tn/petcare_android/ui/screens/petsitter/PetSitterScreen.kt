package tn.petcare_android.ui.screens.petsitter

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.data.model.sitter.SitterCard
import tn.petcare_android.data.model.sitter.ServiceType
import tn.petcare_android.ui.components.LabeledDateField
import tn.petcare_android.ui.components.RadioRow
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.sitter.SitterListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSitterScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    viewModel: SitterListViewModel = viewModel()
) {
    var fromDate by remember { mutableStateOf(Date()) }
    var toDate by remember { mutableStateOf(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) }
    var selectedService by remember { mutableStateOf(ServiceType.AT_HOME) }

    val sitters by viewModel.sitters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load sitters when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadSitters()
    }
    
    // Refresh sitters when subscription becomes active
    val subscriptionActivated by tn.petcare_android.util.SubscriptionManager.subscriptionActivated.collectAsState()
    LaunchedEffect(subscriptionActivated) {
        if (subscriptionActivated) {
            viewModel.loadSitters()
        }
    }


    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "contentFade"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Pet Sitter",
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
                .padding(paddingValues)
                .padding(horizontal = 18.dp)
                .graphicsLayer { this.alpha = alpha },
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            item {
                Text(
                    text = "Find Pet Sitter",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }


            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LabeledDateField(
                        title = "FROM DATE",
                        date = fromDate,
                        onDateChange = { fromDate = it }
                    )

                    LabeledDateField(
                        title = "TO DATE",
                        date = toDate,
                        onDateChange = { toDate = it }
                    )
                }
            }


            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SERVICE TYPE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioRow(
                            title = "At Home",
                            price = "€25/day",
                            isSelected = selectedService == ServiceType.AT_HOME,
                            onClick = { selectedService = ServiceType.AT_HOME }
                        )
                        RadioRow(
                            title = "Visit Only",
                            price = "€15",
                            isSelected = selectedService == ServiceType.VISIT_ONLY,
                            onClick = { selectedService = ServiceType.VISIT_ONLY }
                        )
                        RadioRow(
                            title = "Walking",
                            price = "€20",
                            isSelected = selectedService == ServiceType.WALKING,
                            onClick = { selectedService = ServiceType.WALKING }
                        )
                    }
                }
            }


            item {
                Button(
                    onClick = { 
                        // Navigate to available sitters screen (iOS Reference: PetSitterView.swift line 64)
                        navController.navigate("available_sitters")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
                ) {
                    Text("SEARCH SITTERS", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Available Sitters Section - Show all subscribed sitters
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Available Sitters",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VetCanyon)
                        }
                    } else if (error != null) {
                        Text(
                            text = "Error: $error",
                            fontSize = 14.sp,
                            color = ErrorRed,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (sitters.isEmpty()) {
                        Text(
                            text = "No pet sitters available",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Show all available sitters
                            sitters.forEach { sitter ->
                                SitterRow(
                                    sitter = sitter,
                                    onClick = {
                                        navController.navigate("sitter_profile/${sitter.userId}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SitterRow(
    sitter: SitterCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = sitter.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                // Only show rating/reviews if available
                if (sitter.rating > 0.0 || sitter.reviews > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (sitter.rating > 0.0) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = StarColor
                            )
                            Text(
                                text = String.format("%.1f", sitter.rating),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        if (sitter.reviews > 0) {
                            Text(
                                text = "(${sitter.reviews} ${if (sitter.reviews == 1) "review" else "reviews"})",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .background(CardBackground, CircleShape)
                    .padding(8.dp),
                tint = TextPrimary.copy(alpha = 0.8f)
            )
        }
    }
}
