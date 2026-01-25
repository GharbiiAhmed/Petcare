package tn.petcare_android.data.api

import retrofit2.http.*
import tn.petcare_android.data.model.subscription.*

/**
 * Subscription API Interface
 * iOS Reference: SubscriptionService.swift (lines 1-136)
 * 
 * Endpoints:
 * - POST /subscriptions - Create subscription
 * - GET /subscriptions/me - Get user's subscription
 * - POST /subscriptions/cancel - Cancel subscription
 * - POST /subscriptions/reactivate - Reactivate subscription
 * - POST /subscriptions/renew - Renew subscription
 */
interface SubscriptionApi {

    /**
     * Create a subscription for the user (vet or sitter)
     * Backend handles Stripe payment processing
     * Response includes clientSecret for Stripe PaymentSheet
     */
    @POST("/subscriptions")
    suspend fun createSubscription(
        @Body request: CreateSubscriptionRequest
    ): SubscriptionResponse

    /**
     * Get the current user's subscription
     * Returns subscription with status: active, expires_soon, canceled, expired, pending, or none
     */
    @GET("/subscriptions/me")
    suspend fun getSubscription(): Subscription

    /**
     * Cancel the user's subscription (at period end)
     * Subscription remains active until current period ends
     * Status changes to "canceled", role downgraded when period ends
     */
    @POST("/subscriptions/cancel")
    suspend fun cancelSubscription(): CancelSubscriptionResponse

    /**
     * Reactivate a canceled subscription
     * Can only reactivate if subscription hasn't ended yet
     */
    @POST("/subscriptions/reactivate")
    suspend fun reactivateSubscription(): Subscription

    /**
     * Renew/extend an active or expires_soon subscription
     * Extends the subscription for another billing period
     */
    @POST("/subscriptions/renew")
    suspend fun renewSubscription(): Subscription
    
    /**
     * Update subscription role (vet or sitter)
     * Used when user pays first, then chooses their professional role
     */
    @POST("/subscriptions/update-role")
    suspend fun updateSubscriptionRole(@Body request: UpdateRoleRequest): Subscription
    
    /**
     * Manually activate pending subscription (for testing)
     * Used when webhook doesn't fire automatically
     */
    @POST("/subscriptions/activate-pending")
    suspend fun activatePendingSubscription(): Subscription
    
    /**
     * Verify email with code (after subscription creation)
     * Changes subscription status from "pending" to "active"
     */
    @POST("/subscriptions/verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): VerifyEmailResponse
    
    /**
     * Resend verification code for subscription activation
     * Works even if user is already verified (for subscription activation)
     */
    @POST("/subscriptions/resend-verification")
    suspend fun resendVerificationCode(): retrofit2.Response<tn.petcare_android.data.model.auth.MessageResponse>
}

