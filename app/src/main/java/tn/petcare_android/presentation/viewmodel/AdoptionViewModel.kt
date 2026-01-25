package tn.petcare_android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.*
import tn.petcare_android.data.model.adoption.AdoptionListing
import tn.petcare_android.data.model.adoption.AdoptionApplication
import tn.petcare_android.data.repository.AdoptionRepository

class AdoptionViewModel : ViewModel() {

    private val adoptionRepository = AdoptionRepository(RetrofitInstance.adoptionApi)

    private val _listings = MutableLiveData<List<AdoptionListing>>()
    val listings: LiveData<List<AdoptionListing>> = _listings

    private val _selectedListing = MutableLiveData<AdoptionListing?>()
    val selectedListing: LiveData<AdoptionListing?> = _selectedListing

    private val _rescuerListings = MutableLiveData<List<AdoptionListing>>()
    val rescuerListings: LiveData<List<AdoptionListing>> = _rescuerListings

    private val _applications = MutableLiveData<List<AdoptionApplication>>()
    val applications: LiveData<List<AdoptionApplication>> = _applications

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getAllListings(status: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.getAllListings(status)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _listings.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load listings"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun searchListings(query: String?, species: String?) {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.searchListings(query, species)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _listings.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Search failed"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getListing(listingId: String) {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.getListing(listingId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _selectedListing.value = response.body()
                    } else {
                        _error.value = "Failed to load listing"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getRescuerListings(rescuerId: String, status: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.getRescuerListings(rescuerId, status)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _rescuerListings.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load listings"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun createListing(request: CreateAdoptionListingRequest) {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.createListing(request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Listing created successfully"
                    } else {
                        _error.value = "Failed to create listing"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun toggleLike(listingId: String) {
        viewModelScope.launch {
            adoptionRepository.toggleLike(listingId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Like toggled"
                    } else {
                        _error.value = "Failed to toggle like"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun createApplication(listingId: String, request: CreateAdoptionApplicationRequest) {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.createApplication(listingId, request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Application submitted successfully"
                    } else {
                        _error.value = "Failed to submit application"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getRescuerApplications() {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.getRescuerApplications()
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _applications.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load applications"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getApplicantApplications() {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.getApplicantApplications()
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _applications.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load applications"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun approveApplication(applicationId: String) {
        viewModelScope.launch {
            adoptionRepository.approveApplication(applicationId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Application approved"
                    } else {
                        _error.value = "Failed to approve application"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun rejectApplication(applicationId: String, reason: String) {
        viewModelScope.launch {
            val request = RejectApplicationRequest(reason)
            adoptionRepository.rejectApplication(applicationId, request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Application rejected"
                    } else {
                        _error.value = "Failed to reject application"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun completeApplication(applicationId: String) {
        viewModelScope.launch {
            adoptionRepository.completeApplication(applicationId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Application completed"
                    } else {
                        _error.value = "Failed to complete application"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun deleteListing(listingId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            adoptionRepository.deleteListing(listingId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Listing deleted successfully"
                        onSuccess()
                        // Refresh listings
                        getAllListings()
                    } else {
                        _error.value = "Failed to delete listing"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }
}
