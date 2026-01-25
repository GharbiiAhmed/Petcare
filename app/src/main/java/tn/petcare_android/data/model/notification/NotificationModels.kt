package tn.petcare_android.data.model.notification

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tn.petcare_android.data.model.booking.Booking
import tn.petcare_android.data.model.booking.BookingUser

/**
 * Notification models matching iOS NotificationModels.swift
 * iOS Reference: NotificationModels.swift lines 8-169
 */

@JsonClass(generateAdapter = true)
data class AppNotification(
    // Handle both id and _id from backend
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val _id: String? = null,
    
    // Recipient - backend returns String (user ID), not Object
    @Json(name = "recipient") val recipient: String? = null,
    
    // Sender can be ID or populated object
    @Json(name = "sender") val sender: BookingUser? = null,
    
    @Json(name = "type") val type: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "message") val message: String? = null,
    
    // Booking can be ID or populated object
    @Json(name = "bookingId") val bookingId: String? = null,
    @Json(name = "booking") val booking: Booking? = null,
    
    // Message reference can be ID or populated object
    @Json(name = "messageRefId") val messageRefId: String? = null,
    @Json(name = "messageRef") val messageRef: String? = null,
    
    @Json(name = "read") val read: Boolean = false,
    @Json(name = "readAt") val readAt: String? = null,
    
    // Handle both createdAt and created
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "created") val created: String? = null,
    
    @Json(name = "metadata") val metadata: NotificationMetadata? = null
) {
    /**
     * Normalized ID (handles both id and _id)
     */
    val normalizedId: String
        get() = id ?: _id ?: ""
    
    /**
     * Normalized recipient ID
     */
    val normalizedRecipientId: String
        get() = recipient ?: ""
    
    /**
     * Normalized sender ID
     */
    val normalizedSenderId: String
        get() = sender?.normalizedId ?: ""
    
    /**
     * Normalized booking ID
     */
    val normalizedBookingId: String?
        get() = bookingId ?: booking?.normalizedId
    
    /**
     * Normalized created date
     */
    val normalizedCreatedAt: String?
        get() = createdAt ?: created
    
    /**
     * Display title (fallback to type if no title)
     */
    val displayTitle: String
        get() = title ?: type?.capitalize() ?: "Notification"
    
    /**
     * Display message (fallback to empty string)
     */
    val displayMessage: String
        get() = message ?: ""
}

/**
 * Notification metadata
 * iOS Reference: NotificationModels.swift lines 152-164
 */
@JsonClass(generateAdapter = true)
data class NotificationMetadata(
    @Json(name = "bookingId") val bookingId: String? = null,
    @Json(name = "serviceType") val serviceType: String? = null,
    @Json(name = "dateTime") val dateTime: String? = null,
    @Json(name = "rejectionReason") val rejectionReason: String? = null
)

/**
 * Notification count response
 * iOS Reference: NotificationModels.swift lines 166-168
 */
@JsonClass(generateAdapter = true)
data class NotificationCountResponse(
    @Json(name = "count") val count: Int
)

/**
 * Notification types enum
 */
enum class NotificationType(val value: String) {
    BOOKING_REQUEST("booking_request"),
    BOOKING_ACCEPTED("booking_accepted"),
    BOOKING_REJECTED("booking_rejected"),
    BOOKING_COMPLETED("booking_completed"),
    BOOKING_CANCELLED("booking_cancelled"),
    MESSAGE("message"),
    SYSTEM("system");
    
    companion object {
        fun fromString(value: String?): NotificationType? {
            return values().find { it.value == value }
        }
    }
}

/**
 * Extension to get icon name based on notification type
 */
fun AppNotification.getIconName(): String {
    return when (NotificationType.fromString(type)) {
        NotificationType.BOOKING_REQUEST -> "calendar"
        NotificationType.BOOKING_ACCEPTED -> "checkmark.circle.fill"
        NotificationType.BOOKING_REJECTED -> "xmark.circle.fill"
        NotificationType.BOOKING_COMPLETED -> "checkmark.seal.fill"
        NotificationType.BOOKING_CANCELLED -> "xmark.octagon.fill"
        NotificationType.MESSAGE -> "message.fill"
        NotificationType.SYSTEM -> "bell.fill"
        null -> "bell.fill"
    }
}

/**
 * Extension to get icon color based on notification type
 */
fun AppNotification.getIconColor(): String {
    return when (NotificationType.fromString(type)) {
        NotificationType.BOOKING_REQUEST -> "orange"
        NotificationType.BOOKING_ACCEPTED -> "green"
        NotificationType.BOOKING_REJECTED -> "red"
        NotificationType.BOOKING_COMPLETED -> "blue"
        NotificationType.BOOKING_CANCELLED -> "gray"
        NotificationType.MESSAGE -> "blue"
        NotificationType.SYSTEM -> "gray"
        null -> "gray"
    }
}

