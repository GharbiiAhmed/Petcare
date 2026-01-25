package tn.petcare_android.data.model.vet

import androidx.compose.ui.graphics.Color
import tn.petcare_android.data.model.auth.AppUser

data class VetCard(
    val id: String,
    val name: String,
    val specialties: List<String>,
    val rating: Double,
    val reviews: Int,
    val distanceKm: Double,
    val is247: Boolean,
    val isOpen: Boolean,
    val tint: Color,
    val emoji: String,
    val userId: String,
    val appUser: AppUser? = null
)

enum class VetSort {
    SPECIALTY,
    DISTANCE,
    ALL_DAY
}
