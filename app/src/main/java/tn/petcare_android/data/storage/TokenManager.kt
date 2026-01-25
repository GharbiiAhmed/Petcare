package tn.petcare_android.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_VERSION_KEY = stringPreferencesKey("token_version")
        private val LAST_LOGIN_TIME_KEY = stringPreferencesKey("last_login_time")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, version: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            if (version != null) {
                prefs[TOKEN_VERSION_KEY] = version
            }
            prefs[LAST_LOGIN_TIME_KEY] = System.currentTimeMillis().toString()
        }
    }

    fun getAccessToken(): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN_KEY]
    }

    fun getRefreshToken(): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[REFRESH_TOKEN_KEY]
    }

    fun getTokenVersion(): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_VERSION_KEY]
    }

    fun getLastLoginTime(): Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_LOGIN_TIME_KEY]?.toLongOrNull() ?: 0L
    }

    suspend fun clearTokens() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun hasValidToken(): Boolean {
        val accessToken = getAccessToken().firstOrNull()
        val refreshToken = getRefreshToken().firstOrNull()
        return !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
    }
}
