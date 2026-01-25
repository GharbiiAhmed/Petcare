package tn.petcare_android.viewmodel.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.petcare_android.data.repository.ProfileRepository
import tn.petcare_android.data.repository.PetsRepository
import tn.petcare_android.data.repository.UserRepository
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.storage.UserManager
import tn.petcare_android.data.model.auth.User
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.data.model.profile.UpdateProfileRequest
import tn.petcare_android.util.JwtDecoder
import java.io.File

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val user: User, val pets: List<Pet>) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object UserDeleted : ProfileUiState()
}

sealed class ProfileAction {
    object Idle : ProfileAction()
    object Loading : ProfileAction()
    data class Success(val message: String) : ProfileAction()
    data class Error(val message: String) : ProfileAction()
}

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val petsRepository: PetsRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _actionState = MutableStateFlow<ProfileAction>(ProfileAction.Idle)
    val actionState: StateFlow<ProfileAction> = _actionState

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    init {
        loadProfile()
    }

    private suspend fun getUserIdFromToken(): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (token.isNullOrBlank()) {
            null
        } else {
            JwtDecoder.getUserIdFromToken(token)
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // Fetch user profile by ID
                val userResponse = userRepository.getUserById(userId)
                if (!userResponse.isSuccessful) {
                    _uiState.value = ProfileUiState.Error(
                        userResponse.errorBody()?.string() ?: "Failed to load profile"
                    )
                    return@launch
                }

                val user = userResponse.body()
                if (user == null) {
                    _uiState.value = ProfileUiState.Error("No profile data received")
                    return@launch
                }

                // Fetch pets separately
                val petsResponse = petsRepository.getPetsByOwner(userId)
                val pets = if (petsResponse.isSuccessful) {
                    petsResponse.body() ?: emptyList()
                } else {
                    emptyList()
                }

                _uiState.value = ProfileUiState.Success(user, pets)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _uiState.value = ProfileUiState.Error("User not authenticated. Please login again.")
                    return@launch
                }

                // Fetch user profile
                val userResponse = repository.getProfile(userId)
                if (!userResponse.isSuccessful) {
                    if (userResponse.code() == 404) {
                        tokenManager.clearTokens()
                        userManager.clearUserId()
                        _uiState.value = ProfileUiState.UserDeleted
                    } else {
                        _uiState.value = ProfileUiState.Error(
                            userResponse.errorBody()?.string() ?: "Failed to load profile"
                        )
                    }
                    return@launch
                }

                val user = userResponse.body()
                if (user == null) {
                    _uiState.value = ProfileUiState.Error("No profile data received")
                    return@launch
                }

                // Fetch pets separately
                val petsResponse = petsRepository.getPetsByOwner(userId)
                val pets = if (petsResponse.isSuccessful) {
                    petsResponse.body() ?: emptyList()
                } else {
                    emptyList() // If pets fetch fails, just show empty list
                }

                _uiState.value = ProfileUiState.Success(
                    user = user,
                    pets = pets
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }


    fun updateProfileWithImage(
        name: String? = null,
        phoneNumber: String? = null,
        country: String? = null,
        city: String? = null,
        hasPhoto: Boolean? = null,
        hasPets: Boolean? = null,
        photoFile: File? = null
    ) {
        viewModelScope.launch {
            _actionState.value = ProfileAction.Loading
            try {
                Log.d(TAG, "Updating profile with name: $name, phoneNumber: $phoneNumber, country: $country, city: $city, photoFile: ${photoFile?.name}")

                val response = userRepository.updateProfile(
                    name = name,
                    phoneNumber = phoneNumber,
                    country = country,
                    city = city,
                    hasPhoto = hasPhoto,
                    hasPets = hasPets,
                    photoFile = photoFile
                )

                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d(TAG, "Profile updated successfully: ${user?.id}, photo: ${user?.profileImage}")
                    // Clean up temp file
                    photoFile?.delete()
                    _actionState.value = ProfileAction.Success("Profile updated successfully!")
                    loadProfile()
                } else {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to update profile: $errorBody")
                    // Clean up temp file
                    photoFile?.delete()

                    if (code == 401) {
                        // Session expired or invalid token; clear and force logout
                        tokenManager.clearTokens()
                        userManager.clearUserId()
                        _uiState.value = ProfileUiState.UserDeleted
                        _actionState.value = ProfileAction.Error("Session expired. Please log in again.")
                    } else {
                        _actionState.value = ProfileAction.Error(
                            errorBody ?: "Failed to update profile"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception updating profile", e)
                photoFile?.delete()
                _actionState.value = ProfileAction.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _actionState.value = ProfileAction.Loading
            try {
                Log.d(TAG, "Deleting account")

                val response = userRepository.deleteAccount()

                Log.d(TAG, "Delete response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d(TAG, "Account deleted successfully")
                    // Clear all local data
                    tokenManager.clearTokens()
                    userManager.clearUserId()
                    _actionState.value = ProfileAction.Success("Account deleted successfully")
                    _uiState.value = ProfileUiState.UserDeleted
                } else {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to delete account: $errorBody")

                    if (code == 401) {
                        tokenManager.clearTokens()
                        userManager.clearUserId()
                        _uiState.value = ProfileUiState.UserDeleted
                        _actionState.value = ProfileAction.Error("Session expired. Please log in again.")
                    } else {
                        _actionState.value = ProfileAction.Error(
                            errorBody ?: "Failed to delete account"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception deleting account", e)
                _actionState.value = ProfileAction.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }


    fun resetActionState() {
        _actionState.value = ProfileAction.Idle
    }
}
