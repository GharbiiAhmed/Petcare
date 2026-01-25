package tn.petcare_android.data.repository

import tn.petcare_android.data.api.SubscriptionApi
import tn.petcare_android.data.model.subscription.*

/**
 * Subscription Repository
 * iOS Reference: SubscriptionService.swift (lines 1-136)
 * 
 * Handles all subscription-related data operations
 * Manages communication with backend subscription endpoints
 */
class SubscriptionRepository(private val api: SubscriptionApi) {

    /**
     * Create a new subscription for vet or sitter
     * @param role "vet" or "sitter"
     * @param paymentMethodId Optional Stripe payment method ID
     * @return SubscriptionResponse with subscription and clientSecret
     */
    suspend fun createSubscription(
        role: String,
        paymentMethodId: String? = null
    ): SubscriptionResponse {
        return api.createSubscription(
            CreateSubscriptionRequest(role = role, paymentMethodId = paymentMethodId)
        )
    }

    /**
     * Get the current user's subscription
     * @return Subscription with current status
     */
    suspend fun getSubscription(): Subscription {
        return try {
            api.getSubscription()
        } catch (e: Exception) {
            // If no subscription exists or error, return default "none" subscription
            Subscription(
                id = "",
                userId = "",
                role = "owner",
                status = "none",
                stripeSubscriptionId = null,
                stripeCustomerId = null,
                currentPeriodStart = null,
                currentPeriodEnd = null,
                cancelAtPeriodEnd = null,
                createdAt = null,
                updatedAt = null
            )
        }
    }

    /**
     * Cancel the user's subscription
     * Subscription remains active until end of current billing period
     * @return CancelSubscriptionResponse with updated subscription
     */
    suspend fun cancelSubscription(): CancelSubscriptionResponse {
        return api.cancelSubscription()
    }

    /**
     * Reactivate a canceled subscription
     * Can only reactivate before the end of current period
     * @return Updated subscription with active status
     */
    suspend fun reactivateSubscription(): Subscription {
        return api.reactivateSubscription()
    }

    /**
     * Renew an active or expiring subscription
     * Extends the subscription for another billing period
     * @return Updated subscription with extended period
     */
    suspend fun renewSubscription(): Subscription {
        return api.renewSubscription()
    }
    
    /**
     * Update subscription role after payment
     * Used when user subscribes first, then chooses vet or sitter
     * @param role "vet" or "sitter"
     * @return Updated subscription with new role
     */
    suspend fun updateSubscriptionRole(role: String): Subscription {
        return api.updateSubscriptionRole(UpdateRoleRequest(role = role))
    }
    
    /**
     * Manually activate pending subscription (for testing)
     * Used when webhook doesn't fire automatically
     */
    suspend fun activatePendingSubscription(): Subscription {
        return api.activatePendingSubscription()
    }
    
    /**
     * Verify email with code sent to user's email
     * Changes subscription status from "pending" to "active"
     * @param code Email verification code
     * @return VerifyEmailResponse with success status
     */
    suspend fun verifyEmail(code: String): VerifyEmailResponse {
        return api.verifyEmail(VerifyEmailRequest(code = code))
    }
    
    /**
     * Resend verification code for subscription activation
     * Works even if user is already verified (for subscription activation)
     * @return MessageResponse with success message
     */
    suspend fun resendVerificationCode(): tn.petcare_android.data.model.auth.MessageResponse {
        val response = api.resendVerificationCode()
        if (response.isSuccessful) {
            return response.body() ?: tn.petcare_android.data.model.auth.MessageResponse(message = "Verification code sent")
        } else {
            throw Exception(response.errorBody()?.string() ?: "Failed to resend verification code")
        }
    }

    /**
     * Check subscription status
     * Helper method to determine subscription state
     * @param subscription Subscription to check
     * @return Triple of (isActive, daysUntilExpiration, willExpireSoon)
     */
    fun checkSubscriptionStatus(subscription: Subscription): Triple<Boolean, Int?, Boolean> {
        return Triple(
            subscription.isActive,
            subscription.daysUntilExpiration,
            subscription.willExpireSoon
        )
    }
}

