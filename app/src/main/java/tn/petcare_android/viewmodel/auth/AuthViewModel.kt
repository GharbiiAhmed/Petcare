package tn.petcare_android.viewmodel.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.repository.AuthRepository
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.storage.UserManager
import tn.petcare_android.data.session.SessionManager
import tn.petcare_android.data.model.auth.LoginRequest
import tn.petcare_android.data.model.auth.RegisterRequest
import tn.petcare_android.data.model.auth.VerifyEmailRequest
import tn.petcare_android.util.ValidationUtil

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState
    
    private val sessionManager = SessionManager.getInstance(context)

    fun register(email: String, password: String, name: String, role: String) {
        val validation = ValidationUtil.validateRegistrationInput(email, password, name, role)
        if (!validation.isValid) {
            _uiState.value = AuthUiState.Error(validation.errorMessage ?: "Invalid input")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.register(
                    RegisterRequest(
                        email = email,
                        password = password,
                        name = name,
                        role = role
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Success(
                        response.body()?.message ?: "Registration successful! Check your email for verification code."
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Registration failed.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun verifyEmail(email: String, code: String) {
        val validation = ValidationUtil.validateVerificationInput(code)
        if (!validation.isValid) {
            _uiState.value = AuthUiState.Error(validation.errorMessage ?: "Invalid code")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.verifyEmail(
                    VerifyEmailRequest(email = email, code = code)
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Success(
                        response.body()?.message ?: "Email verified successfully!"
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Verification failed.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun login(email: String, password: String) {
        val validation = ValidationUtil.validateLoginInput(email, password)
        if (!validation.isValid) {
            _uiState.value = AuthUiState.Error(validation.errorMessage ?: "Invalid input")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.tokens != null && body.user != null) {
                        tokenManager.saveTokens(
                            accessToken = body.tokens.accessToken,
                            refreshToken = body.tokens.refreshToken
                        )
                        val userId = body.user.id ?: body.user.email
                        userManager.saveUserId(userId)
                        _uiState.value = AuthUiState.Success("Login successful!")
                    } else {
                        _uiState.value = AuthUiState.Error("Invalid server response.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Login failed.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                sessionManager.clearSession()
                _uiState.value = AuthUiState.Success("Logged out successfully.")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Failed to logout.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    // Password Management
    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.forgotPassword(
                    tn.petcare_android.data.model.auth.ForgotPasswordRequest(email = email)
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Success(
                        response.body()?.message ?: "Password reset code sent to your email"
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Failed to send reset code")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        if (email.isBlank() || code.isBlank() || newPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }

        if (newPassword.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.resetPassword(
                    tn.petcare_android.data.model.auth.ResetPasswordRequest(
                        email = email,
                        code = code,
                        newPassword = newPassword
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Success(
                        response.body()?.message ?: "Password reset successfully!"
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Failed to reset password")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }

        if (newPassword.length < 6) {
            _uiState.value = AuthUiState.Error("New password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.changePassword(
                    tn.petcare_android.data.model.auth.ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                )
                if (response.isSuccessful) {
                    // Clear tokens since backend invalidates them
                    sessionManager.clearSession()
                    _uiState.value = AuthUiState.Success(
                        response.body()?.message ?: "Password changed! Please login again."
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Failed to change password")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    // Email Management
    fun changeEmail(newEmail: String, password: String) {
        if (newEmail.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.changeEmail(
                    tn.petcare_android.data.model.auth.ChangeEmailRequest(
                        newEmail = newEmail,
                        password = password
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Success(
                        response.body()?.message ?: "Verification code sent to new email"
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Failed to change email")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun verifyNewEmail(newEmail: String, code: String) {
        if (newEmail.isBlank() || code.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.verifyNewEmail(
                    tn.petcare_android.data.model.auth.VerifyNewEmailRequest(
                        newEmail = newEmail,
                        code = code
                    )
                )
                if (response.isSuccessful) {
                    // Clear tokens since backend invalidates them
                    sessionManager.clearSession()
                    _uiState.value = AuthUiState.Success(
                        response.body()?.message ?: "Email changed! Please login with new email."
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Failed to verify new email")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    // Google Sign-In
    fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.googleSignIn(
                    tn.petcare_android.data.model.auth.GoogleSignInRequest(id_token = idToken)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.tokens != null && body.user != null) {
                        tokenManager.saveTokens(
                            accessToken = body.tokens.accessToken,
                            refreshToken = body.tokens.refreshToken
                        )
                        val userId = body.user.id ?: body.user.email
                        userManager.saveUserId(userId)
                        sessionManager.setAuthenticated(true)
                        _uiState.value = AuthUiState.Success("Login successful!")
                    } else {
                        _uiState.value = AuthUiState.Error("Invalid server response.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = AuthUiState.Error(errorBody ?: "Google Sign-In failed.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }
}
