package tn.petcare_android.viewmodel.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.subscription.Subscription
import tn.petcare_android.data.model.subscription.SubscriptionResponse
import tn.petcare_android.data.model.subscription.SubscriptionStatus
import tn.petcare_android.data.repository.SubscriptionRepository
import tn.petcare_android.data.repository.AuthRepository
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.ResendVerificationRequest

/**
 * Subscription ViewModel
 * iOS Reference: SubscriptionManager.swift (lines 1-91)
 * 
 * Manages subscription state and operations:
 * - Create subscription ($30/month for vet or sitter)
 * - Get subscription status
 * - Cancel subscription
 * - Reactivate subscription
 * - Renew subscription
 * 
 * Note: Email verification is no longer required.
 * Subscriptions activate automatically upon successful payment.
 */
sealed class SubscriptionUiState {
    object Idle : SubscriptionUiState()
    object Loading : SubscriptionUiState()
    data class Success(val message: String? = null) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
    data class PaymentRequired(val clientSecret: String) : SubscriptionUiState()
    data class RoleUpdated(val role: String) : SubscriptionUiState()
}

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val authRepository: AuthRepository = AuthRepository(RetrofitInstance.api)
) : ViewModel() {

    companion object {
        const val SUBSCRIPTION_PRICE = 30.0 // $30/month
    }

    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription

    private val _uiState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Idle)
    val uiState: StateFlow<SubscriptionUiState> = _uiState

    private val _showExpirationAlert = MutableStateFlow(false)
    val showExpirationAlert: StateFlow<Boolean> = _showExpirationAlert

    private val _expirationMessage = MutableStateFlow<String?>(null)
    val expirationMessage: StateFlow<String?> = _expirationMessage

    /**
     * Get the current user's subscription
     */
    fun getSubscription() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            try {
                val sub = repository.getSubscription()
                _subscription.value = sub
                _uiState.value = SubscriptionUiState.Idle // Changed from Success() to Idle
                checkForAlerts(sub)
            } catch (e: Exception) {
                _uiState.value = SubscriptionUiState.Error(e.message ?: "Failed to load subscription")
            }
        }
    }

    /**
     * Create a new subscription
     * @param role "vet", "sitter", or "premium" (role chosen later)
     */
    fun createSubscription(role: String) {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            try {
                val response: SubscriptionResponse = repository.createSubscription(role)
                _subscription.value = response.subscription
                
                // If backend returns clientSecret, payment is required
                if (response.clientSecret != null) {
                    _uiState.value = SubscriptionUiState.PaymentRequired(response.clientSecret)
                } else {
                    // Test mode - subscription activated immediately
                    // Navigate to manage subscription to choose role
                    _uiState.value = SubscriptionUiState.Success(
                        response.message ?: "Subscription activated! Choose your professional role."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SubscriptionUiState.Error(
                    e.message ?: "Failed to create subscription"
                )
            }
        }
    }

    /**
     * Cancel the subscription
     * Subscription remains active until end of current period
     */
    fun cancelSubscription() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            try {
                val response = repository.cancelSubscription()
                _subscription.value = response.subscription
                _uiState.value = SubscriptionUiState.Success(
                    response.message ?: "Subscription cancelled. You can use it until ${response.subscription.currentPeriodEnd}"
                )
            } catch (e: Exception) {
                _uiState.value = SubscriptionUiState.Error(
                    e.message ?: "Failed to cancel subscription"
                )
            }
        }
    }

    /**
     * Reactivate a cancelled subscription
     */
    fun reactivateSubscription() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            try {
                val sub = repository.reactivateSubscription()
                _subscription.value = sub
                _uiState.value = SubscriptionUiState.Success("Subscription reactivated!")
            } catch (e: Exception) {
                _uiState.value = SubscriptionUiState.Error(
                    e.message ?: "Failed to reactivate subscription"
                )
            }
        }
    }

    /**
     * Renew the subscription
     * Extends the subscription for another billing period
     */
    fun renewSubscription() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            try {
                val sub = repository.renewSubscription()
                _subscription.value = sub
                _uiState.value = SubscriptionUiState.Success("Subscription renewed!")
            } catch (e: Exception) {
                _uiState.value = SubscriptionUiState.Error(
                    e.message ?: "Failed to renew subscription"
                )
            }
        }
    }

    /**
     * Update subscription role (vet or sitter)
     * Used when user pays first, then chooses their professional role
     */
    fun updateSubscriptionRole(role: String) {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            try {
                val sub = repository.updateSubscriptionRole(role)
                _subscription.value = sub
                _uiState.value = SubscriptionUiState.RoleUpdated(role)
            } catch (e: Exception) {
                _uiState.value = SubscriptionUiState.Error(
                    e.message ?: "Failed to update role"
                )
            }
        }
    }

    /**
     * Manually activate pending subscription (for testing when webhook doesn't fire)
     */
    fun activatePendingSubscription() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            try {
                val sub = repository.activatePendingSubscription()
                _subscription.value = sub
                _uiState.value = SubscriptionUiState.Success("Subscription activated!")
            } catch (e: Exception) {
                _uiState.value = SubscriptionUiState.Error(
                    e.message ?: "Failed to activate subscription"
                )
            }
        }
    }

    /**
     * Check for expiration alerts
     * Shows alerts if subscription is expiring soon or expired
     */
    private fun checkForAlerts(sub: Subscription) {
        when (sub.subscriptionStatus) {
            SubscriptionStatus.ACTIVE -> {
                if (sub.willExpireSoon) {
                    val days = sub.daysUntilExpiration ?: 0
                    _expirationMessage.value = "Your subscription expires in $days day${if (days != 1) "s" else ""}. Renew now to continue your service."
                    _showExpirationAlert.value = true
                }
            }
            SubscriptionStatus.EXPIRED -> {
                _expirationMessage.value = "Your subscription has expired. Your role has been downgraded to owner."
                _showExpirationAlert.value = true
            }
            SubscriptionStatus.EXPIRES_SOON -> {
                val days = sub.daysUntilExpiration ?: 0
                _expirationMessage.value = "Your subscription will expire in $days day${if (days != 1) "s" else ""}. Renew to keep your ${sub.role} status."
                _showExpirationAlert.value = true
            }
            else -> {
                _showExpirationAlert.value = false
            }
        }
    }

    /**
     * Dismiss expiration alert
     */
    fun dismissExpirationAlert() {
        _showExpirationAlert.value = false
    }

    /**
     * Reset UI state
     */
    fun resetUiState() {
        _uiState.value = SubscriptionUiState.Idle
    }
    
    /**
     * Set error state manually (e.g., from payment handler)
     */
    fun setError(message: String) {
        _uiState.value = SubscriptionUiState.Error(message)
    }
}

