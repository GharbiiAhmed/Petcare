package tn.petcare_android.ui.screens.medical

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalScreen(navController: NavHostController) {
    var vaccinations by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var chronicConditions by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(true) }
    var currentStep by remember { mutableStateOf(1) } // Medical step is active

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
                        isActive = currentStep == 0,
                        isCompleted = currentStep > 0,
                        onClick = { navController.popBackStack() }
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    StepIndicator(
                        label = "Medical",
                        isActive = currentStep == 1,
                        isCompleted = currentStep > 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    StepIndicator(
                        label = "Review",
                        isActive = currentStep == 2,
                        isCompleted = false
                    )
                }
            }

            // Medical Information Card
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
                        // Header with expand/collapse
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Medical Info",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row {
                                IconButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Collapse",
                                        tint = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        }

                        if (isExpanded) {
                            // Vaccinations
                            MedicalTextField(
                                value = vaccinations,
                                onValueChange = { vaccinations = it },
                                label = "Vaccinations",
                                placeholder = "Ztggg34555",
                                icon = Icons.Default.Favorite
                            )

                            // Allergies
                            MedicalTextField(
                                value = allergies,
                                onValueChange = { allergies = it },
                                label = "Allergies",
                                placeholder = "• chicken, • dairy...",
                                icon = Icons.Default.Warning
                            )

                            // Chronic Conditions
                            MedicalTextField(
                                value = chronicConditions,
                                onValueChange = { chronicConditions = it },
                                label = "Chronic conditions",
                                placeholder = "",
                                icon = Icons.Default.CheckCircle
                            )

                            // Current Medication
                            MedicalTextField(
                                value = medication,
                                onValueChange = { medication = it },
                                label = "Current medication + dosage",
                                placeholder = "",
                                icon = Icons.Default.Info
                            )
                        }
                    }
                }
            }

            // Optional Message
            item {
                Text(
                    text = "Optional — you can fill this later",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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

                    // Next Button
                    Button(
                        onClick = { navController.navigate("review") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCD9B7F)
                        )
                    ) {
                        Text(
                            text = "Next",
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
    isCompleted: Boolean,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) OrangeAccent else Color.White,
            contentColor = if (isActive) Color.White else TextSecondary
        ),
        border = if (!isActive) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary, fontSize = 14.sp) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = TextSecondary.copy(alpha = 0.5f)) }
        } else null,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            focusedBorderColor = Color(0xFFE0E0E0),
            unfocusedBorderColor = Color(0xFFE0E0E0)
        ),
        singleLine = true
    )
}

@Suppress("unused") // @Preview(showBackground = true)
@Composable
fun MedicalScreenPreview() {
     AppTheme {
        MedicalScreen(navController = rememberNavController())
    }
}