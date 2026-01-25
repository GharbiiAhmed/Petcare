package tn.petcare_android.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tn.petcare_android.R
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.auth.AuthUiState
import tn.petcare_android.viewmodel.auth.AuthViewModel
import tn.petcare_android.util.GoogleSignInHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToVerify: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleSignInHelper = remember { GoogleSignInHelper(context) }

    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val message = (uiState as AuthUiState.Success).message
            // If it's a Google Sign-In success, go to home
            if (message.contains("Login", ignoreCase = true) ||
                message.contains("successful", ignoreCase = true)) {
                onNavigateToHome()
            } else {
                // Regular registration - go to verify
                onNavigateToVerify(email.text.trim())
            }
            viewModel.resetState()
        }
    }

    // Show error dialog for Google Sign-In
    if (googleSignInError != null) {
        AlertDialog(
            onDismissRequest = { googleSignInError = null },
            title = { Text("Google Sign-Up Failed") },
            text = { Text(googleSignInError ?: "") },
            confirmButton = {
                TextButton(onClick = { googleSignInError = null }) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Logo
            Image(
                painter = painterResource(id = R.drawable.logo_cat_dog),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            // App Name
            Text(
                text = "Petcare",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )

            Text(
                text = "Create Your Account",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Name TextField
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password TextField
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading,
                isError = confirmPassword.text.isNotEmpty() && password.text != confirmPassword.text
            )

            if (confirmPassword.text.isNotEmpty() && password.text != confirmPassword.text) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selection
            Text(
                text = "Select Your Role",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Role Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Owner Role
                RoleChip(
                    role = "owner",
                    label = "Pet Owner",
                    icon = "🐾",
                    isSelected = selectedRole == "owner",
                    onClick = { selectedRole = "owner" },
                    modifier = Modifier.weight(1f)
                )
                // Pet Sitter Role
                RoleChip(
                    role = "sitter",
                    label = "Pet Sitter",
                    icon = "🏠",
                    isSelected = selectedRole == "sitter",
                    onClick = { selectedRole = "sitter" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vet Role
                RoleChip(
                    role = "vet",
                    label = "Veterinarian",
                    icon = "🏥",
                    isSelected = selectedRole == "vet",
                    onClick = { selectedRole = "vet" },
                    modifier = Modifier.weight(1f),
                    requiresApproval = true
                )
                // Trainer Role
                RoleChip(
                    role = "trainer",
                    label = "Trainer",
                    icon = "🎓",
                    isSelected = selectedRole == "trainer",
                    onClick = { selectedRole = "trainer" },
                    modifier = Modifier.weight(1f),
                    requiresApproval = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Salon Role
                RoleChip(
                    role = "salon",
                    label = "Pet Salon",
                    icon = "✂️",
                    isSelected = selectedRole == "salon",
                    onClick = { selectedRole = "salon" },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedRole == "vet" || selectedRole == "trainer") {
                Text(
                    text = "⚠️ This role requires admin approval after registration",
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            when (val state = uiState) {
                is AuthUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                else -> {}
            }

            // Register Button
            Button(
                onClick = {
                    viewModel.register(
                        email.text.trim(),
                        password.text,
                        name.text,
                        selectedRole ?: "owner"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4)), // Teal
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading &&
                         name.text.isNotBlank() &&
                         email.text.isNotBlank() &&
                         password.text.isNotBlank() &&
                         confirmPassword.text.isNotBlank() &&
                         password.text == confirmPassword.text &&
                         selectedRole != null
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "SIGN UP", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Already have an account?", color = Color.Gray)
                TextButton(onClick = onNavigateBack) {
                    Text("Sign In", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            HorizontalDivider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Google Sign Up Button
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val result = googleSignInHelper.signIn()
                        result.onSuccess { idToken ->
                            viewModel.googleSignIn(idToken)
                        }.onFailure { error ->
                            // Show user-friendly error dialog
                            googleSignInError = error.message ?: "Google Sign-Up failed. Please try again."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = uiState !is AuthUiState.Loading).copy(
                    brush = SolidColor(OrangePrimary)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangePrimary
                ),
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text(
                    text = "SIGN UP WITH GOOGLE",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RoleChip(
    role: String,
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    requiresApproval: Boolean = false
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4ECDC4).copy(alpha = 0.2f) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF4ECDC4) else Color.LightGray
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF4ECDC4) else Color.Black,
                textAlign = TextAlign.Center
            )
            if (requiresApproval) {
                Text(
                    text = "Requires approval",
                    fontSize = 9.sp,
                    color = Color(0xFFFF9800),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
