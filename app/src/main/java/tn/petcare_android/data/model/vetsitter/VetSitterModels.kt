package tn.petcare_android.data.model.vetsitter

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class CreateVetRequest(
    val email: String,
    val name: String,
    val password: String,
    val phoneNumber: String? = null,
    val licenseNumber: String,
    val clinicName: String,
    val clinicAddress: String,
    val specializations: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null
)

@JsonClass(generateAdapter = true)
data class ConvertVetRequest(
    val email: String,
    val licenseNumber: String,
    val clinicName: String,
    val clinicAddress: String,
    val specializations: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateVetRequest(
    val email: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    val country: String? = null,
    val city: String? = null,
    val licenseNumber: String? = null,
    val clinicName: String? = null,
    val clinicAddress: String? = null,
    val specializations: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null
)


@JsonClass(generateAdapter = true)
data class CreateSitterRequest(
    val email: String,
    val name: String,
    val password: String,
    val phoneNumber: String? = null,
    val hourlyRate: Double,
    val sitterAddress: String,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val availableWeekends: Boolean? = null,
    val canHostPets: Boolean? = null,
    val availability: List<String>? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null
)

@JsonClass(generateAdapter = true)
data class ConvertSitterRequest(
    val hourlyRate: Double,
    val sitterAddress: String,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val availableWeekends: Boolean? = null,
    val canHostPets: Boolean? = null,
    val availability: List<String>? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateSitterRequest(
    val email: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    val hourlyRate: Double? = null,
    val sitterAddress: String? = null,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val availableWeekends: Boolean? = null,
    val canHostPets: Boolean? = null,
    val availability: List<String>? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bio: String? = null
)
