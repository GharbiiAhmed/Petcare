package tn.petcare_android.util

import android.util.Base64
import org.json.JSONObject

/**
 * Utility for decoding JWT tokens and extracting claims.
 * Used to check token expiration and extract user information.
 */
object JwtDecoder {

    fun getUserIdFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val jsonObject = JSONObject(payload)

            when {
                jsonObject.has("userId") -> jsonObject.getString("userId")
                jsonObject.has("user_id") -> jsonObject.getString("user_id")
                jsonObject.has("id") -> jsonObject.getString("id")
                jsonObject.has("_id") -> jsonObject.getString("_id")
                jsonObject.has("sub") -> jsonObject.getString("sub")
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getEmailFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val jsonObject = JSONObject(payload)

            jsonObject.optString("email", null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Extract expiration time from JWT token.
     * 
     * @param token JWT access token
     * @return expiration time in seconds since epoch, or null if invalid
     */
    fun getExpirationTimeFromToken(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val jsonObject = JSONObject(payload)

            val exp = jsonObject.optLong("exp", -1L)
            if (exp > 0) exp else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Check if JWT token is expired.
     * 
     * @param token JWT access token
     * @param bufferSeconds Optional buffer time in seconds (token considered expired this many seconds before actual expiration)
     * @return true if token is expired, false otherwise
     */
    fun isTokenExpired(token: String, bufferSeconds: Long = 0): Boolean {
        return try {
            val expirationTime = getExpirationTimeFromToken(token) ?: return true
            val currentTime = System.currentTimeMillis() / 1000
            (expirationTime - bufferSeconds) <= currentTime
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * Extract role from JWT token.
     * 
     * @param token JWT access token
     * @return user role from token, or null if invalid
     */
    fun getRoleFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val jsonObject = JSONObject(payload)

            jsonObject.optString("role")?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

