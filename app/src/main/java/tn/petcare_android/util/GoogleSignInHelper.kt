package tn.petcare_android.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import tn.petcare_android.BuildConfig
import java.security.MessageDigest
import java.util.UUID

class GoogleSignInHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSignInHelper"
        // Web Client ID is loaded from local.properties (not committed to git)
        private val WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID
    }

    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): Result<String> {
        return try {
            // Generate a nonce for security
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            // Configure Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false) // Changed to false to show picker even with one account
                .build()

            // Build credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Get credential
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )

            // Handle the credential
            handleSignIn(result)
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No Google accounts found on device", e)
            Result.failure(Exception("No Google accounts found. Please add a Google account in device settings."))
        } catch (e: GetCredentialCancellationException) {
            Log.e(TAG, "User cancelled Google Sign-In", e)
            Result.failure(Exception("Sign-in cancelled"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException: ${e.message}", e)
            val errorMsg = when {
                e.message?.contains("No credentials available") == true ->
                    "No Google accounts found. Please add a Google account to your device."
                e.message?.contains("cancelled") == true ->
                    "Sign-in was cancelled"
                else ->
                    "Google Sign-In failed: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            Result.failure(e)
        }
    }

    private fun handleSignIn(result: GetCredentialResponse): Result<String> {
        return when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "Successfully got Google ID token")
                        Result.success(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token response", e)
                        Result.failure(Exception("Invalid Google credential"))
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    Result.failure(Exception("Unexpected credential type"))
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential type")
                Result.failure(Exception("Unexpected credential type"))
            }
        }
    }
}

