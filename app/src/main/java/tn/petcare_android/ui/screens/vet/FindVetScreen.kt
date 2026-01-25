package tn.petcare_android.ui.screens.vet

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import tn.petcare_android.data.model.vet.VetCard
import tn.petcare_android.data.model.vet.VetSort
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.vet.VetListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindVetScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    viewModel: VetListViewModel = viewModel()
) {
    var activeSort by remember { mutableStateOf(VetSort.SPECIALTY) }
    var showOnlyOpen by remember { mutableStateOf(false) }

    val vets by viewModel.vets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Refresh vets list when subscription becomes active
    val subscriptionActivated by tn.petcare_android.util.SubscriptionManager.subscriptionActivated.collectAsState()
    LaunchedEffect(subscriptionActivated) {
        if (subscriptionActivated) {
            viewModel.loadVets()
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
                title = "Find a Vet",
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
                .graphicsLayer { this.alpha = alpha },
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {

            item {
                Text(
                    text = "Find a Vet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }


            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SortChip(
                        title = "Specialty",
                        isActive = activeSort == VetSort.SPECIALTY,
                        onClick = { activeSort = VetSort.SPECIALTY }
                    )
                    SortChip(
                        title = "Distance",
                        isActive = activeSort == VetSort.DISTANCE,
                        onClick = { activeSort = VetSort.DISTANCE }
                    )
                    SortChip(
                        title = "24/7",
                        isActive = activeSort == VetSort.ALL_DAY,
                        onClick = { activeSort = VetSort.ALL_DAY }
                    )

                    Spacer(modifier = Modifier.weight(1f))


                    OutlinedButton(
                        onClick = { showOnlyOpen = !showOnlyOpen },
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(
                            1.dp,
                            if (showOnlyOpen) Color(0xFF10B981) else VetStroke
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showOnlyOpen) Color(0xFF10B981).copy(alpha = 0.12f) else CardBackground,
                            contentColor = if (showOnlyOpen) Color(0xFF059669) else TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (showOnlyOpen) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "Open",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }


            if (isLoading && vets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VetCanyon)
                    }
                }
            }


            if (!error.isNullOrEmpty() && vets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Error loading vets",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = error!!,
                                fontSize = 13.sp,
                                color = Color(0xFFB91C1C)
                            )
                            Button(
                                onClick = { viewModel.loadVets() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VetCanyon
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }


            items(filterAndSort(vets, activeSort, showOnlyOpen)) { vet ->
                VetListRow(
                    vet = vet,
                    onClick = { navController.navigate("vet_profile/${vet.userId}") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


@Composable
private fun SortChip(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) VetCanyon.copy(alpha = 0.14f) else CardBackground,
            contentColor = if (isActive) VetCanyon else TextPrimary
        ),
        border = BorderStroke(1.dp, if (isActive) VetCanyon else VetStroke),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VetListRow(
    vet: VetCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(vet.tint.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vet.emoji,
                    fontSize = 22.sp
                )
            }


            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = vet.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = vet.specialties.joinToString(", "),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "• ${String.format("%.1f", vet.rating)}★",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "(${vet.reviews})",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VetCanyon,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", vet.distanceKm)} km",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VetCanyon
                        )
                    }

                    StatusPill(
                        text = if (vet.isOpen) "Open" else "Closed",
                        backgroundColor = if (vet.isOpen) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                        textColor = if (vet.isOpen) Color(0xFF059669) else Color(0xFFEF4444)
                    )

                    if (vet.is247) {
                        StatusPill(
                            text = "24/7",
                            backgroundColor = VetCanyon.copy(alpha = 0.14f),
                            textColor = VetCanyon
                        )
                    }
                }
            }


            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
                    .then(
                        Modifier.clip(CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}


private fun filterAndSort(
    vets: List<VetCard>,
    sort: VetSort,
    showOnlyOpen: Boolean
): List<VetCard> {
    var result = vets


    if (showOnlyOpen) {
        result = result.filter { it.isOpen }
    }


    result = when (sort) {
        VetSort.SPECIALTY -> result.sortedBy { it.name }
        VetSort.DISTANCE -> result.sortedBy { it.distanceKm }
        VetSort.ALL_DAY -> result.sortedWith(
            compareBy({ if (it.is247) 0 else 1 }, { it.distanceKm })
        )
    }

    return result
}
