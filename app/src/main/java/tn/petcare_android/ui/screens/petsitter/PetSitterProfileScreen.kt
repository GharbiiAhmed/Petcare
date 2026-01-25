package tn.petcare_android.ui.screens.petsitter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
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
import tn.petcare_android.viewmodel.sitter.SitterProfileViewModel

@Composable
fun PetSitterProfileScreen(
    navController: NavHostController,
    sitterId: String?,
    viewModel: SitterProfileViewModel = viewModel()
) {
    val sitter by viewModel.sitter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load sitter data when screen opens
    LaunchedEffect(sitterId) {
        sitterId?.let { viewModel.loadSitter(it) }
    }

    Scaffold(
        topBar = { SitterProfileTopBar(navController, sitter?.name ?: "Pet Sitter Profile") },
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
                        CircularProgressIndicator(color = Color(0xFF10B981))
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
                            onClick = { sitterId?.let { viewModel.retry(it) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                // Success state
                sitter != null -> {
                    SitterProfileContent(
                        navController = navController,
                        sitter = sitter!!
                    )
                }
            }
        }
    }
}

@Composable
private fun SitterProfileContent(
    navController: NavHostController,
    sitter: AppUser
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
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐾",
                        fontSize = 40.sp
                    )
                }

                // Name
                Text(
                    text = sitter.name ?: "Pet Sitter",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Services offered
                Text(
                    text = sitter.services?.joinToString(" · ") ?: "Pet Care Specialist",
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
                        text = "4.9",
                        backgroundColor = CardBackground,
                        textColor = TextPrimary,
                        borderColor = GreyBorder
                    )

                    // Hourly rate
                    sitter.hourlyRate?.let { rate ->
                        BadgeChip(
                            icon = "💰",
                            text = "$rate/hr",
                            backgroundColor = CardBackground,
                            textColor = TextPrimary,
                            borderColor = GreyBorder
                        )
                    }

                    // Weekend availability
                    if (sitter.availableWeekends == true) {
                        BadgeChip(
                            icon = "📅",
                            text = "Weekends",
                            backgroundColor = Color(0xFF10B981).copy(alpha = 0.18f),
                            textColor = Color(0xFF10B981),
                            borderColor = Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        // About Section
        item {
            SectionCard(title = "About") {
                Text(
                    text = sitter.bio ?: "Experienced pet sitter providing loving care for your furry friends.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }

        // Services Offered
        item {
            SectionCard(title = "Services Offered") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val services = sitter.services ?: listOf("Pet Sitting", "Dog Walking", "Pet Boarding")
                    
                    services.forEach { service ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = service,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Experience & Skills
        item {
            SectionCard(title = "Experience & Skills") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExperienceItem("🏡", sitter.canHostPets == true, "Can host pets at home")
                    ExperienceItem("📅", sitter.availableWeekends == true, "Weekend availability")
                    ExperienceItem("⭐", true, "Experienced pet care professional")
                    ExperienceItem("💝", true, "Loving and caring approach")
                }
            }
        }

        // Availability
        item {
            SectionCard(title = "Availability") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val availability = buildList {
                        add("Mon–Fri: Generally Available")
                        if (sitter.availableWeekends == true) {
                            add("Weekends: Available")
                        }
                        add("Flexible scheduling options")
                    }
                    
                    availability.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = item,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Location Section
        if (sitter.city != null || sitter.country != null) {
            item {
                SectionCard(title = "Location") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = listOfNotNull(sitter.city, sitter.country).joinToString(", "),
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Contact Buttons
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        // Navigate to booking create screen for pet sitter
                        navController.navigate("booking_create/${sitter.id}/${sitter.name}/sitter")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Book Service",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { navController.navigate("chat/${sitter.id}/${sitter.name ?: "Sitter"}") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Message",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExperienceItem(icon: String, available: Boolean, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (available) TextPrimary else TextSecondary,
            fontWeight = if (available) FontWeight.Medium else FontWeight.Normal
        )
        if (available) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SitterProfileTopBar(navController: NavHostController, sitterName: String) {
    TopAppBar(
        title = {
            Text(
                sitterName,
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
fun PetSitterProfileScreenPreview() {
    AppTheme {
        PetSitterProfileScreen(
            navController = rememberNavController(),
            sitterId = "1"
        )
    }
}
