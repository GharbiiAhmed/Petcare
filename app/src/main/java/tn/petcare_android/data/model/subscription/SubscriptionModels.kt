package tn.petcare_android.data.model.subscription

import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.*

/**
 * Subscription Models
 * iOS Reference: SubscriptionModels.swift (lines 1-172)
 */

// MARK: - Subscription Status
enum class SubscriptionStatus(val value: String) {
    ACTIVE("active"),
    EXPIRES_SOON("expires_soon"),
    CANCELED("canceled"),
    EXPIRED("expired"),
    PENDING("pending"),
    NONE("none");

    companion object {
        fun fromString(value: String): SubscriptionStatus {
            return values().find { it.value == value } ?: NONE
        }
    }
}

// MARK: - Subscription Model
@JsonClass(generateAdapter = true)
data class Subscription(
    val id: String,
    val userId: String,
    val role: String, // "vet" or "sitter"
    val status: String,
    val stripeSubscriptionId: String? = null,
    val stripeCustomerId: String? = null,
    val currentPeriodStart: String? = null,
    val currentPeriodEnd: String? = null,
    val cancelAtPeriodEnd: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val subscriptionStatus: SubscriptionStatus
        get() = SubscriptionStatus.fromString(status)

    val isActive: Boolean
        get() = subscriptionStatus == SubscriptionStatus.ACTIVE && !isExpired

    val isExpired: Boolean
        get() {
            val endDate = parseDate(currentPeriodEnd) ?: return false // No date = not expired
            return endDate.before(Date())
        }

    val daysUntilExpiration: Int?
        get() {
            val endDate = parseDate(currentPeriodEnd) ?: return null
            val diffInMillis = endDate.time - Date().time
            return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        }

    val willExpireSoon: Boolean
        get() {
            val days = daysUntilExpiration ?: return false
            return days <= 7 && days > 0
        }

    private fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(dateString)
        } catch (e: Exception) {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                formatter.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
    }
}

// MARK: - Create Subscription Request
@JsonClass(generateAdapter = true)
data class CreateSubscriptionRequest(
    val role: String, // "vet", "sitter", or "trainer"
    val paymentMethodId: String? = null // Stripe payment method ID (optional for test mode)
)

// MARK: - Update Role Request
@JsonClass(generateAdapter = true)
data class UpdateRoleRequest(
    val role: String // "vet", "sitter", or "trainer"
)

// MARK: - Subscription Response
@JsonClass(generateAdapter = true)
data class SubscriptionResponse(
    val subscription: Subscription,
    val clientSecret: String? = null, // For Stripe PaymentSheet
    val message: String? = null
)

// MARK: - Cancel Subscription Response
@JsonClass(generateAdapter = true)
data class CancelSubscriptionResponse(
    val subscription: Subscription,
    val message: String? = null
)

// MARK: - Verify Email Request
@JsonClass(generateAdapter = true)
data class VerifyEmailRequest(
    val code: String
)

// MARK: - Verify Email Response
@JsonClass(generateAdapter = true)
data class VerifyEmailResponse(
    val success: Boolean,
    val message: String? = null,
    val subscription: Subscription? = null
)

