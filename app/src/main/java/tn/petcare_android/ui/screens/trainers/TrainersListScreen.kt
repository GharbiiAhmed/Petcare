package tn.petcare_android.ui.screens.trainers

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import tn.petcare_android.data.model.trainers.Trainer
import tn.petcare_android.presentation.viewmodel.TrainersViewModel
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainersListScreen(
    navController: NavHostController,
    viewModel: TrainersViewModel = viewModel()
) {
    val trainers by viewModel.trainers.observeAsState(emptyList())
    val isLoading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getAllTrainers()
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Pet Trainers",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        },
        containerColor = PageBackground
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
                        color = VetCanyon
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
                            onClick = { viewModel.getAllTrainers() },
                            colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                trainers.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No trainers available",
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
                        items(trainers.filterIsInstance<Trainer>()) { trainer ->
                            TrainerCard(
                                trainer = trainer,
                                onClick = {
                                    navController.navigate("trainer_detail/${trainer.id ?: ""}")
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
private fun TrainerCard(
    trainer: Trainer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            if (trainer.profileImage != null) {
                AsyncImage(
                    model = trainer.profileImage,
                    contentDescription = "Trainer photo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34C759)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trainer.name?.firstOrNull()?.uppercase() ?: "T",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Trainer info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = trainer.name ?: "Unknown Trainer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                if (trainer.yearsOfExperience != null) {
                    Text(
                        text = "${trainer.yearsOfExperience} years experience",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                
                if (!trainer.specialization.isNullOrEmpty()) {
                    Text(
                        text = trainer.specialization,
                        fontSize = 12.sp,
                        color = Color(0xFF34C759),
                        maxLines = 1
                    )
                }
                
                if (trainer.hourlyRate != null) {
                    Text(
                        text = "$${trainer.hourlyRate}/hr",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VetCanyon
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
