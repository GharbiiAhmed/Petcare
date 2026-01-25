package tn.petcare_android.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.subscription.SubscriptionUiState
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModel
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModelFactory

/**
 * Subscription Benefits Screen
 * 
 * Shows subscription benefits and pricing
 * User confirms to proceed with payment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBenefitsScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Initialize Stripe PaymentSheet (must be at composable scope level)
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Payment successful - subscription activated automatically via webhook
                // Navigate to subscription_management to activate and choose role
                navController.navigate("subscription_management") {
                    popUpTo("subscription_benefits") { inclusive = true }
                }
                viewModel.resetUiState()
            }
            is PaymentSheetResult.Canceled -> {
                // Payment was cancelled - subscription remains pending
                // User can try again or will see manual activation option
                viewModel.setError("Payment cancelled. You can try again or the subscription will remain pending.")
            }
            is PaymentSheetResult.Failed -> {
                viewModel.setError(result.error.message ?: "Payment failed. Please try again.")
            }
        }
    }
    
    // Handle payment flow
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SubscriptionUiState.PaymentRequired -> {
                // Present Stripe payment sheet
                paymentSheet.presentWithPaymentIntent(
                    state.clientSecret,
                    PaymentSheet.Configuration(
                        merchantDisplayName = "Petcare Pet Care",
                        allowsDelayedPaymentMethods = false
                    )
                )
            }
            is SubscriptionUiState.Success -> {
                // Test mode - subscription activated immediately
                // Navigate to subscription_management to activate and choose role
                navController.navigate("subscription_management") {
                    popUpTo("subscription_benefits") { inclusive = true }
                }
                viewModel.resetUiState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Premium Subscription",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium",
                        modifier = Modifier.size(64.dp),
                        tint = OrangeAccent
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Become a Professional",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlock premium features",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Benefits Section
            Text(
                text = "What You Get:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            BenefitItem(
                icon = Icons.Default.CheckCircle,
                title = "Verified Badge",
                description = "Get a verified badge next to your name"
            )
            
            BenefitItem(
                icon = Icons.Default.Person,
                title = "Professional Profile",
                description = "Choose to become a Veterinarian or Pet Sitter"
            )
            
            BenefitItem(
                icon = Icons.Default.LocationOn,
                title = "Discover Visibility",
                description = "Appear in search results and on the map"
            )
            
            BenefitItem(
                icon = Icons.Default.Star,
                title = "Client Bookings",
                description = "Receive booking requests from pet owners"
            )
            
            BenefitItem(
                icon = Icons.Default.DateRange,
                title = "Schedule Management",
                description = "Manage your availability and appointments"
            )
            
            BenefitItem(
                icon = Icons.Default.Favorite,
                title = "Build Your Business",
                description = "Grow your client base and reputation"
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Subscribe Button
            when (val state = uiState) {
                is SubscriptionUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = OrangeAccent
                    )
                }
                is SubscriptionUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Subscribe Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Subscribe Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Subscription") },
            text = {
                Column {
                    Text("You're about to subscribe to our Premium plan.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Price: $${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("After payment, you'll be able to choose your role (Veterinarian or Pet Sitter) and set up your professional profile.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createSubscription("premium") // Role will be chosen after payment
                        showConfirmDialog = false
                    }
                ) {
                    Text("Continue to Payment", color = OrangeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BenefitItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = OrangeAccent
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
