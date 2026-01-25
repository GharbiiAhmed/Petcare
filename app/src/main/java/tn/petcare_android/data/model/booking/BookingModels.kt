package tn.petcare_android.data.model.booking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Booking(
    // id can come as `id` or `_id`
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val _id: String? = null,

    // owner may be provided as ownerId or populated owner object
    @Json(name = "ownerId") val ownerId: String? = null,
    @Json(name = "owner") val owner: BookingUser? = null,

    // provider may be provided as providerId or populated provider object
    @Json(name = "providerId") val providerId: String? = null,
    @Json(name = "provider") val provider: BookingUser? = null,

    @Json(name = "providerType") val providerType: String? = null, // "vet" or "sitter"

    // pet may be provided as petId or populated pet object
    @Json(name = "petId") val petId: String? = null,
    @Json(name = "pet") val pet: BookingPet? = null,

    @Json(name = "serviceType") val serviceType: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "dateTime") val dateTime: String? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "price") val price: Double? = null,

    // keep status as raw string for resilience
    @Json(name = "status") val status: String? = null,

    @Json(name = "rejectionReason") val rejectionReason: String? = null,
    @Json(name = "completedAt") val completedAt: String? = null,
    @Json(name = "cancelledAt") val cancelledAt: String? = null,
    @Json(name = "cancellationReason") val cancellationReason: String? = null,

    // created/updated may come as created/createdAt and updated/updatedAt
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "created") val created: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
    @Json(name = "updated") val updated: String? = null
)

// Helper/extension-like properties for normalized access
{
    val normalizedId: String
        get() = id ?: _id ?: ""

    val normalizedOwnerId: String
        get() = ownerId ?: owner?.normalizedId ?: ""

    val normalizedProviderId: String
        get() = providerId ?: provider?.normalizedId ?: ""

    val normalizedPetId: String?
        get() = petId ?: pet?.normalizedId

    val normalizedCreatedAt: String?
        get() = createdAt ?: created

    val normalizedUpdatedAt: String?
        get() = updatedAt ?: updated

    val displayStatus: String
        get() = status ?: "pending"
}

@JsonClass(generateAdapter = true)
enum class BookingStatus {
    @Json(name = "pending") PENDING,
    @Json(name = "accepted") ACCEPTED,
    @Json(name = "rejected") REJECTED,
    @Json(name = "completed") COMPLETED,
    @Json(name = "cancelled") CANCELLED
}

@JsonClass(generateAdapter = true)
data class BookingUser(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "profileImage") val profileImage: String? = null,
    @Json(name = "avatarUrl") val avatarUrl: String? = null
) {
    val normalizedId: String
        get() = id ?: _id ?: ""

    val normalizedProfileImage: String?
        get() = profileImage ?: avatarUrl
}

@JsonClass(generateAdapter = true)
data class BookingPet(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "species") val species: String? = null,
    @Json(name = "breed") val breed: String? = null
) {
    val normalizedId: String
        get() = id ?: _id ?: ""
}

@JsonClass(generateAdapter = true)
data class CreateBookingRequest(
    @Json(name = "providerId") val providerId: String,
    @Json(name = "providerType") val providerType: String, // "vet" or "sitter"
    @Json(name = "petId") val petId: String?,
    @Json(name = "serviceType") val serviceType: String,
    @Json(name = "description") val description: String?,
    @Json(name = "dateTime") val dateTime: String,
    @Json(name = "duration") val duration: Int?,
    @Json(name = "price") val price: Double?
)

@JsonClass(generateAdapter = true)
data class UpdateBookingRequest(
    @Json(name = "status") val status: String? = null,
    @Json(name = "rejectionReason") val rejectionReason: String? = null,
    @Json(name = "cancellationReason") val cancellationReason: String? = null
)
