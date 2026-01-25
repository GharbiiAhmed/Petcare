package tn.petcare_android.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import tn.petcare_android.data.model.subscription.SubscriptionStatus
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.subscription.SubscriptionUiState
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModel
import tn.petcare_android.viewmodel.subscription.SubscriptionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Subscription Management Screen
 * iOS Reference: STRIPE_SUBSCRIPTION_IMPLEMENTATION.md
 * 
 * Displays current subscription status and allows:
 * - View subscription details
 * - Cancel subscription
 * - Reactivate subscription
 * - Renew subscription
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionManagementScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModelFactory())
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val subscription by viewModel.subscription.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val showExpirationAlert by viewModel.showExpirationAlert.collectAsState()
    val expirationMessage by viewModel.expirationMessage.collectAsState()
    
    // Dialogs
    var showCancelDialog by remember { mutableStateOf(false) }
    var showReactivateDialog by remember { mutableStateOf(false) }
    var showRenewDialog by remember { mutableStateOf(false) }
    var lastSuccessMessage by remember { mutableStateOf<String?>(null) }
    
    // Load subscription on start
    LaunchedEffect(Unit) {
        viewModel.getSubscription()
    }
    
    // Handle success messages from operations (not from simple fetches)
    LaunchedEffect(uiState) {
        if (uiState is SubscriptionUiState.Success) {
            val message = (uiState as SubscriptionUiState.Success).message
            // Only show toast and reload if there's a message (indicating an operation, not just a fetch)
            if (!message.isNullOrEmpty() && message != lastSuccessMessage) {
                lastSuccessMessage = message
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                // Reload subscription data after operation
                kotlinx.coroutines.delay(500) // Small delay to let backend update
                viewModel.getSubscription()
            }
        }
    }
    
    // Cancel dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Subscription?") },
            text = {
                Text("Your subscription will remain active until ${formatDate(subscription?.currentPeriodEnd)}. After that, your role will be downgraded to owner and you'll be removed from the discover list.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelSubscription()
                        showCancelDialog = false
                    }
                ) {
                    Text("Cancel Subscription", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Subscription")
                }
            }
        )
    }
    
    // Reactivate dialog
    if (showReactivateDialog) {
        AlertDialog(
            onDismissRequest = { showReactivateDialog = false },
            title = { Text("Reactivate Subscription?") },
            text = {
                Text("Your subscription will continue and you'll keep your ${subscription?.role} status.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reactivateSubscription()
                        showReactivateDialog = false
                    }
                ) {
                    Text("Reactivate", color = OrangeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Renew dialog
    if (showRenewDialog) {
        AlertDialog(
            onDismissRequest = { showRenewDialog = false },
            title = { Text("Renew Subscription?") },
            text = {
                Text("Your subscription will be extended for another month ($${SubscriptionViewModel.SUBSCRIPTION_PRICE}).")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renewSubscription()
                        showRenewDialog = false
                    }
                ) {
                    Text("Renew", color = OrangeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Expiration alert
    if (showExpirationAlert && expirationMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExpirationAlert() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subscription Alert")
                }
            },
            text = { Text(expirationMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissExpirationAlert() }) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Subscription",
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
            when (uiState) {
                is SubscriptionUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OrangeAccent
                    )
                }
                is SubscriptionUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Error",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (uiState as SubscriptionUiState.Error).message,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.getSubscription() },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    if (subscription == null || subscription?.subscriptionStatus == SubscriptionStatus.NONE) {
                        NoSubscriptionContent(navController)
                    } else {
                        SubscriptionContent(
                            subscription = subscription!!,
                            viewModel = viewModel,
                            navController = navController,
                            onCancel = { showCancelDialog = true },
                            onReactivate = { showReactivateDialog = true },
                            onRenew = { showRenewDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoSubscriptionContent(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            "No subscription",
            modifier = Modifier.size(80.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Active Subscription",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Subscribe as a vet or pet sitter to appear in the discover list and receive booking requests.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("join_vet_sitter") },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp)
        ) {
            Text("Become a Vet or Sitter", fontSize = 16.sp)
        }
    }
}

@Composable
private fun SubscriptionContent(
    subscription: tn.petcare_android.data.model.subscription.Subscription,
    viewModel: SubscriptionViewModel,
    navController: NavHostController,
    onCancel: () -> Unit,
    onReactivate: () -> Unit,
    onRenew: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var selectedNewRole by remember { mutableStateOf<String?>(null) }
    
    // Get current user to check approval status
    val profileViewModel: tn.petcare_android.viewmodel.profile.ProfileViewModel = 
        androidx.lifecycle.viewmodel.compose.viewModel(factory = tn.petcare_android.viewmodel.profile.ProfileViewModelFactory(context))
    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentUser = (profileUiState as? tn.petcare_android.viewmodel.profile.ProfileUiState.Success)?.user
    val roleApprovalStatus = currentUser?.roleApprovalStatus ?: "approved"
    
    // Load profile to get approval status
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = getStatusBackgroundColor(subscription.subscriptionStatus)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = getStatusIcon(subscription.subscriptionStatus),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = getStatusColor(subscription.subscriptionStatus)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = getStatusText(subscription.subscriptionStatus),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(subscription.subscriptionStatus)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subscription.role.uppercase(),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
        
        // Role Approval Status Card (if pending or rejected)
        if (subscription.subscriptionStatus == SubscriptionStatus.ACTIVE && 
            (roleApprovalStatus == "pending" || roleApprovalStatus == "rejected")) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (roleApprovalStatus == "pending") 
                        Color(0xFFFFF3CD) else Color(0xFFFFE0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (roleApprovalStatus == "pending") Icons.Default.Info else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (roleApprovalStatus == "pending") Color(0xFFFF9800) else Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (roleApprovalStatus == "pending") 
                                    "Role Approval Pending" else "Role Approval Rejected",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = if (roleApprovalStatus == "pending") 
                                "Your ${subscription.role} role is pending admin approval. You'll be notified once approved." 
                            else "Your ${subscription.role} role request was rejected. Please contact support for more information.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        currentUser?.roleApprovalNotes?.let { notes ->
                            if (notes.isNotBlank()) {
                                Text(
                                    text = "Notes: $notes",
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
        }
        
        // Role Switching Card (for active subscriptions)
        if (subscription.subscriptionStatus == SubscriptionStatus.ACTIVE) {
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
                        text = "Switch Role",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Change your professional role. Vet and Trainer require admin approval.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    
                    // Role options
                    RoleOption(
                        role = "vet",
                        label = "Veterinarian",
                        icon = "🏥",
                        isSelected = subscription.role == "vet",
                        onClick = {
                            if (subscription.role != "vet") {
                                selectedNewRole = "vet"
                                showRoleSwitchDialog = true
                            }
                        }
                    )
                    RoleOption(
                        role = "trainer",
                        label = "Trainer",
                        icon = "🎓",
                        isSelected = subscription.role == "trainer",
                        onClick = {
                            if (subscription.role != "trainer") {
                                selectedNewRole = "trainer"
                                showRoleSwitchDialog = true
                            }
                        }
                    )
                    RoleOption(
                        role = "sitter",
                        label = "Pet Sitter",
                        icon = "🐾",
                        isSelected = subscription.role == "sitter",
                        onClick = {
                            if (subscription.role != "sitter") {
                                selectedNewRole = "sitter"
                                showRoleSwitchDialog = true
                            }
                        }
                    )
                }
            }
        }
        
        // Role Switch Confirmation Dialog
        if (showRoleSwitchDialog && selectedNewRole != null) {
            AlertDialog(
                onDismissRequest = { 
                    showRoleSwitchDialog = false
                    selectedNewRole = null
                },
                title = { Text("Switch Role?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("You're about to change your role from ${subscription.role.replaceFirstChar { it.uppercase() }} to ${selectedNewRole!!.replaceFirstChar { it.uppercase() }}.")
                        if (selectedNewRole == "vet" || selectedNewRole == "trainer") {
                            Text(
                                "Note: ${selectedNewRole!!.replaceFirstChar { it.uppercase() }} role requires admin approval. Your role will be pending until approved.",
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                "Pet Sitter role is approved immediately - no admin approval needed.",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedNewRole?.let { role ->
                                viewModel.updateSubscriptionRole(role)
                            }
                            showRoleSwitchDialog = false
                            selectedNewRole = null
                        }
                    ) {
                        Text("Switch")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRoleSwitchDialog = false
                            selectedNewRole = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Details Card
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
                    text = "Subscription Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                
                DetailRow(
                    label = "Plan",
                    value = "${subscription.role.replaceFirstChar { it.uppercase() }} - Monthly"
                )
                DetailRow(
                    label = "Price",
                    value = "$${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month"
                )
                DetailRow(
                    label = "Current Period Start",
                    value = formatDate(subscription.currentPeriodStart)
                )
                DetailRow(
                    label = "Current Period End",
                    value = formatDate(subscription.currentPeriodEnd)
                )
                
                subscription.daysUntilExpiration?.let { days ->
                    DetailRow(
                        label = "Days Until Expiration",
                        value = "$days day${if (days != 1) "s" else ""}",
                        valueColor = if (days <= 7) Color.Red else TextPrimary
                    )
                }
                
                if (subscription.cancelAtPeriodEnd == true) {
                    DetailRow(
                        label = "Cancellation",
                        value = "Scheduled at period end",
                        valueColor = Color.Red
                    )
                }
            }
        }
        
        // Actions
        when (subscription.subscriptionStatus) {
            SubscriptionStatus.ACTIVE -> {
                // Edit Professional Profile Button
                if (subscription.role == "vet" || subscription.role == "sitter" || subscription.role == "trainer") {
                    Button(
                        onClick = {
                            val route = when (subscription.role) {
                                "vet" -> "edit_vet_profile"
                                "sitter" -> "edit_sitter_profile"
                                "trainer" -> "trainer_detail/${subscription.userId}" // Navigate to trainer detail/edit
                                else -> null
                            }
                            route?.let { navController.navigate(it) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Icon(Icons.Default.Edit, "Edit Profile")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit ${subscription.role.replaceFirstChar { it.uppercase() }} Profile", fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (subscription.willExpireSoon) {
                    Button(
                        onClick = onRenew,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Icon(Icons.Default.Refresh, "Renew")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Renew Subscription", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Close, "Cancel")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Subscription", fontSize = 16.sp)
                }
            }
            
            SubscriptionStatus.CANCELED -> {
                // Canceled subscriptions cannot be reactivated, only re-subscribed
                // Show message and subscribe again button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                "Info",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subscription Canceled",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This subscription has been permanently canceled. To become a professional again, you'll need to subscribe and set up a new profile.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subscribe Again button
                Button(
                    onClick = {
                        // Navigate to join subscription screen to choose vet or sitter
                        navController.navigate("join_vet_sitter") {
                            popUpTo("subscription_management") { inclusive = false }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Default.Add, "Subscribe Again")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subscribe Again", fontSize = 16.sp)
                }
            }
            
            SubscriptionStatus.EXPIRED, SubscriptionStatus.EXPIRES_SOON -> {
                Button(
                    onClick = onRenew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Default.Refresh, "Renew")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Renew Subscription", fontSize = 16.sp)
                }
            }
            
            SubscriptionStatus.PENDING -> {
                // Pending subscriptions should auto-activate via webhook
                // Show message and refresh button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "⏳ Payment Processing",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your payment is being processed. Your subscription will activate automatically once payment is confirmed.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Refresh button to check if webhook has activated the subscription
                Button(
                    onClick = { viewModel.getSubscription() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Default.Refresh, "Refresh")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Status", fontSize = 16.sp)
                }
            }
            
            else -> {}
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

private fun getStatusColor(status: SubscriptionStatus): Color {
    return when (status) {
        SubscriptionStatus.ACTIVE -> Color(0xFF4CAF50)
        SubscriptionStatus.EXPIRES_SOON -> Color(0xFFFF9800)
        SubscriptionStatus.CANCELED -> Color.Gray
        SubscriptionStatus.EXPIRED -> Color.Red
        SubscriptionStatus.PENDING -> Color(0xFFFF9800)
        SubscriptionStatus.NONE -> Color.Gray
    }
}

private fun getStatusBackgroundColor(status: SubscriptionStatus): Color {
    return getStatusColor(status).copy(alpha = 0.1f)
}

private fun getStatusIcon(status: SubscriptionStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        SubscriptionStatus.ACTIVE -> Icons.Default.CheckCircle
        SubscriptionStatus.EXPIRES_SOON -> Icons.Default.Warning
        SubscriptionStatus.CANCELED -> Icons.Default.Close
        SubscriptionStatus.EXPIRED -> Icons.Default.Info
        SubscriptionStatus.PENDING -> Icons.Default.Warning
        SubscriptionStatus.NONE -> Icons.Default.Info
    }
}

private fun getStatusText(status: SubscriptionStatus): String {
    return when (status) {
        SubscriptionStatus.ACTIVE -> "Active"
        SubscriptionStatus.EXPIRES_SOON -> "Expires Soon"
        SubscriptionStatus.CANCELED -> "Cancelled"
        SubscriptionStatus.EXPIRED -> "Expired"
        SubscriptionStatus.PENDING -> "Pending Verification"
        SubscriptionStatus.NONE -> "No Subscription"
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return "N/A")
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return "N/A")
        } catch (e: Exception) {
            "N/A"
        }
    }
}

@Composable
private fun RoleOption(
    role: String,
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) OrangeAccent.copy(alpha = 0.1f) else CardBackground
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) OrangeAccent else Color.LightGray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = TextPrimary
                    )
                    if (role == "vet" || role == "trainer") {
                        Text(
                            text = "Requires admin approval",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    "Selected",
                    tint = OrangeAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}