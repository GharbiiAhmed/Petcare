package tn.petcare_android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.*
import tn.petcare_android.data.model.marketplace.MarketplaceListing
import tn.petcare_android.data.model.marketplace.MarketplaceInquiry
import tn.petcare_android.data.repository.MarketplaceRepository

class MarketplaceViewModel : ViewModel() {

    private val marketplaceRepository = MarketplaceRepository(RetrofitInstance.marketplaceApi)

    private val _listings = MutableLiveData<List<MarketplaceListing>>()
    val listings: LiveData<List<MarketplaceListing>> = _listings

    private val _selectedListing = MutableLiveData<MarketplaceListing?>()
    val selectedListing: LiveData<MarketplaceListing?> = _selectedListing

    private val _userListings = MutableLiveData<List<MarketplaceListing>>()
    val userListings: LiveData<List<MarketplaceListing>> = _userListings

    private val _inquiries = MutableLiveData<List<MarketplaceInquiry>>()
    val inquiries: LiveData<List<MarketplaceInquiry>> = _inquiries

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getAllListings(status: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.getAllListings(status)
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

    fun searchListings(
        query: String?,
        species: String?,
        minPrice: Int?,
        maxPrice: Int?
    ) {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.searchListings(query, species, minPrice, maxPrice)
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
            marketplaceRepository.getListing(listingId)
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

    fun getUserListings(userId: String, status: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.getUserListings(userId, status)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _userListings.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load your listings"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun createListing(request: CreateMarketplaceListingRequest) {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.createListing(request)
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
            marketplaceRepository.toggleLike(listingId)
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

    fun createInquiry(listingId: String, request: CreateMarketplaceInquiryRequest) {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.createInquiry(listingId, request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Inquiry sent successfully"
                    } else {
                        _error.value = "Failed to send inquiry"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getSellerInquiries() {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.getSellerInquiries()
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _inquiries.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load inquiries"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getBuyerInquiries() {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.getBuyerInquiries()
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _inquiries.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load inquiries"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun acceptInquiry(inquiryId: String) {
        viewModelScope.launch {
            marketplaceRepository.acceptInquiry(inquiryId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Inquiry accepted"
                    } else {
                        _error.value = "Failed to accept inquiry"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun rejectInquiry(inquiryId: String, reason: String) {
        viewModelScope.launch {
            val request = RejectInquiryRequest(reason)
            marketplaceRepository.rejectInquiry(inquiryId, request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Inquiry rejected"
                    } else {
                        _error.value = "Failed to reject inquiry"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun completeInquiry(inquiryId: String) {
        viewModelScope.launch {
            marketplaceRepository.completeInquiry(inquiryId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Inquiry completed"
                    } else {
                        _error.value = "Failed to complete inquiry"
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
            marketplaceRepository.deleteListing(listingId)
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
