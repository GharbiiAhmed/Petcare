package tn.petcare_android.ui.screens.medical

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

data class MedicalEvent(
    val icon: String,
    val title: String,
    val date: String,
    val description: String
)

data class Medication(
    val icon: String,
    val name: String,
    val dosage: String,
    val info: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    navController: NavHostController,
    petId: String? = null
) {
    // TODO: Use MedicalViewModel to fetch medical history from backend API
    // Example: val viewModel: MedicalViewModel = viewModel(factory = MedicalViewModelFactory(LocalContext.current))
    // val medicalHistory by viewModel.medicalHistory.collectAsState()
    // val medications by viewModel.medications.collectAsState()
    // Use petId to fetch medical history for specific pet
    val medicalHistory = emptyList<MedicalEvent>() // Replace with dynamic data from backend API
    val medications = emptyList<Medication>() // Replace with dynamic data from backend API

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Medical History",
                navController = navController
            )
        },
        containerColor = PageBackground,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(PetAvatarBrown),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐕", fontSize = 60.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Max's Medical History",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Doberman • 3 years old",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                }
            }

            item {
                Text(
                    text = "Timeline",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            itemsIndexed(medicalHistory) { index, event ->
                TimelineItem(event = event, isLast = index == medicalHistory.lastIndex)
            }

            item {
                Text(
                    text = "Current Medications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(medications.size) { index ->
                MedicationCard(medication = medications[index])
            }
        }
    }
}


@Composable
private fun TimelineItem(event: MedicalEvent, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TimelineDot),
                contentAlignment = Alignment.Center
            ) {
                Text(text = event.icon, fontSize = 20.sp)
            }
            if (!isLast) {
                Canvas(modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)) {
                    drawLine(
                        color = TimelineLine,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 4f
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = event.date,
                    fontSize = 12.sp,
                    color = OrangeAccent,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MedicationCard(medication: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = medication.icon, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = medication.dosage,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = medication.info,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

