package tn.petcare_android.viewmodel.vet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.AppUser

class VetProfileViewModel : ViewModel() {

    private val _vet = MutableStateFlow<AppUser?>(null)
    val vet: StateFlow<AppUser?> = _vet.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val vetSitterApi = RetrofitInstance.vetSitterApi

    fun loadVet(vetId: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = vetSitterApi.getVet(vetId)
                if (response.isSuccessful) {
                    _vet.value = response.body()
                } else {
                    _error.value = "Failed to load vet profile: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load vet profile"
                println("❌ Failed to load vet profile: $e")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry(vetId: String) {
        loadVet(vetId)
    }
}
