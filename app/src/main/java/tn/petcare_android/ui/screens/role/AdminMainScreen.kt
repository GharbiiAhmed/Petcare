package tn.petcare_android.ui.screens.role

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.petcare_android.data.model.auth.User
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.components.NavigationDrawerOverlay
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.admin.AdminViewModel
import tn.petcare_android.viewmodel.admin.AdminViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState
import tn.petcare_android.viewmodel.admin.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    navController: androidx.navigation.NavHostController,
    themePreference: ThemePreference,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showApproveDialog by remember { mutableStateOf<User?>(null) }
    var showRejectDialog by remember { mutableStateOf<User?>(null) }
    var rejectNotes by remember { mutableStateOf("") }
    var showSettingsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPendingApprovals()
        viewModel.loadAllUsers()
        profileViewModel.loadProfile()
    }

    val userName = (profileUiState as? ProfileUiState.Success)?.user?.name
    val userImage = (profileUiState as? ProfileUiState.Success)?.user?.profileImage
    val userRole = (profileUiState as? ProfileUiState.Success)?.user?.role ?: "admin"

    // Observe drawer state to conditionally render overlay
    val isDrawerOpen by tn.petcare_android.ui.components.DrawerState.isExpanded

    // Callback to handle sidebar menu item clicks
    val onSidebarItemClick: (String) -> Unit = { menuItem ->
        when (menuItem) {
            "Dashboard" -> selectedTab = 0
            "Pending Approvals" -> selectedTab = 0
            "All Users" -> selectedTab = 1
            "Reports" -> {
                // TODO: Add reports tab or screen
                selectedTab = 0
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopNavBar(
                    title = "Admin Dashboard",
                    navController = navController,
                    showBackButton = false,
                    showMenuButton = true,
                    onSettingsClick = {
                        // Close drawer if open when clicking settings
                        tn.petcare_android.ui.components.DrawerState.isExpanded.value = false
                        showSettingsMenu = true
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Pending Approvals (${pendingApprovals.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("All Users (${allUsers.size})") }
                    )
                }

                // Content
                when (selectedTab) {
                    0 -> PendingApprovalsTab(
                        pendingApprovals = pendingApprovals,
                        onApprove = { user -> showApproveDialog = user },
                        onReject = { user -> showRejectDialog = user },
                        isLoading = uiState is AdminUiState.Loading
                    )
                    1 -> AllUsersTab(
                        users = allUsers,
                        isLoading = uiState is AdminUiState.Loading
                    )
                }
            }
        }

        // Approve Dialog
        showApproveDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showApproveDialog = null },
            title = { Text("Approve ${user.role.replaceFirstChar { it.uppercase() }} Role?") },
            text = {
                Column {
                    Text("Approve ${user.name}'s request to become a ${user.role}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user.email}", fontSize = 14.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.approveRole(user.id ?: "")
                        showApproveDialog = null
                    }
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reject Dialog
    showRejectDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showRejectDialog = null; rejectNotes = "" },
            title = { Text("Reject ${user.role.replaceFirstChar { it.uppercase() }} Role?") },
            text = {
                Column {
                    Text("Reject ${user.name}'s request to become a ${user.role}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user.email}", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectNotes,
                        onValueChange = { rejectNotes = it },
                        label = { Text("Rejection reason (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rejectRole(user.id ?: "", rejectNotes)
                        showRejectDialog = null
                        rejectNotes = ""
                    }
                ) {
                    Text("Reject", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = null; rejectNotes = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success/Error messages
    LaunchedEffect(uiState) {
        val currentState = uiState
        when (currentState) {
            is AdminUiState.Success -> {
                viewModel.loadPendingApprovals()
                viewModel.loadAllUsers()
                android.widget.Toast.makeText(
                    context,
                    currentState.message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                viewModel.resetState()
            }
            is AdminUiState.Error -> {
                android.widget.Toast.makeText(
                    context,
                    currentState.message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                viewModel.resetState()
            }
            else -> { /* Do nothing for Loading or Idle */ }
        }
    }

        // Navigation Drawer - Only render when open to prevent blocking clicks
        if (isDrawerOpen) {
            NavigationDrawerOverlay(
                navController = navController,
                userName = userName,
                userImage = userImage,
                role = userRole,
                onSettingsClick = {
                    showSettingsMenu = true
                },
                onMenuItemClick = onSidebarItemClick
            )
        }

        // Settings Menu Dialog
        if (showSettingsMenu) {
            AlertDialog(
                onDismissRequest = { showSettingsMenu = false },
                title = { Text("Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showSettingsMenu = false
                                onNavigateToChangePassword()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Password")
                        }
                        TextButton(
                            onClick = {
                                showSettingsMenu = false
                                onNavigateToChangeEmail()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Email")
                        }
                        HorizontalDivider()
                        TextButton(
                            onClick = {
                                showSettingsMenu = false
                                onLogout()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsMenu = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun PendingApprovalsTab(
    pendingApprovals: List<User>,
    onApprove: (User) -> Unit,
    onReject: (User) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (pendingApprovals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No pending approvals", fontSize = 16.sp, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pendingApprovals) { user ->
            PendingApprovalCard(
                user = user,
                onApprove = { onApprove(user) },
                onReject = { onReject(user) }
            )
        }
    }
}

@Composable
private fun PendingApprovalCard(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Requesting ${user.role.replaceFirstChar { it.uppercase() }} role",
                            fontSize = 14.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
private fun AllUsersTab(
    users: List<User>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No users found", fontSize = 16.sp, color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users) { user ->
            UserCard(user = user)
        }
    }
}

@Composable
private fun UserCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (user.role.lowercase()) {
                            "admin" -> Color(0xFFFF5722)
                            "vet" -> Color(0xFF2196F3)
                            "trainer" -> Color(0xFF4CAF50)
                            "sitter" -> Color(0xFFFF9800)
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (user.roleApprovalStatus == "pending") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFF9800),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PENDING",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

