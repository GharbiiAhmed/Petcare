package tn.petcare_android.viewmodel.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.pet.Pet
import tn.petcare_android.data.model.pet.UpdatePetRequest
import tn.petcare_android.data.repository.PetsRepository
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.util.JwtDecoder
import kotlinx.coroutines.flow.firstOrNull

sealed class PetDetailUiState {
    object Idle : PetDetailUiState()
    object Loading : PetDetailUiState()
    data class Success(val pet: Pet) : PetDetailUiState()
    data class Error(val message: String) : PetDetailUiState()
}

sealed class PetActionState {
    object Idle : PetActionState()
    object Loading : PetActionState()
    data class Success(val message: String) : PetActionState()
    data class Error(val message: String) : PetActionState()
}

class PetDetailViewModel(
    private val repository: PetsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetDetailUiState>(PetDetailUiState.Idle)
    val uiState: StateFlow<PetDetailUiState> = _uiState

    private val _actionState = MutableStateFlow<PetActionState>(PetActionState.Idle)
    val actionState: StateFlow<PetActionState> = _actionState

    private suspend fun getUserIdFromToken(): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (token.isNullOrBlank()) {
            null
        } else {
            JwtDecoder.getUserIdFromToken(token)
        }
    }

    fun loadPetDetails(petId: String) {
        viewModelScope.launch {
            _uiState.value = PetDetailUiState.Loading
            try {
                val response = repository.getPetById(petId)
                if (response.isSuccessful) {
                    val pet = response.body()
                    if (pet != null) {
                        _uiState.value = PetDetailUiState.Success(pet)
                    } else {
                        _uiState.value = PetDetailUiState.Error("Pet not found")
                    }
                } else {
                    _uiState.value = PetDetailUiState.Error(
                        response.errorBody()?.string() ?: "Failed to load pet details"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PetDetailUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun updatePet(petId: String, pet: Pet, photoFile: java.io.File? = null) {
        viewModelScope.launch {
            _actionState.value = PetActionState.Loading
            try {
                val response = repository.updatePet(
                    petId = petId,
                    name = pet.name,
                    species = pet.species,
                    breed = pet.breed,
                    age = pet.age,
                    gender = pet.gender,
                    color = pet.color,
                    weight = pet.weight,
                    height = pet.height,
                    microchipId = pet.microchipId,
                    photoFile = photoFile
                )

                if (response.isSuccessful) {
                    // Clean up temp file
                    photoFile?.delete()
                    _actionState.value = PetActionState.Success("Pet updated successfully!")
                    // Reload pet details
                    loadPetDetails(petId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    // Clean up temp file
                    photoFile?.delete()

                    // Parse error message for better user feedback
                    val errorMessage = when {
                        errorBody?.contains("duplicate", ignoreCase = true) == true ||
                        errorBody?.contains("already exists", ignoreCase = true) == true ||
                        errorBody?.contains("microchip", ignoreCase = true) == true -> {
                            "This microchip ID is already registered to another pet."
                        }
                        response.code() == 409 -> {
                            "This microchip ID is already in use."
                        }
                        else -> errorBody ?: "Failed to update pet"
                    }

                    _actionState.value = PetActionState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _actionState.value = PetActionState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            _actionState.value = PetActionState.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _actionState.value = PetActionState.Error("User not authenticated")
                    return@launch
                }

                val response = repository.deletePet(userId, petId)
                if (response.isSuccessful) {
                    _actionState.value = PetActionState.Success("Pet deleted successfully!")
                } else {
                    _actionState.value = PetActionState.Error(
                        response.errorBody()?.string() ?: "Failed to delete pet"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = PetActionState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun resetActionState() {
        _actionState.value = PetActionState.Idle
    }
}

