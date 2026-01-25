package tn.petcare_android.data.model.adoption

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdoptionListing(
    @Json(name = "_id") val id: String?,
    val rescuer: AdoptionUser?,
    val adoptedBy: AdoptionUser?,
    val petName: String?,
    val species: String?,
    val breed: String?,
    val age: Int?,
    val color: String?,
    val weight: Double?,
    val gender: String?,
    val description: String?,
    val images: List<String>?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val vaccinated: Boolean?,
    val neutered: Boolean?,
    val medicalHistory: String?,
    val traits: List<String>?,
    val adoptionRequirements: String?,
    val status: String?,
    val adoptedDate: String?,
    val views: Int?,
    val likes: Int?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class AdoptionUser(
    @Json(name = "_id") val id: String?,
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val profileImage: String?,
    val location: String?
)

@JsonClass(generateAdapter = true)
data class AdoptionApplication(
    @Json(name = "_id") val id: String?,
    val applicant: AdoptionUser?,
    val listing: AdoptionListingInfo?,
    val phoneNumber: String?,
    val address: String?,
    val housingType: String?,
    val ownRent: String?,
    val otherPets: String?,
    val familyDescription: String?,
    val workSchedule: String?,
    val motivation: String?,
    val experience: String?,
    val vetReference: String?,
    val additionalInfo: String?,
    val status: String?,
    val rejectionReason: String?,
    val approvedAt: String?,
    val rejectedAt: String?,
    val completedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class AdoptionListingInfo(
    @Json(name = "_id") val id: String?,
    val petName: String?,
    val species: String?,
    val images: List<String>?,
    val adoptionRequirements: String?,
    val rescuer: AdoptionUser?
)
