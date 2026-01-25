package tn.petcare_android.viewmodel.pet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.pet.AddPetRequest
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.data.repository.PetsRepository
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.util.JwtDecoder

sealed class PetUiState {
    object Idle : PetUiState()
    object Loading : PetUiState()
    data class Success(val pet: Pet? = null, val message: String = "Success") : PetUiState()
    data class Error(val message: String) : PetUiState()
}

class PetViewModel(
    private val repository: PetsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetUiState>(PetUiState.Idle)
    val uiState: StateFlow<PetUiState> = _uiState
    
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    companion object {
        private const val TAG = "PetViewModel"
    }

    private suspend fun getUserIdFromToken(): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (token.isNullOrBlank()) {
            null
        } else {
            JwtDecoder.getUserIdFromToken(token)
        }
    }
    
    fun loadPets() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    Log.e(TAG, "User not authenticated")
                    _loading.value = false
                    return@launch
                }

                val response = repository.getPetsByOwner(userId)
                if (response.isSuccessful) {
                    _pets.value = response.body() ?: emptyList()
                    Log.d(TAG, "Loaded ${_pets.value.size} pets")
                } else {
                    Log.e(TAG, "Failed to load pets: ${response.errorBody()?.string()}")
                    _pets.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading pets", e)
                _pets.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun addPet(
        name: String,
        species: String,
        breed: String? = null,
        age: Double? = null,
        gender: String? = null,
        color: String? = null,
        weight: Double? = null,
        height: Double? = null,
        photoFile: java.io.File? = null,
        microchipId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = PetUiState.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _uiState.value = PetUiState.Error("User not authenticated. Please login again.")
                    return@launch
                }

                Log.d(TAG, "Adding pet with photo file: ${photoFile?.name}")

                val response = repository.addPet(
                    ownerId = userId,
                    name = name,
                    species = species,
                    breed = breed,
                    age = age,
                    gender = gender,
                    color = color,
                    weight = weight,
                    height = height,
                    microchipId = microchipId,
                    photoFile = photoFile
                )

                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val pet = response.body()
                    Log.d(TAG, "Pet added successfully: ${pet?.id}, photo: ${pet?.photo}")
                    // Clean up temp file
                    photoFile?.delete()
                    _uiState.value = PetUiState.Success(pet, "Pet added successfully!")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to add pet: $errorBody")
                    // Clean up temp file
                    photoFile?.delete()

                    // Parse error message for better user feedback
                    val errorMessage = when {
                        errorBody?.contains("duplicate", ignoreCase = true) == true ||
                        errorBody?.contains("already exists", ignoreCase = true) == true ||
                        errorBody?.contains("microchip", ignoreCase = true) == true -> {
                            "This microchip ID is already registered. Please use a unique microchip ID."
                        }
                        errorBody?.contains("validation", ignoreCase = true) == true -> {
                            "Invalid pet data. Please check all fields."
                        }
                        response.code() == 400 -> {
                            "Invalid pet information. ${errorBody ?: "Please check your input."}"
                        }
                        response.code() == 409 -> {
                            "This microchip ID is already in use. Please use a different one."
                        }
                        else -> errorBody ?: "Failed to add pet. Please try again."
                    }

                    _uiState.value = PetUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception adding pet", e)
                photoFile?.delete()
                _uiState.value = PetUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = PetUiState.Idle
    }
}

