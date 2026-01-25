package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.trainers.Trainer

interface TrainersApi {

    // Trainer Profile Management
    @POST("trainers")
    suspend fun createTrainer(
        @Body request: CreateTrainerRequest
    ): Response<Any>

    @GET("trainers")
    suspend fun getAllTrainers(): Response<List<Trainer>>

    @GET("trainers/{trainerId}")
    suspend fun getTrainer(
        @Path("trainerId") trainerId: String
    ): Response<Any>

    @PUT("trainers/{trainerId}")
    suspend fun updateTrainer(
        @Path("trainerId") trainerId: String,
        @Body request: UpdateTrainerRequest
    ): Response<Any>

    @DELETE("trainers/{trainerId}")
    suspend fun deleteTrainer(
        @Path("trainerId") trainerId: String
    ): Response<Unit>

    @POST("trainers/convert/{userId}")
    suspend fun convertUserToTrainer(
        @Path("userId") userId: String,
        @Body request: ConvertTrainerRequest
    ): Response<Any>

    // Trainer Bookings
    @POST("trainers/{trainerId}/bookings")
    suspend fun createTrainerBooking(
        @Path("trainerId") trainerId: String,
        @Body request: CreateTrainerBookingRequest
    ): Response<Any>

    @GET("trainers/{trainerId}/bookings")
    suspend fun getTrainerBookings(
        @Path("trainerId") trainerId: String
    ): Response<List<Any>>

    @GET("trainers/bookings/owner/{ownerId}")
    suspend fun getOwnerBookings(
        @Path("ownerId") ownerId: String
    ): Response<List<Any>>

    @GET("trainers/bookings/{bookingId}")
    suspend fun getBooking(
        @Path("bookingId") bookingId: String
    ): Response<Any>

    @PUT("trainers/bookings/{bookingId}")
    suspend fun updateBooking(
        @Path("bookingId") bookingId: String,
        @Body request: UpdateTrainerBookingRequest
    ): Response<Any>

    @PUT("trainers/bookings/{bookingId}/confirm")
    suspend fun confirmBooking(
        @Path("bookingId") bookingId: String
    ): Response<Any>

    @PUT("trainers/bookings/{bookingId}/reject")
    suspend fun rejectBooking(
        @Path("bookingId") bookingId: String,
        @Body request: RejectBookingRequest
    ): Response<Any>

    @PUT("trainers/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: String,
        @Body request: CancelBookingRequest
    ): Response<Any>

    @PUT("trainers/bookings/{bookingId}/complete")
    suspend fun completeBooking(
        @Path("bookingId") bookingId: String
    ): Response<Any>
}

// Trainer Request Models
data class CreateTrainerRequest(
    val specialization: String,
    val hourlyRate: Double,
    val yearsOfExperience: Int?,
    val certifications: List<String>?,
    val bio: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val trainingMethods: List<String>?,
    val phoneNumber: String?,
    val email: String?
)

data class UpdateTrainerRequest(
    val specialization: String?,
    val hourlyRate: Double?,
    val yearsOfExperience: Int?,
    val certifications: List<String>?,
    val bio: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val trainingMethods: List<String>?,
    val phoneNumber: String?,
    val email: String?,
    val isAvailable: Boolean?
)

data class ConvertTrainerRequest(
    val specialization: String,
    val hourlyRate: Double,
    val yearsOfExperience: Int?,
    val certifications: List<String>?,
    val bio: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val trainingMethods: List<String>?,
    val phoneNumber: String?
)

data class CreateTrainerBookingRequest(
    val sessionType: String,
    val description: String,
    val startDateTime: String,
    val endDateTime: String,
    val duration: Int,
    val totalPrice: Double,
    val petId: String?,
    val notes: String?,
    val location: String?
)

data class UpdateTrainerBookingRequest(
    val status: String?,
    val rejectionReason: String?,
    val cancelledAt: String?,
    val cancellationReason: String?
)

data class RejectBookingRequest(
    val rejectionReason: String
)

data class CancelBookingRequest(
    val cancellationReason: String
)
