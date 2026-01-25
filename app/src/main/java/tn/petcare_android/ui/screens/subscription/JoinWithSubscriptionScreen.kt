package tn.petcare_android.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import tn.petcare_android.data.model.subscription.SubscriptionStatus
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.subscription.SubscriptionUiState
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModel
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModelFactory

/**
 * Join Vet/Sitter/Trainer with Subscription Screen
 * 
 * Flow:
 * 1. User selects role (vet, sitter, or trainer)
 * 2. Reviews subscription details ($30/month)
 * 3. Creates subscription
 * 4. Completes payment via Stripe
 * 5. Subscription activates automatically upon payment success
 * 6. User role upgraded, added to discover list/map
 * 7. Navigate to professional profile setup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinWithSubscriptionScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModelFactory())
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val subscription by viewModel.subscription.collectAsState()
    
    // Load subscription on start to check if user already has one
    LaunchedEffect(Unit) {
        viewModel.getSubscription()
    }
    
    // Check if user already has active subscription
    val hasActiveSubscription = subscription?.subscriptionStatus == SubscriptionStatus.ACTIVE
    val currentRole = subscription?.role?.lowercase()
    
    // If user has active subscription with a role, pre-select it
    LaunchedEffect(currentRole) {
        if (hasActiveSubscription && currentRole != null && currentRole in listOf("vet", "sitter", "trainer", "salon")) {
            selectedRole = currentRole
        }
    }
    
    // Initialize Stripe PaymentSheet (must be at composable scope level)
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Payment successful, subscription activated automatically
                // Navigate to appropriate profile setup based on selected role
                val destination = when (selectedRole) {
                    "vet" -> "join_vet"
                    "sitter" -> "join_sitter"
                    "trainer" -> "join_trainer"
                    "salon" -> "join_salon"
                    else -> "profile"
                }
                navController.navigate(destination) {
                    popUpTo("join_vet_sitter") { inclusive = true }
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
    
    // Handle UI state changes
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
                // Navigate to appropriate profile setup based on selected role
                val destination = when (selectedRole) {
                    "vet" -> "join_vet"
                    "sitter" -> "join_sitter"
                    "trainer" -> "join_trainer"
                    "salon" -> "join_salon"
                    else -> "profile"
                }
                navController.navigate(destination) {
                    popUpTo("join_vet_sitter") { inclusive = true }
                }
                viewModel.resetUiState()
            }
            is SubscriptionUiState.RoleUpdated -> {
                // Role updated successfully, navigate to profile form
                val destination = when (state.role) {
                    "vet" -> "join_vet"
                    "sitter" -> "join_sitter"
                    "trainer" -> "join_trainer"
                    else -> "profile"
                }
                navController.navigate(destination) {
                    popUpTo("join_vet_sitter") { inclusive = true }
                }
                viewModel.resetUiState()
            }
            else -> {}
        }
    }
    
    // Confirm dialog
    if (showConfirmDialog && selectedRole != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { 
                Text(
                    if (hasActiveSubscription && selectedRole != currentRole) {
                        "Change Your Role"
                    } else if (hasActiveSubscription) {
                        "You're Already Subscribed"
                    } else {
                        "Confirm Subscription"
                    }
                ) 
            },
            text = {
                Column {
                    if (hasActiveSubscription && selectedRole != currentRole) {
                        Text("You want to change your role from ${currentRole?.replaceFirstChar { it.uppercase() }} to ${selectedRole!!.replaceFirstChar { it.uppercase() }}.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your subscription will remain active with the new role.")
                    } else if (hasActiveSubscription) {
                        Text("You already have an active subscription as ${currentRole?.replaceFirstChar { it.uppercase() }}.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("You can continue to your profile setup or change your role.")
                    } else {
                        Text("You're about to subscribe as a ${selectedRole!!.replaceFirstChar { it.uppercase() }}.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Price: $${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("After payment, you'll be able to set up your professional profile.")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (hasActiveSubscription && selectedRole != currentRole) {
                            // Update role of existing subscription
                            viewModel.updateSubscriptionRole(selectedRole!!)
                        } else if (!hasActiveSubscription) {
                            // Create new subscription
                            viewModel.createSubscription(selectedRole!!)
                        } else {
                            // Same role, just navigate to profile
                            val destination = when (selectedRole) {
                                "vet" -> "join_vet"
                                "sitter" -> "join_sitter"
                                "trainer" -> "join_trainer"
                                else -> "profile"
                            }
                            navController.navigate(destination) {
                                popUpTo("join_vet_sitter") { inclusive = true }
                            }
                        }
                        showConfirmDialog = false
                    }
                ) {
                    Text(
                        when {
                            hasActiveSubscription && selectedRole != currentRole -> "Change Role"
                            hasActiveSubscription -> "Continue to Profile"
                            else -> "Subscribe"
                        },
                        color = OrangeAccent
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Become a Professional",
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
                // Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            "Professional",
                            modifier = Modifier.size(48.dp),
                            tint = OrangeAccent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Join as a Professional",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (hasActiveSubscription) {
                                "You already have an active subscription. You can change your role below."
                            } else {
                                "Get discovered by pet owners and start receiving booking requests"
                            },
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Show active subscription info if user is already subscribed
                if (hasActiveSubscription) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                "Active",
                                tint = Color(0xFF4CAF50)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Active Subscription",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                if (currentRole != null) {
                                    Text(
                                        text = "Current role: ${currentRole.replaceFirstChar { it.uppercase() }}",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                Text(
                    text = if (hasActiveSubscription) "Change Your Role" else "Choose Your Role",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Vet Option
                RoleCard(
                    title = "Veterinarian",
                    description = "Provide medical care and consultations for pets",
                    icon = Icons.Default.Star,
                    color = Color(0xFF2196F3),
                    isSelected = selectedRole == "vet",
                    onClick = { selectedRole = "vet" }
                )
                
                // Sitter Option
                RoleCard(
                    title = "Pet Sitter",
                    description = "Offer pet sitting, walking, and care services",
                    icon = Icons.Default.Favorite,
                    color = Color(0xFF4CAF50),
                    isSelected = selectedRole == "sitter",
                    onClick = { selectedRole = "sitter" }
                )
                
                // Trainer Option
                RoleCard(
                    title = "Trainer",
                    description = "Provide pet training and behavior modification services",
                    icon = Icons.Default.Person,
                    color = OrangeAccent,
                    isSelected = selectedRole == "trainer",
                    onClick = { selectedRole = "trainer" }
                )
                
                // Salon Option
                RoleCard(
                    title = "Pet Salon",
                    description = "Offer grooming, styling, and beauty services for pets",
                    icon = Icons.Default.Favorite,
                    color = Color(0xFF9C27B0),
                    isSelected = selectedRole == "salon",
                    onClick = { selectedRole = "salon" }
                )
                
                // Subscription Benefits
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Subscription Benefits",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        
                        BenefitRow("✅", "Appear in discover list and map")
                        BenefitRow("✅", "Receive booking requests from pet owners")
                        BenefitRow("✅", "Manage your schedule and appointments")
                        BenefitRow("✅", "Build your professional profile")
                        BenefitRow("✅", "Connect with pet owners via chat")
                        
                        if (!hasActiveSubscription) {
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monthly Subscription",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeAccent
                                )
                            }
                        }
                    }
                }
                
                // Important Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Info",
                            tint = Color(0xFF2196F3)
                        )
                        Column {
                            Text(
                                text = "Email Verification Required",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "After subscribing, you'll receive a verification code via email to activate your subscription.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // Error message
                if (uiState is SubscriptionUiState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = (uiState as SubscriptionUiState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Subscribe/Update Button
                Button(
                    onClick = {
                        if (selectedRole != null) {
                            // If user has active subscription and selected different role, update role
                            if (hasActiveSubscription && selectedRole != currentRole) {
                                showConfirmDialog = true
                            } else if (!hasActiveSubscription) {
                                showConfirmDialog = true
                            } else {
                                // Same role selected, navigate to profile setup
                                val destination = when (selectedRole) {
                                    "vet" -> "join_vet"
                                    "sitter" -> "join_sitter"
                                    "trainer" -> "join_trainer"
                                    else -> "profile"
                                }
                                navController.navigate(destination) {
                                    popUpTo("join_vet_sitter") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    enabled = selectedRole != null && uiState !is SubscriptionUiState.Loading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is SubscriptionUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, if (hasActiveSubscription) "Update" else "Subscribe")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasActiveSubscription && selectedRole != currentRole) {
                                "Update Role"
                            } else if (hasActiveSubscription) {
                                "Continue to Profile"
                            } else {
                                "Subscribe Now"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
    }
}

@Composable
private fun RoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = color
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    "Selected",
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(bullet: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = bullet,
            fontSize = 14.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}

