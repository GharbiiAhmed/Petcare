package tn.petcare_android.data.model.auth

import com.squareup.moshi.Json

data class User(
    @Json(name = "_id") val id: String? = null,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val phoneNumber: String? = null,
    val country: String? = null,
    val city: String? = null,
    val profileImage: String? = null,
    val hasPhoto: Boolean? = null,
    val hasPets: Boolean? = null,
    val balance: Double? = null,
    val isVerified: Boolean? = null,
    val hasActiveSubscription: Boolean? = null,
    val provider: String? = "local", // "local" or "google"
    val providerId: String? = null,
    val roleApprovalStatus: String? = "approved", // "pending", "approved", "rejected"
    val roleApprovalNotes: String? = null
)