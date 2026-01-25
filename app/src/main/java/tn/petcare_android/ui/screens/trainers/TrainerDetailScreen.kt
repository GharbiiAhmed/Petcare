package tn.petcare_android.ui.screens.trainers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.presentation.viewmodel.TrainersViewModel
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerDetailScreen(
    navController: NavHostController,
    trainerId: String?,
    viewModel: TrainersViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Trainer Details",
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Trainer Detail Screen\nID: $trainerId",
                fontSize = 16.sp,
                color = TextSecondary
            )
        }
    }
}
