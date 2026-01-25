package tn.petcare_android.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModel
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModelFactory

/**
 * Email Verification Screen - DEPRECATED
 * 
 * Email verification is no longer required for subscriptions.
 * Subscriptions are activated automatically upon successful payment.
 * This screen now just redirects to the profile/manage subscription screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModelFactory())
) {
    // Redirect to profile since email verification is no longer needed
    LaunchedEffect(Unit) {
        navController.navigate("profile") {
            popUpTo("email_verification") { inclusive = true }
        }
    }
    
    // Show a simple message while redirecting
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Redirecting...")
        }
    }
}
