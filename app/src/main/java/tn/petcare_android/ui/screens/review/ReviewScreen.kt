package tn.petcare_android.ui.screens.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavHostController) {
    // Sample data - In production, these would come from ViewModel or navigation arguments
    val petName = "Eeee"
    val petType = "Bird"
    val petBreed = "Dddddd"
    val petGender = "Male"
    val petDob = "6 Aug 2025"
    val petWeight = "34 kg"
    val petHeight = "34 cm"
    val petMicrochip = "-"

    val vaccinations = "Ztggg34555"
    val allergies = "Eeeeee, Digg"
    val chronicConditions = "Effigie"
    val medications = "Fghijk"

    var currentStep by remember { mutableStateOf(2) } // Review step is active

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Add New Pet",
                navController = navController,
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = OrangeAccent
                        )
                    }
                    IconButton(onClick = { /* settings */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                }
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Progress Steps
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepIndicator(
                        label = "Pet Info",
                        isActive = false,
                        isCompleted = true
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    StepIndicator(
                        label = "Medical",
                        isActive = false,
                        isCompleted = true
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    StepIndicator(
                        label = "Review",
                        isActive = true,
                        isCompleted = false
                    )
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Pet Information
                        SummaryRow(label = "Name", value = petName)
                        SummaryRow(label = "Type", value = petType)
                        SummaryRow(label = "Breed", value = petBreed)
                        SummaryRow(label = "Gender", value = petGender)
                        SummaryRow(label = "Date of Birth", value = petDob)
                        SummaryRow(label = "Weight", value = petWeight)
                        SummaryRow(label = "Height", value = petHeight)
                        SummaryRow(label = "Microchip", value = petMicrochip)
                    }
                }
            }

            // Medical Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Medical",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Medical Information
                        SummaryRow(label = "Vaccinations", value = vaccinations)
                        SummaryRow(label = "Allergies", value = allergies)
                        SummaryRow(label = "Conditions", value = chronicConditions)
                        SummaryRow(label = "Medications", value = medications)
                    }
                }
            }

            // Navigation Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Back Button
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, OrangeAccent),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OrangeAccent
                        )
                    ) {
                        Text(
                            text = "Back",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Save Pet Profile Button
                    Button(
                        onClick = {
                            // Handle saving pet profile
                            navController.navigate("home") {
                                popUpTo("add_pet") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChipSelectedBg
                        )
                    ) {
                        Text(
                            text = "Save Pet Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun StepIndicator(
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Button(
        onClick = { },
        enabled = false,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) OrangeAccent else Color.White,
            contentColor = if (isActive) Color.White else TextSecondary,
            disabledContainerColor = if (isActive) OrangeAccent else Color.White,
            disabledContentColor = if (isActive) Color.White else TextSecondary
        ),
        border = if (!isActive) BorderStroke(1.dp, GreyBorder) else null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    AppTheme {
        ReviewScreen(navController = rememberNavController())
    }
}