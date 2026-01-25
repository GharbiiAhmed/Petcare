package tn.petcare_android.viewmodel.vet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.AppUser
import tn.petcare_android.data.model.vet.VetCard
import tn.petcare_android.ui.theme.VetCanyon
import kotlin.random.Random

class VetListViewModel : ViewModel() {

    private val _vets = MutableStateFlow<List<VetCard>>(emptyList())
    val vets: StateFlow<List<VetCard>> = _vets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val vetSitterApi = RetrofitInstance.vetSitterApi

    init {
        loadVets()
    }

    fun loadVets() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val appUsers = vetSitterApi.getAllVets()
                _vets.value = appUsers.map { mapToVetCard(it) }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load vets"
                println("❌ Failed to load vets: $e")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapToVetCard(user: AppUser): VetCard {

        val specialties = user.vetSpecializations?.takeIf { it.isNotEmpty() }
            ?: listOf("General Practice")


        val distanceKm = Random.nextDouble(1.0, 10.0)


        val rating = 4.5
        val reviews = 0


        val isOpen = true
        val is247 = user.vetEmergencyAvailable ?: false


        val displayName = user.vetClinicName
            ?: user.name
            ?: user.email.split("@").firstOrNull()
            ?: "Veterinarian"

        return VetCard(
            id = user.id,
            name = displayName,
            specialties = specialties,
            rating = rating,
            reviews = reviews,
            distanceKm = distanceKm,
            is247 = is247,
            isOpen = isOpen,
            tint = VetCanyon,
            emoji = "🧑‍⚕️",
            userId = user.id,
            appUser = user
        )
    }
}
