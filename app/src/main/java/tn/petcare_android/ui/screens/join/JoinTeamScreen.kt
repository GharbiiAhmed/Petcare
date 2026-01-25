package tn.petcare_android.ui.screens.join

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

/**
 * JoinTeamView matching iOS JoinTeamView.swift
 * Unified screen for choosing between Pet Sitter or Vet roles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTeamScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Join Our Team",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Grow with vet.tn",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Choose your role to get started.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
            
            // Join Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                JoinCard(
                    emoji = "🧑‍🍼",
                    title = "Join as Pet Sitter",
                    blurb = "Offer trusted care for pets near you. Set your availability, receive bookings, and get paid securely through the app.",
                    bullets = listOf(
                        "Flexible schedule",
                        "In-app chat & bookings",
                        "Secure payouts"
                    ),
                    cta = "Become a Pet Sitter",
                    accent = VetCanyon,
                    onClick = {
                        navController.navigate("join_sitter")
                    }
                )
                
                JoinCard(
                    emoji = "🩺",
                    title = "Join as Veterinary",
                    blurb = "Reach pet owners, manage appointments, and grow your clinic with our tools and 24/7 support.",
                    bullets = listOf(
                        "Appointments & reminders",
                        "Clinic profile & reviews",
                        "Dashboard & analytics"
                    ),
                    cta = "Register Clinic",
                    accent = VetCanyon,
                    onClick = {
                        navController.navigate("join_vet")
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun JoinCard(
    emoji: String,
    title: String,
    blurb: String,
    bullets: List<String>,
    cta: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with emoji and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = emoji,
                    fontSize = 28.sp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = blurb,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Bullet points
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                bullets.forEach { bullet ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(accent)
                        )
                        Text(
                            text = bullet,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
            
            // CTA Button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = cta,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

