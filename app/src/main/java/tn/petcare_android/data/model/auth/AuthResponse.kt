package tn.petcare_android.data.model.auth

import tn.petcare_android.data.model.auth.User

data class AuthResponse(
    val message: String?,
    val tokens: Tokens? = null,
    val user: User? = null
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)

data class MessageResponse(
    val message: String
)

