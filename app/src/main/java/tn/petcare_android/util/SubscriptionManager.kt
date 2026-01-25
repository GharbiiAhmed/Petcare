package tn.petcare_android.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.subscription.Subscription
import tn.petcare_android.data.model.subscription.SubscriptionStatus
import tn.petcare_android.data.repository.SubscriptionRepository
import tn.petcare_android.data.storage.TokenManager
import java.util.Date
import java.text.SimpleDateFormat
import java.util.*

/**
 * Subscription Manager
 * iOS Reference: SubscriptionManager.swift (lines 1-91)
 * 
 * Manages subscription status checking and alerts:
 * - Periodic checking (every 30 minutes)
 * - Check on app foreground
 * - Alert cooldown (1 hour)
 * - Expiration alerts
 * - Auto-cancellation after 3 days of expiration
 */
object SubscriptionManager {
    private const val TAG = "SubscriptionManager"
    private const val CHECK_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
    private const val ALERT_COOLDOWN_MS = 60 * 60 * 1000L // 1 hour
    private const val AUTO_CANCEL_DAYS = 3L // Auto-cancel after 3 days of expiration
    
    private var repository: SubscriptionRepository? = null
    private var tokenManager: TokenManager? = null
    private var checkJob: Job? = null
    private var lastAlertShownDate: Date? = null
    
    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription.asStateFlow()
    
    private val _showExpirationAlert = MutableStateFlow(false)
    val showExpirationAlert: StateFlow<Boolean> = _showExpirationAlert.asStateFlow()
    
    private val _expirationMessage = MutableStateFlow<String?>(null)
    val expirationMessage: StateFlow<String?> = _expirationMessage.asStateFlow()
    
    // Event to notify when subscription becomes active (for refreshing discover list/map)
    private val _subscriptionActivated = MutableStateFlow(false)
    val subscriptionActivated: StateFlow<Boolean> = _subscriptionActivated.asStateFlow()
    
    /**
     * Initialize the subscription manager
     */
    fun initialize(
        repository: SubscriptionRepository,
        tokenManager: TokenManager
    ) {
        this.repository = repository
        this.tokenManager = tokenManager
        startPeriodicCheck()
    }
    
    /**
     * Start periodic subscription checking
     * iOS Reference: SubscriptionManager.swift line 28
     */
    fun startPeriodicCheck() {
        stopPeriodicCheck() // Stop any existing check
        
        checkJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(CHECK_INTERVAL_MS)
                checkSubscriptionStatus()
            }
        }
        
        Log.d(TAG, "✅ Started periodic subscription check (every 30 minutes)")
    }
    
    /**
     * Stop periodic checking
     * iOS Reference: SubscriptionManager.swift line 36
     */
    fun stopPeriodicCheck() {
        checkJob?.cancel()
        checkJob = null
        Log.d(TAG, "⏹️ Stopped periodic subscription check")
    }
    
    /**
     * Check subscription status and show alerts if needed
     * iOS Reference: SubscriptionManager.swift line 43
     */
    suspend fun checkSubscriptionStatus() {
        val repo = repository ?: run {
            Log.w(TAG, "⚠️ Repository not initialized")
            return
        }
        
        val token = tokenManager?.getAccessToken()?.firstOrNull()
        if (token.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ No access token available")
            return
        }
        
        try {
            val previousSubscription = _subscription.value
            val sub = repo.getSubscription()
            _subscription.value = sub
            
            // Check if subscription just became active (for refreshing discover list/map)
            if (previousSubscription?.subscriptionStatus != SubscriptionStatus.ACTIVE && 
                sub.subscriptionStatus == SubscriptionStatus.ACTIVE) {
                _subscriptionActivated.value = true
                // Reset after a short delay
                CoroutineScope(Dispatchers.IO).launch {
                    delay(100)
                    _subscriptionActivated.value = false
                }
                Log.d(TAG, "✅ Subscription activated - user should appear in discover list/map")
            }
            
            // Check if subscription is expiring soon
            if (sub.subscriptionStatus == SubscriptionStatus.ACTIVE && sub.willExpireSoon) {
                val days = sub.daysUntilExpiration
                if (days != null && days > 0) {
                    // Only show alert if we haven't shown it recently (cooldown)
                    val now = Date()
                    if (lastAlertShownDate != null) {
                        val timeSinceLastAlert = now.time - lastAlertShownDate!!.time
                        if (timeSinceLastAlert < ALERT_COOLDOWN_MS) {
                            Log.d(TAG, "⏸️ Alert still in cooldown period")
                            return // Still in cooldown period
                        }
                    }
                    
                    _expirationMessage.value = "Your subscription expires in $days day${if (days == 1) "" else "s"}. Renew now to continue your service."
                    _showExpirationAlert.value = true
                    lastAlertShownDate = now
                    Log.d(TAG, "⚠️ Showing expiration alert: $days days remaining")
                }
            }
            
            // Check if subscription has expired
            if (sub.isExpired) {
                // Check if subscription status is EXPIRED (not already canceled)
                if (sub.subscriptionStatus == SubscriptionStatus.EXPIRED) {
                    // Check if expired for more than 3 days - backend should auto-cancel, but verify
                    val expiredDate = parseDate(sub.currentPeriodEnd)
                    if (expiredDate != null) {
                        val daysSinceExpiration = (Date().time - expiredDate.time) / (1000 * 60 * 60 * 24)
                        if (daysSinceExpiration >= AUTO_CANCEL_DAYS) {
                            // Subscription expired for 3+ days - should be auto-canceled by backend
                            // Refresh subscription to get updated status
                            Log.d(TAG, "⚠️ Subscription expired for $daysSinceExpiration days - should be auto-canceled")
                            // Backend should handle auto-cancellation, but we refresh to get updated status
                            val updatedSub = repo.getSubscription()
                            _subscription.value = updatedSub
                            
                            if (updatedSub.subscriptionStatus == SubscriptionStatus.CANCELED) {
                                _expirationMessage.value = "Your subscription has been automatically canceled after 3 days of expiration. Your role has been downgraded to owner."
                                _showExpirationAlert.value = true
                                lastAlertShownDate = Date()
                                Log.d(TAG, "❌ Subscription auto-canceled after 3 days")
                            }
                        } else {
                            // Still within 3-day grace period
                            val remainingDays = AUTO_CANCEL_DAYS - daysSinceExpiration
                            _expirationMessage.value = "Your subscription has expired. You have $remainingDays day${if (remainingDays != 1L) "s" else ""} to renew before it's automatically canceled."
                            _showExpirationAlert.value = true
                            lastAlertShownDate = Date()
                            Log.d(TAG, "⚠️ Subscription expired - $remainingDays days until auto-cancellation")
                        }
                    }
                } else if (sub.subscriptionStatus == SubscriptionStatus.ACTIVE) {
                    // Subscription expired but status still shows active (shouldn't happen, but handle it)
                    _expirationMessage.value = "Your subscription has expired. Your role has been downgraded to owner."
                    _showExpirationAlert.value = true
                    lastAlertShownDate = Date()
                    Log.d(TAG, "❌ Subscription expired")
                }
            }
            
            // Check for expires_soon status (7 days before expiration)
            if (sub.subscriptionStatus == SubscriptionStatus.EXPIRES_SOON) {
                val days = sub.daysUntilExpiration ?: 0
                if (days > 0 && days <= 7) {
                    val now = Date()
                    if (lastAlertShownDate != null) {
                        val timeSinceLastAlert = now.time - lastAlertShownDate!!.time
                        if (timeSinceLastAlert >= ALERT_COOLDOWN_MS) {
                            _expirationMessage.value = "Your subscription will expire in $days day${if (days != 1) "s" else ""}. Renew now to keep your ${sub.role} status."
                            _showExpirationAlert.value = true
                            lastAlertShownDate = now
                            Log.d(TAG, "⚠️ Subscription expires soon: $days days remaining")
                        }
                    } else {
                        _expirationMessage.value = "Your subscription will expire in $days day${if (days != 1) "s" else ""}. Renew now to keep your ${sub.role} status."
                        _showExpirationAlert.value = true
                        lastAlertShownDate = now
                        Log.d(TAG, "⚠️ Subscription expires soon: $days days remaining")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Failed to check subscription status: ${e.message}", e)
        }
    }
    
    /**
     * Refresh subscription data
     * iOS Reference: SubscriptionManager.swift line 86
     */
    suspend fun refreshSubscription() {
        checkSubscriptionStatus()
    }
    
    /**
     * Dismiss expiration alert
     */
    fun dismissExpirationAlert() {
        _showExpirationAlert.value = false
    }
    
    /**
     * Clear all state (e.g., on logout)
     */
    fun clear() {
        stopPeriodicCheck()
        _subscription.value = null
        _showExpirationAlert.value = false
        _expirationMessage.value = null
        lastAlertShownDate = null
        repository = null
        tokenManager = null
    }
    
    /**
     * Parse date string to Date object
     * Helper function to parse subscription dates
     */
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

