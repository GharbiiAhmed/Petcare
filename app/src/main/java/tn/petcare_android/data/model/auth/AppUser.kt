package tn.petcare_android.data.model.auth

import com.squareup.moshi.Json

/**
 * App user model matching backend structure
 * Used across vet, sitter, and general user features
 */
data class AppUser(
    @Json(name = "_id") val id: String,
    val email: String,
    val name: String? = null,
    val role: String? = null,
    @Json(name = "profileImage") val avatarUrl: String? = null, // Backend uses profileImage
    val city: String? = null,
    val country: String? = null,
    val phoneNumber: String? = null, // Backend field name - matches user schema
    val phone: String? = null, // Alias for phoneNumber (some code uses this)
    
    // Vet-specific fields (backend merges these into user document)
    val vetClinicName: String? = null,
    val vetAddress: String? = null,
    val vetSpecializations: List<String>? = null,
    val vetLicenseNumber: String? = null,
    val vetYearsOfExperience: Int? = null,
    val vetEmergencyAvailable: Boolean? = null,
    val vetBio: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Sitter-specific fields
    val sitterAddress: String? = null,
    val hourlyRate: Double? = null,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val availableWeekends: Boolean? = null,
    val canHostPets: Boolean? = null,
    val availability: List<String>? = null,
    val bio: String? = null,
    
    // Salon-specific fields (backend merges these into user document)
    val salonName: String? = null,
    val salonAddress: String? = null,
    val salonServices: List<String>? = null,
    val salonYearsOfExperience: Int? = null,
    val salonBio: String? = null,
    val salonPricing: Map<String, Double>? = null
)
