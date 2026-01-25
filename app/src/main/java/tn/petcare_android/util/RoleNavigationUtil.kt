package tn.petcare_android.util

import kotlinx.coroutines.flow.firstOrNull
import tn.petcare_android.data.storage.TokenManager

object RoleNavigationUtil {
    /**
     * Get the user's role from JWT token
     */
    suspend fun getUserRole(tokenManager: TokenManager): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (!token.isNullOrBlank()) {
            JwtDecoder.getRoleFromToken(token)
        } else {
            null
        }
    }

    /**
     * Get the route based on user role
     */
    suspend fun getRouteForRole(tokenManager: TokenManager): String {
        val role = getUserRole(tokenManager) ?: "owner"
        return when (role.lowercase()) {
            "admin" -> "admin_main"
            "vet" -> "vet_main"
            "trainer" -> "trainer_main"
            "sitter" -> "sitter_main"
            "salon" -> "salon_main"
            else -> "owner_main" // Default to owner
        }
    }
}







