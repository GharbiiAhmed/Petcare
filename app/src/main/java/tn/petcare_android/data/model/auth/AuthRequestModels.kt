package tn.petcare_android.data.model.auth


data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class ResendVerificationRequest(
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)


// Password Management
data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// Email Management
data class ChangeEmailRequest(
    val newEmail: String,
    val password: String
)

data class VerifyNewEmailRequest(
    val newEmail: String,
    val code: String
)

// Google Sign-In
data class GoogleSignInRequest(
    val id_token: String
)

