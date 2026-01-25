package tn.petcare_android.viewmodel.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.AppUser
import tn.petcare_android.data.model.map.VetLocation
import tn.petcare_android.data.model.map.SitterLocation
import tn.petcare_android.data.model.map.TrainerLocation
import tn.petcare_android.data.model.map.SalonLocation
import tn.petcare_android.data.model.trainers.Trainer

/**
 * Map ViewModel for fetching and displaying vet/sitter locations
 * iOS Reference: MapViewModel.swift
 */
class MapViewModel : ViewModel() {

    private val vetSitterApi = RetrofitInstance.vetSitterApi
    private val trainersApi = RetrofitInstance.trainersApi
    private val salonApi = RetrofitInstance.salonApi

    private val _vetLocations = MutableStateFlow<List<VetLocation>>(emptyList())
    val vetLocations: StateFlow<List<VetLocation>> = _vetLocations.asStateFlow()

    private val _sitterLocations = MutableStateFlow<List<SitterLocation>>(emptyList())
    val sitterLocations: StateFlow<List<SitterLocation>> = _sitterLocations.asStateFlow()

    private val _trainerLocations = MutableStateFlow<List<TrainerLocation>>(emptyList())
    val trainerLocations: StateFlow<List<TrainerLocation>> = _trainerLocations.asStateFlow()

    private val _salonLocations = MutableStateFlow<List<SalonLocation>>(emptyList())
    val salonLocations: StateFlow<List<SalonLocation>> = _salonLocations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadLocations()
    }

    /**
     * Load vets, sitters, and trainers from backend
     * iOS Reference: MapViewModel.swift lines 19-42
     */
    fun loadLocations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Fetch vets, sitters, and trainers in parallel
                val vets = try {
                    vetSitterApi.getAllVets()
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching vets: ${e.message}", e)
                    emptyList()
                }
                
                val sitters = try {
                    vetSitterApi.getAllSitters()
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching sitters: ${e.message}", e)
                    emptyList()
                }

                val trainersResponse = try {
                    trainersApi.getAllTrainers()
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching trainers: ${e.message}", e)
                    null
                }

                val salons = try {
                    salonApi.getAllSalons()
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching salons: ${e.message}", e)
                    emptyList()
                }

                // Convert AppUser to VetLocation (iOS Reference: MapViewModel.swift lines 31-47)
                _vetLocations.value = vets
                    .filter { 
                        val lat = it.latitude ?: 0.0
                        val lon = it.longitude ?: 0.0
                        // Only filter out if both are exactly 0.0 (invalid coordinates)
                        !(lat == 0.0 && lon == 0.0)
                    }
                    .map { vet ->
                        val lat = vet.latitude ?: 0.0
                        val lon = vet.longitude ?: 0.0
                        
                        // Extract address (iOS Reference: MapViewModel.swift extractAddress)
                        val address = when {
                            !vet.vetAddress.isNullOrBlank() -> vet.vetAddress!!
                            !vet.city.isNullOrBlank() && !vet.country.isNullOrBlank() -> "${vet.city}, ${vet.country}"
                            !vet.city.isNullOrBlank() -> vet.city!!
                            !vet.country.isNullOrBlank() -> vet.country!!
                            else -> vet.email.substringAfter("@", "Unknown location")
                        }
                        
                        VetLocation(
                            id = vet.id,
                            userId = vet.id,
                            name = vet.vetClinicName ?: vet.name ?: vet.email.substringBefore("@", "Veterinarian"),
                            address = address,
                            latitude = lat,
                            longitude = lon,
                            isAvailable = vet.vetEmergencyAvailable ?: true, // Use emergency available as availability
                            appUser = vet
                        )
                    }

                // Convert AppUser to SitterLocation (iOS Reference: MapViewModel.swift lines 50-66)
                _sitterLocations.value = sitters
                    .filter { 
                        val lat = it.latitude ?: 0.0
                        val lon = it.longitude ?: 0.0
                        // Only filter out if both are exactly 0.0 (invalid coordinates)
                        !(lat == 0.0 && lon == 0.0)
                    }
                    .map { sitter ->
                        val lat = sitter.latitude ?: 0.0
                        val lon = sitter.longitude ?: 0.0
                        
                        // Extract address (iOS Reference: MapViewModel.swift extractAddress)
                        val address = when {
                            !sitter.sitterAddress.isNullOrBlank() -> sitter.sitterAddress!!
                            !sitter.city.isNullOrBlank() && !sitter.country.isNullOrBlank() -> "${sitter.city}, ${sitter.country}"
                            !sitter.city.isNullOrBlank() -> sitter.city!!
                            !sitter.country.isNullOrBlank() -> sitter.country!!
                            else -> sitter.email.substringAfter("@", "Unknown location")
                        }
                        
                        SitterLocation(
                            id = sitter.id,
                            userId = sitter.id,
                            name = sitter.name ?: sitter.email.substringBefore("@", "Pet Sitter"),
                            address = address,
                            latitude = lat,
                            longitude = lon,
                            isAvailable = sitter.availableWeekends ?: true, // Use availableWeekends as availability indicator
                            appUser = sitter
                        )
                    }

                // Convert trainers to TrainerLocation
                val trainers = trainersResponse?.body() ?: emptyList()
                _trainerLocations.value = trainers
                    .filter { 
                        val lat = it.latitude ?: 0.0
                        val lon = it.longitude ?: 0.0
                        !(lat == 0.0 && lon == 0.0)
                    }
                    .map { trainer ->
                        val lat = trainer.latitude ?: 0.0
                        val lon = trainer.longitude ?: 0.0
                        
                        // Extract address
                        val address = when {
                            !trainer.location.isNullOrBlank() -> trainer.location!!
                            else -> trainer.email?.substringAfter("@", "Unknown location") ?: "Unknown location"
                        }
                        
                        TrainerLocation(
                            id = trainer.id ?: "",
                            userId = trainer.id ?: "",
                            name = trainer.name ?: trainer.email?.substringBefore("@", "Trainer") ?: "Trainer",
                            address = address,
                            latitude = lat,
                            longitude = lon,
                            isAvailable = trainer.isAvailable ?: true,
                            specialization = trainer.specialization,
                            hourlyRate = trainer.hourlyRate
                        )
                    }

                // Convert AppUser to SalonLocation
                _salonLocations.value = salons
                    .filter { 
                        val lat = it.latitude ?: 0.0
                        val lon = it.longitude ?: 0.0
                        !(lat == 0.0 && lon == 0.0)
                    }
                    .map { salon ->
                        val lat = salon.latitude ?: 0.0
                        val lon = salon.longitude ?: 0.0
                        
                        // Extract address
                        val address = when {
                            !salon.salonAddress.isNullOrBlank() -> salon.salonAddress!!
                            !salon.city.isNullOrBlank() && !salon.country.isNullOrBlank() -> "${salon.city}, ${salon.country}"
                            !salon.city.isNullOrBlank() -> salon.city!!
                            !salon.country.isNullOrBlank() -> salon.country!!
                            else -> salon.email.substringAfter("@", "Unknown location")
                        }
                        
                        SalonLocation(
                            id = salon.id,
                            userId = salon.id,
                            name = salon.salonName ?: salon.name ?: salon.email.substringBefore("@", "Pet Salon"),
                            address = address,
                            latitude = lat,
                            longitude = lon,
                            isAvailable = true, // Salons are generally available
                            appUser = salon
                        )
                    }

                Log.d("MapViewModel", "Loaded ${_vetLocations.value.size} vets, ${_sitterLocations.value.size} sitters, ${_trainerLocations.value.size} trainers, and ${_salonLocations.value.size} salons")

            } catch (e: Exception) {
                Log.e("MapViewModel", "Error loading locations: ${e.message}", e)
                _error.value = "Failed to load locations: ${e.message}"
                // Keep existing locations on error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshLocations() {
        loadLocations()
    }
}
