package tn.petcare_android.data.repository

import tn.petcare_android.data.api.AuthApi
import tn.petcare_android.data.model.auth.*
import retrofit2.Response

class AuthRepository(private val api: AuthApi) {

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return api.register(request)
    }

    suspend fun verifyEmail(request: VerifyEmailRequest): Response<AuthResponse> {
        return api.verifyEmail(request)
    }

    suspend fun resendVerificationCode(request: ResendVerificationRequest): Response<MessageResponse> {
        return api.resendVerificationCode(request)
    }

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return api.login(request)
    }

    // Password Management
    suspend fun forgotPassword(request: ForgotPasswordRequest): Response<MessageResponse> {
        return api.forgotPassword(request)
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Response<MessageResponse> {
        return api.resetPassword(request)
    }

    suspend fun changePassword(request: ChangePasswordRequest): Response<MessageResponse> {
        return api.changePassword(request)
    }

    // Email Management
    suspend fun changeEmail(request: ChangeEmailRequest): Response<MessageResponse> {
        return api.changeEmail(request)
    }

    suspend fun verifyNewEmail(request: VerifyNewEmailRequest): Response<MessageResponse> {
        return api.verifyNewEmail(request)
    }

    // Google Sign-In
    suspend fun googleSignIn(request: GoogleSignInRequest): Response<AuthResponse> {
        return api.googleSignIn(request)
    }
}
