package tn.petcare_android.viewmodel.sitter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.booking.Booking
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.screens.role.SittingRequest
import tn.petcare_android.ui.screens.role.SittingBooking
import tn.petcare_android.util.JwtDecoder
import java.text.SimpleDateFormat
import java.util.*

sealed class PetSitterMainUiState {
    object Idle : PetSitterMainUiState()
    object Loading : PetSitterMainUiState()
    data class Success(val message: String) : PetSitterMainUiState()
    data class Error(val message: String) : PetSitterMainUiState()
}

class PetSitterMainViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetSitterMainUiState>(PetSitterMainUiState.Idle)
    val uiState: StateFlow<PetSitterMainUiState> = _uiState

    private val _sittingRequests = MutableStateFlow<List<SittingRequest>>(emptyList())
    val sittingRequests: StateFlow<List<SittingRequest>> = _sittingRequests

    private val _activeBookings = MutableStateFlow<List<SittingBooking>>(emptyList())
    val activeBookings: StateFlow<List<SittingBooking>> = _activeBookings

    private suspend fun getUserId(): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (token.isNullOrBlank()) null else JwtDecoder.getUserIdFromToken(token)
    }

    fun loadSittingRequests() {
        viewModelScope.launch {
            _uiState.value = PetSitterMainUiState.Loading
            try {
                // Fetch bookings where user is provider and providerType is 'sitter'
                val bookings = RetrofitInstance.bookingApi.getBookings(role = "provider")
                val sitterBookings = bookings.filter { it.providerType == "sitter" }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                
                _sittingRequests.value = sitterBookings.map { booking ->
                    val dateTimeStr = booking.dateTime ?: ""
                    val startDate = dateTimeStr.substringBefore("T")
                    val endDate = if (booking.duration != null) {
                        try {
                            val startDateObj = dateFormat.parse(dateTimeStr)
                            val calendar = Calendar.getInstance()
                            calendar.time = startDateObj
                            calendar.add(Calendar.MINUTE, booking.duration)
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                        } catch (e: Exception) {
                            startDate
                        }
                    } else {
                        startDate
                    }
                    
                    SittingRequest(
                        id = booking.normalizedId,
                        petName = booking.pet?.name ?: "Unknown Pet",
                        ownerName = booking.owner?.name ?: "Unknown Owner",
                        ownerId = booking.normalizedOwnerId,
                        startDate = startDate,
                        endDate = endDate,
                        hourlyRate = booking.price ?: 0.0,
                        status = booking.status ?: "pending"
                    )
                }.sortedBy { it.startDate }
                
                _uiState.value = PetSitterMainUiState.Success("Sitting requests loaded")
            } catch (e: Exception) {
                _uiState.value = PetSitterMainUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadActiveBookings() {
        viewModelScope.launch {
            _uiState.value = PetSitterMainUiState.Loading
            try {
                // Fetch bookings where user is provider, providerType is 'sitter', and status is 'accepted'
                val bookings = RetrofitInstance.bookingApi.getBookings(role = "provider")
                val activeSitterBookings = bookings.filter { 
                    it.providerType == "sitter" && (it.status == "accepted" || it.status == "completed")
                }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                
                _activeBookings.value = activeSitterBookings.map { booking ->
                    val dateTimeStr = booking.dateTime ?: ""
                    val startDate = dateTimeStr.substringBefore("T")
                    val endDate = if (booking.duration != null) {
                        try {
                            val startDateObj = dateFormat.parse(dateTimeStr)
                            val calendar = Calendar.getInstance()
                            calendar.time = startDateObj
                            calendar.add(Calendar.MINUTE, booking.duration)
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                        } catch (e: Exception) {
                            startDate
                        }
                    } else {
                        startDate
                    }
                    
                    SittingBooking(
                        id = booking.normalizedId,
                        petName = booking.pet?.name ?: "Unknown Pet",
                        ownerName = booking.owner?.name ?: "Unknown Owner",
                        ownerId = booking.normalizedOwnerId,
                        startDate = startDate,
                        endDate = endDate,
                        status = booking.status ?: "active"
                    )
                }.sortedBy { it.startDate }
                
                _uiState.value = PetSitterMainUiState.Success("Active bookings loaded")
            } catch (e: Exception) {
                _uiState.value = PetSitterMainUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = PetSitterMainUiState.Loading
            try {
                // Update booking status to accepted
                val updateRequest = tn.petcare_android.data.model.booking.UpdateBookingRequest(
                    status = "accepted"
                )
                RetrofitInstance.bookingApi.updateBooking(requestId, updateRequest)
                loadSittingRequests() // Refresh requests
                loadActiveBookings() // Refresh bookings
                _uiState.value = PetSitterMainUiState.Success("Request accepted")
            } catch (e: Exception) {
                _uiState.value = PetSitterMainUiState.Error(e.message ?: "Failed to accept request")
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = PetSitterMainUiState.Loading
            try {
                // Update booking status to rejected
                val updateRequest = tn.petcare_android.data.model.booking.UpdateBookingRequest(
                    status = "rejected",
                    rejectionReason = "Rejected by pet sitter"
                )
                RetrofitInstance.bookingApi.updateBooking(requestId, updateRequest)
                loadSittingRequests() // Refresh requests
                _uiState.value = PetSitterMainUiState.Success("Request rejected")
            } catch (e: Exception) {
                _uiState.value = PetSitterMainUiState.Error(e.message ?: "Failed to reject request")
            }
        }
    }
}

class PetSitterMainViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetSitterMainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PetSitterMainViewModel(TokenManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

