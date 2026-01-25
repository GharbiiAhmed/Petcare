package tn.petcare_android.viewmodel.salon

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.AppUser
import tn.petcare_android.data.model.salon.SalonCard
import tn.petcare_android.ui.theme.VetCanyon
import kotlin.random.Random

class SalonListViewModel : ViewModel() {

    private val _salons = MutableStateFlow<List<SalonCard>>(emptyList())
    val salons: StateFlow<List<SalonCard>> = _salons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val salonApi = RetrofitInstance.salonApi

    init {
        loadSalons()
    }

    fun loadSalons() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val appUsers = salonApi.getAllSalons()
                _salons.value = appUsers.map { mapToSalonCard(it) }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load salons"
                android.util.Log.e("SalonListViewModel", "Failed to load salons: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapToSalonCard(user: AppUser): SalonCard {
        val services = user.salonServices?.takeIf { it.isNotEmpty() }
            ?: listOf("Grooming", "General Services")

        val distanceKm = Random.nextDouble(1.0, 10.0)

        val rating = 4.5
        val reviews = 0

        val isOpen = true

        val displayName = user.salonName
            ?: user.name
            ?: user.email.split("@").firstOrNull()
            ?: "Pet Salon"

        return SalonCard(
            id = user.id,
            name = displayName,
            services = services,
            rating = rating,
            reviews = reviews,
            distanceKm = distanceKm,
            isOpen = isOpen,
            tint = Color(0xFF9C27B0),
            emoji = "✂️",
            userId = user.id,
            appUser = user
        )
    }
}
