package tn.petcare_android.data.model.sitter

import androidx.compose.ui.graphics.Color
import tn.petcare_android.data.model.auth.AppUser

data class SitterCard(
    val id: String,
    val name: String,
    val service: String,
    val description: String,
    val rating: Double,
    val reviews: Int = 0,
    val emoji: String,
    val tint: Color,
    val userId: String,
    val appUser: AppUser? = null
)

enum class ServiceType {
    AT_HOME,
    VISIT_ONLY,
    WALKING
}
