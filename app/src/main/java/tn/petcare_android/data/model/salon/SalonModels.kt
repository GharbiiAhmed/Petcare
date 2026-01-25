package tn.petcare_android.data.model.salon

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateSalonRequest(
    val email: String,
    val name: String,
    val password: String,
    val phoneNumber: String? = null,
    val salonName: String,
    val salonAddress: String,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null,
    val pricing: Map<String, Double>? = null
)

@JsonClass(generateAdapter = true)
data class ConvertSalonRequest(
    val salonName: String,
    val salonAddress: String,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null,
    val pricing: Map<String, Double>? = null
)

@JsonClass(generateAdapter = true)
data class UpdateSalonRequest(
    val email: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    val country: String? = null,
    val city: String? = null,
    val salonName: String? = null,
    val salonAddress: String? = null,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null,
    val pricing: Map<String, Double>? = null
)


