package tn.petcare_android.data.session

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.RefreshRequest
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.storage.UserManager
import tn.petcare_android.util.JwtDecoder

/**
 * SessionManager handles automatic token refresh and session restoration.
 * This is the Android equivalent of iOS SessionManager.swift
 * 
 * Features:
 * - Automatic token refresh on app start
 * - Handles token expiration gracefully
 * - Prevents multiple simultaneous refresh attempts
 * - Token version tracking for email/password changes
 */
class SessionManager private constructor(private val context: Context) {
    
    private val tokenManager = TokenManager(context)
    private val userManager = UserManager(context)
    private val refreshMutex = Mutex() // Prevent concurrent refresh attempts
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _hasRestoredSession = MutableStateFlow(false)
    val hasRestoredSession: StateFlow<Boolean> = _hasRestoredSession.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    companion object {
        private const val TAG = "SessionManager"
        
        @Volatile
        private var instance: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Restore session from stored tokens.
     * This is called on app start to check if user has valid session.
     * 
     * Flow:
     * 1. Check if tokens exist
     * 2. Verify access token is not expired
     * 3. If expired, try to refresh using refresh token
     * 4. If refresh succeeds, restore session
     * 5. If refresh fails, clear session and require login
     */
    suspend fun restoreSession(): Boolean {
        try {
            Log.d(TAG, "Attempting to restore session...")
            
            val accessToken = tokenManager.getAccessToken().firstOrNull()
            val refreshToken = tokenManager.getRefreshToken().firstOrNull()
            
            if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                Log.d(TAG, "No tokens found, session cannot be restored")
                _hasRestoredSession.value = true
                _isAuthenticated.value = false
                return false
            }
            
            // Check if access token is expired or about to expire (within 5 minutes)
            val isAccessTokenValid = isTokenValid(accessToken, bufferSeconds = 300)
            
            if (isAccessTokenValid) {
                // Access token is still valid, trust it and restore session
                // The tokenAuthenticator will handle token refresh if needed on API calls
                Log.d(TAG, "Access token is valid, restoring session...")
                _isAuthenticated.value = true
                _hasRestoredSession.value = true
                Log.d(TAG, "Session restored successfully with existing token")
                return true
            }
            
            // Access token is expired or invalid, try to refresh
            Log.d(TAG, "Access token expired, attempting refresh...")
            val refreshed = refreshTokens()
            
            if (refreshed) {
                _isAuthenticated.value = true
                _hasRestoredSession.value = true
                Log.d(TAG, "Session restored successfully after refresh")
                return true
            } else {
                // Refresh failed, clear session
                Log.w(TAG, "Token refresh failed, clearing session")
                clearSession()
                _hasRestoredSession.value = true
                _isAuthenticated.value = false
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring session: ${e.message}", e)
            _hasRestoredSession.value = true
            _isAuthenticated.value = false
            return false
        }
    }
    
    /**
     * Refresh tokens using the refresh token.
     * Uses mutex to prevent multiple simultaneous refresh attempts.
     * 
     * @return true if refresh succeeded, false otherwise
     */
    suspend fun refreshTokens(): Boolean {
        return refreshMutex.withLock {
            try {
                if (_isRefreshing.value) {
                    Log.d(TAG, "Already refreshing, skipping duplicate refresh")
                    return@withLock false
                }
                
                _isRefreshing.value = true
                Log.d(TAG, "Starting token refresh...")
                
                val refreshToken = tokenManager.getRefreshToken().firstOrNull()
                if (refreshToken.isNullOrBlank()) {
                    Log.w(TAG, "No refresh token available")
                    return@withLock false
                }
                
                // Call refresh endpoint directly (without auth interceptor)
                val response = RetrofitInstance.api.refresh(RefreshRequest(refreshToken))
                
                if (response.isSuccessful) {
                    val tokens = response.body()?.tokens
                    if (tokens != null) {
                        // Save new tokens
                        tokenManager.saveTokens(
                            accessToken = tokens.accessToken,
                            refreshToken = tokens.refreshToken
                        )
                        
                        // Verify new tokens work by fetching profile
                        try {
                            val profileResponse = RetrofitInstance.api.getMe()
                            if (profileResponse.isSuccessful && profileResponse.body() != null) {
                                val user = profileResponse.body()!!
                                val userId = user.id ?: user.email ?: ""
                                userManager.saveUserId(userId)
                                
                                Log.d(TAG, "Token refresh successful")
                                return@withLock true
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Profile fetch after refresh failed: ${e.message}")
                        }
                    }
                }
                
                Log.w(TAG, "Token refresh failed: ${response.code()}")
                return@withLock false
                
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh error: ${e.message}", e)
                return@withLock false
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Check if a JWT token is valid (not expired).
     * 
     * @param token JWT token to check
     * @param bufferSeconds Add buffer time before expiration (default 60 seconds)
     * @return true if token is valid, false if expired or invalid
     */
    private fun isTokenValid(token: String, bufferSeconds: Long = 60): Boolean {
        return try {
            val expirationTime = JwtDecoder.getExpirationTimeFromToken(token)
            if (expirationTime == null) {
                Log.w(TAG, "Could not extract expiration from token")
                return false
            }
            
            val currentTime = System.currentTimeMillis() / 1000
            val timeUntilExpiration = expirationTime - currentTime
            
            if (timeUntilExpiration <= bufferSeconds) {
                Log.d(TAG, "Token expires in ${timeUntilExpiration}s, considering it expired")
                return false
            }
            
            Log.d(TAG, "Token is valid, expires in ${timeUntilExpiration}s")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking token validity: ${e.message}", e)
            false
        }
    }
    
    /**
     * Clear session and all stored tokens.
     * This is called on logout or when tokens are invalid.
     */
    suspend fun clearSession() {
        Log.d(TAG, "Clearing session...")
        tokenManager.clearTokens()
        userManager.clearUserId()
        _isAuthenticated.value = false
    }
    
    /**
     * Set authenticated state after successful login.
     */
    fun setAuthenticated(value: Boolean) {
        _isAuthenticated.value = value
    }
    
    /**
     * Check if user is currently authenticated.
     */
    fun isUserAuthenticated(): Boolean {
        return _isAuthenticated.value
    }
}
