package tn.petcare_android.ui.screens.vet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.petcare_android.data.model.auth.AppUser
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.vet.VetProfileViewModel


// Data classes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetProfileScreen(
    navController: NavHostController,
    vetId: String? = null,
    viewModel: VetProfileViewModel = viewModel()
) {
    val vet by viewModel.vet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Load vet data when screen opens
    LaunchedEffect(vetId) {
        vetId?.let { viewModel.loadVet(it) }
    }

    Scaffold(
        topBar = { VetProfileTopBar(navController, vet?.name ?: "Vet Profile") },
        containerColor = PageBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VetCanyon)
                    }
                }
                
                // Error state
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error loading profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = error!!,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { vetId?.let { viewModel.retry(it) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VetCanyon
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                // Success state
                vet != null -> {
                    VetProfileContent(
                        navController = navController,
                        vet = vet!!
                    )
                }
            }
        }
    }
}

@Composable
private fun VetProfileContent(
    navController: NavHostController,
    vet: AppUser
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 40.dp)
    ) {
        // Header Section
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(VetCanyon.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧑‍⚕️",
                        fontSize = 40.sp
                    )
                }

                // Name
                Text(
                    text = vet.vetClinicName ?: vet.name ?: "Veterinarian",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Role
                Text(
                    text = vet.vetSpecializations?.joinToString(" · ") ?: "Veterinary Specialist",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                // Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Rating Badge
                    BadgeChip(
                        icon = "⭐",
                        text = "4.8",
                        backgroundColor = CardBackground,
                        textColor = TextPrimary,
                        borderColor = GreyBorder
                    )

                    // Years of experience
                    vet.vetYearsOfExperience?.let { years ->
                        BadgeChip(
                            icon = "📅",
                            text = "$years years",
                            backgroundColor = CardBackground,
                            textColor = TextPrimary,
                            borderColor = GreyBorder
                        )
                    }

                    // 24/7 Badge
                    if (vet.vetEmergencyAvailable == true) {
                        BadgeChip(
                            icon = "⚡",
                            text = "24/7",
                            backgroundColor = OrangeAccent.copy(alpha = 0.18f),
                            textColor = OrangeAccent,
                            borderColor = OrangeAccent
                        )
                    }
                }
            }
        }

        // About Section
        item {
            SectionCard(title = "About") {
                Text(
                    text = vet.vetBio ?: "Experienced veterinarian providing quality care for your pets.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }

        // Services & Pricing Section
        item {
            SectionCard(title = "Services & Pricing") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val services = listOf(
                        VetService("Cabinet", "35€"),
                        VetService("Home Visit", "60€"),
                        VetService("Video Call", "25€")
                    )
                    
                    services.forEachIndexed { index, service ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = service.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = service.price,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrangeAccent
                            )
                        }

                        if (index < services.size - 1) {
                            HorizontalDivider(color = GreyBorder)
                        }
                    }
                }
            }
        }

        // Working Hours Section
        item {
            SectionCard(title = "Working Hours") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val hours = if (vet.vetEmergencyAvailable == true) {
                        listOf(
                            "Mon–Sat: 9:00 AM – 6:00 PM",
                            "Sun: 10:00 AM – 4:00 PM",
                            "24/7 Emergency Available"
                        )
                    } else {
                        listOf(
                            "Mon–Fri: 9:00 AM – 6:00 PM",
                            "Sat: 10:00 AM – 2:00 PM",
                            "Sun: Closed"
                        )
                    }
                    
                    hours.forEach { hour ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = VetCanyon,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = hour,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Location Section
        vet.vetAddress?.let { address ->
            item {
                SectionCard(title = "Location") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VetCanyon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = address,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // License info
        vet.vetLicenseNumber?.let { license ->
            item {
                SectionCard(title = "License") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VetCanyon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "License: $license",
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Contact Button
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        // Navigate to booking create screen
                        navController.navigate("booking_create/${vet.id}/${vet.name}/vet")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VetCanyon),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Book Appointment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { navController.navigate("chat/${vet.id}/${vet.name ?: "Vet"}") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = BorderStroke(1.dp, VetCanyon),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = VetCanyon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Message",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VetCanyon
                    )
                }
            }
        }
    }
}

// Data class for services
data class VetService(val label: String, val price: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VetProfileTopBar(navController: NavHostController, vetName: String) {
    TopAppBar(
        title = {
            Text(
                vetName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = { /* notifications */ }) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground)
                        .border(1.dp, GreyBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* settings */ }) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground)
                        .border(1.dp, GreyBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun BadgeChip(
    icon: String,
    text: String,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 11.sp
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GreyBorder, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VetProfileScreenPreview() {
    AppTheme {
        VetProfileScreen(
            navController = rememberNavController(),
            vetId = "1"
        )
    }
}