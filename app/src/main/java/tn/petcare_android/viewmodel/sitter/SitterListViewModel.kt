package tn.petcare_android.viewmodel.sitter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.AppUser
import tn.petcare_android.data.model.sitter.SitterCard
import tn.petcare_android.ui.theme.VetCanyon

class SitterListViewModel : ViewModel() {

    private val _sitters = MutableStateFlow<List<SitterCard>>(emptyList())
    val sitters: StateFlow<List<SitterCard>> = _sitters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val vetSitterApi = RetrofitInstance.vetSitterApi
    private val userApi = RetrofitInstance.userApi

    init {
        loadSitters()
    }

    fun loadSitters() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val appUsers = try {
                    // Try the dedicated pet-sitters endpoint first
                    vetSitterApi.getAllSitters()
                } catch (e: HttpException) {
                    // If 404, fallback to users endpoint and filter by role
                    if (e.code() == 404) {
                        android.util.Log.w("SitterListViewModel", "pet-sitters endpoint returned 404, using users fallback")
                        try {
                            val allUsers = userApi.getAllUsers()
                            // Filter users with role='sitter'
                            allUsers.filter { it.role?.lowercase() == "sitter" }
                        } catch (fallbackError: Exception) {
                            android.util.Log.e("SitterListViewModel", "Error fetching users fallback: ${fallbackError.message}", fallbackError)
                            emptyList()
                        }
                    } else {
                        android.util.Log.e("SitterListViewModel", "Error fetching sitters: ${e.message}", e)
                        emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SitterListViewModel", "Error fetching sitters: ${e.message}", e)
                    emptyList()
                }
                _sitters.value = appUsers.map { mapToSitter(it) }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load sitters"
                android.util.Log.e("SitterListViewModel", "Error loading sitters: ${e.message}", e)
                // Keep existing sitters on error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapToSitter(user: AppUser): SitterCard {
        val displayName = user.name ?: user.email.split("@").firstOrNull() ?: "Pet Sitter"

        val service = user.services?.joinToString(" · ")?.takeIf { it.isNotEmpty() } ?: ""

        val location = listOfNotNull(user.city, user.country)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(", ")

        val description = user.bio ?: location

        // Use actual rating from backend if available, otherwise 0.0
        val rating = 0.0 // Will be updated when backend provides rating data

        return SitterCard(
            id = user.id,
            name = displayName,
            service = service,
            description = description,
            rating = rating,
            reviews = 0, // Will be updated when backend provides review count
            emoji = "", // Remove static emoji
            tint = VetCanyon.copy(alpha = 0.25f),
            userId = user.id,
            appUser = user
        )
    }
}
