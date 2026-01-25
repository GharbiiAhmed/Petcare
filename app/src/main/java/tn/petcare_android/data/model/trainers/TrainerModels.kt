package tn.petcare_android.data.model.trainers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trainer(
    @Json(name = "_id") val id: String?,
    val specialization: String?,
    val hourlyRate: Double?,
    val yearsOfExperience: Int?,
    val certifications: List<String>?,
    val bio: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isAvailable: Boolean?,
    val trainingMethods: List<String>?,
    val profileImage: String?,
    val averageRating: Double?,
    val totalReviews: Int?,
    val phoneNumber: String?,
    val email: String?,
    val name: String?,
    val profileImageUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class TrainerBooking(
    @Json(name = "_id") val id: String?,
    val owner: TrainerBookingUser?,
    val trainer: TrainerWithInfo?,
    val pet: BookingPet?,
    val sessionType: String?,
    val description: String?,
    val startDateTime: String?,
    val endDateTime: String?,
    val duration: Int?,
    val totalPrice: Double?,
    val status: String?,
    val notes: String?,
    val location: String?,
    val rejectionReason: String?,
    val cancelledAt: String?,
    val completedAt: String?,
    val cancellationReason: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class TrainerBookingUser(
    @Json(name = "_id") val id: String?,
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val profileImage: String?
)

@JsonClass(generateAdapter = true)
data class TrainerWithInfo(
    @Json(name = "_id") val id: String?,
    val user: TrainerBookingUser?,
    val specialization: String?,
    val hourlyRate: Double?,
    val yearsOfExperience: Int?,
    val isAvailable: Boolean?
)

@JsonClass(generateAdapter = true)
data class BookingPet(
    @Json(name = "_id") val id: String?,
    val name: String?,
    val breed: String?,
    val species: String?
)
