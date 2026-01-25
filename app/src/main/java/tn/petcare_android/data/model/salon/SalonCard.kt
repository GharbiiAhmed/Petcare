package tn.petcare_android.data.model.salon

import androidx.compose.ui.graphics.Color
import tn.petcare_android.data.model.auth.AppUser

data class SalonCard(
    val id: String,
    val name: String,
    val services: List<String>,
    val rating: Double,
    val reviews: Int = 0,
    val distanceKm: Double,
    val isOpen: Boolean,
    val tint: Color,
    val emoji: String,
    val userId: String,
    val appUser: AppUser? = null
)

enum class SalonSort {
    SERVICES,
    DISTANCE,
    RATING
}
