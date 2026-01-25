package tn.petcare_android.ui.screens.discover

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.components.EmbeddedMapView
import tn.petcare_android.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference
) {
    // Notification badge manager
    val badgeViewModel: tn.petcare_android.util.NotificationBadgeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val notificationCount by badgeViewModel.notificationCount.collectAsState()
    val messageCount by badgeViewModel.messageCount.collectAsState()
    
    // Refresh badge counts periodically
    LaunchedEffect(Unit) {
        badgeViewModel.refresh()
        while (true) {
            kotlinx.coroutines.delay(30000)
            badgeViewModel.refresh()
        }
    }
    
    var selectedMode by remember { mutableStateOf(DiscoverMode.FIND_CARE) }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Discover",
                navController = navController,
                showBackButton = false,
                showMenuButton = true,
                onMessagesClick = { navController.navigate("conversations") },
                onNotificationsClick = { navController.navigate("notifications") },
                messageCount = messageCount,
                notificationCount = notificationCount
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ModeSelector(
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it },
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
            )


            when (selectedMode) {
                DiscoverMode.FIND_CARE -> FindCareView(navController = navController)
                DiscoverMode.MAP -> {
                    // Embedded map view (iOS Reference: DiscoverView.swift lines 270-344)
                    EmbeddedMapView(navController = navController)
                }
            }
        }
    }
}

enum class DiscoverMode(val title: String) {
    FIND_CARE("Find Care"),
    MAP("Map")
}

@Composable
private fun ModeSelector(
    selectedMode: DiscoverMode,
    onModeSelected: (DiscoverMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DiscoverMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) VetCanyon else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextPrimary
                )
            }
        }
    }
}


@Composable
private fun FindCareView(navController: NavHostController) {

    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "contentFade"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha },
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }


        item {
            HeroCard(modifier = Modifier.padding(horizontal = 18.dp))
        }


        item {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DestinationCard(
                    title = "Find a Vet",
                    subtitle = "Search trusted veterinary clinics near you, view availability and book appointments.",
                    icon = "🩺",
                    tintColor = VetCanyon,
                    onClick = { navController.navigate("clinic") }
                )

                DestinationCard(
                    title = "Find a Pet Sitter",
                    subtitle = "Browse verified pet sitters, review profiles and arrange stays or daily visits.",
                    icon = "🏠",
                    tintColor = Color(0xFF007AFF),
                    onClick = { navController.navigate("petsitter") }
                )

                DestinationCard(
                    title = "Find a Trainer",
                    subtitle = "Discover certified pet trainers with specialized skills and experience for your pet's needs.",
                    icon = "🎓",
                    tintColor = Color(0xFF34C759),
                    onClick = { navController.navigate("trainers_list") }
                )

                DestinationCard(
                    title = "Find a Pet Salon",
                    subtitle = "Discover professional grooming salons, view services and book appointments for your pet.",
                    icon = "✂️",
                    tintColor = Color(0xFF9C27B0),
                    onClick = { navController.navigate("salons") }
                )

                DestinationCard(
                    title = "Adopt a Pet",
                    subtitle = "Find loving pets looking for their forever home. Browse adoption listings and give a pet a second chance.",
                    icon = "🐾",
                    tintColor = Color(0xFFFF9500),
                    onClick = { navController.navigate("adoption_listings") }
                )
            }
        }


        item {
            CareHighlightsSection(modifier = Modifier.padding(horizontal = 18.dp))
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun HeroCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
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


            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = VetCanyon,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(36.dp)
            )
        }
    }
}

@Composable
private fun DestinationCard(
    title: String,
    subtitle: String,
    icon: String,
    tintColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 22.sp
                )
            }


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


            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CareHighlightsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
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
                icon = "⭐",
                title = "Top-rated clinics",
                detail = "Meet the best-reviewed vets near you, curated from community feedback."
            )

            InsightRow(
                icon = "⏰",
                title = "Same-day visits",
                detail = "Filter for professionals available today for urgent care appointments."
            )

            InsightRow(
                icon = "💬",
                title = "Chat first",
                detail = "Reach out in advance, ask pre-visit questions and share pet histories."
            )
        }
    }
}

@Composable
private fun InsightRow(icon: String, title: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {

            Text(
                text = icon,
                fontSize = 18.sp,
                modifier = Modifier.width(28.dp)
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
