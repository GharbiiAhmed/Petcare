package tn.petcare_android.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.petcare_android.R
import tn.petcare_android.ui.theme.LoginBackground
import tn.petcare_android.ui.theme.OrangePrimary
import tn.petcare_android.viewmodel.auth.AuthUiState
import tn.petcare_android.viewmodel.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    viewModel: AuthViewModel,
    email: String,
    onVerified: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var code by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val message = (uiState as AuthUiState.Success).message
            if (message.contains("verified", ignoreCase = true)) {
                onVerified()
                viewModel.resetState()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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
                text = "Verify Your Email",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            Text(
                text = "Enter the verification code sent to:",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                text = email,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OrangePrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Verification Code TextField
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.text.length <= 6) code = it },
                label = { Text("Verification Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading,
                placeholder = { Text("Enter 6-digit code") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            when (val state = uiState) {
                is AuthUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                else -> {}
            }

            // Verify Button
            Button(
                onClick = {
                    viewModel.verifyEmail(email, code.text.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4)), // Teal
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading && code.text.length == 6
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "VERIFY", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resend Code
            TextButton(
                onClick = { /* TODO: Resend verification code */ },
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text(
                    "Didn't receive the code? Resend",
                    color = OrangePrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
