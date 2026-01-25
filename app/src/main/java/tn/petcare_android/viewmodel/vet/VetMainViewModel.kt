package tn.petcare_android.viewmodel.vet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.booking.Booking
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.screens.role.VetPatient
import tn.petcare_android.ui.screens.role.VetAppointment
import java.text.SimpleDateFormat
import java.util.*

sealed class VetMainUiState {
    object Idle : VetMainUiState()
    object Loading : VetMainUiState()
    data class Success(val message: String) : VetMainUiState()
    data class Error(val message: String) : VetMainUiState()
}

class VetMainViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<VetMainUiState>(VetMainUiState.Idle)
    val uiState: StateFlow<VetMainUiState> = _uiState

    private val _patients = MutableStateFlow<List<VetPatient>>(emptyList())
    val patients: StateFlow<List<VetPatient>> = _patients

    private val _appointments = MutableStateFlow<List<VetAppointment>>(emptyList())
    val appointments: StateFlow<List<VetAppointment>> = _appointments

    fun loadPatients() {
        viewModelScope.launch {
            _uiState.value = VetMainUiState.Loading
            try {
                // Fetch bookings where user is provider and providerType is 'vet'
                val bookings = RetrofitInstance.bookingApi.getBookings(role = "provider")
                val vetBookings = bookings.filter { it.providerType == "vet" }
                
                // Group bookings by owner to create patient list
                val patientsMap = mutableMapOf<String, VetPatient>()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                
                vetBookings.forEach { booking ->
                    val ownerId = booking.normalizedOwnerId
                    val ownerName = booking.owner?.name ?: "Unknown Owner"
                    val petName = booking.pet?.name ?: "Unknown Pet"
                    val petId = booking.normalizedPetId ?: booking.petId
                    
                    // Use petId as key to group by pet (not owner)
                    // This ensures each pet is a separate patient entry
                    if (petId != null && !patientsMap.containsKey(petId)) {
                        // Find the most recent booking for this pet
                        val petBookings = vetBookings.filter { 
                            (it.normalizedPetId ?: it.petId) == petId
                        }
                        val lastBooking = petBookings.maxByOrNull { 
                            it.dateTime?.let { dateFormat.parse(it)?.time ?: 0L } ?: 0L
                        }
                        val lastVisit = lastBooking?.dateTime?.substringBefore("T") ?: ""
                        
                        // Check if there are pending bookings that need attention
                        val needsAttention = petBookings.any { 
                            it.status == "pending" || it.status == "accepted"
                        }
                        
                        patientsMap[petId] = VetPatient(
                            id = petId, // Use pet ID, not owner ID
                            petName = petName,
                            ownerName = ownerName,
                            ownerId = ownerId,
                            needsAttention = if (needsAttention) "Has pending appointments" else "",
                            lastVisit = lastVisit
                        )
                    }
                }
                
                _patients.value = patientsMap.values.toList()
                _uiState.value = VetMainUiState.Success("Patients loaded")
            } catch (e: Exception) {
                _uiState.value = VetMainUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _uiState.value = VetMainUiState.Loading
            try {
                // Fetch bookings where user is provider and providerType is 'vet'
                val bookings = RetrofitInstance.bookingApi.getBookings(role = "provider")
                val vetBookings = bookings.filter { it.providerType == "vet" }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val displayDateFormat = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault())
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                _appointments.value = vetBookings.map { booking ->
                    val dateTimeStr = booking.dateTime ?: ""
                    val isToday = dateTimeStr.startsWith(today)
                    
                    VetAppointment(
                        id = booking.normalizedId,
                        petName = booking.pet?.name ?: "Unknown Pet",
                        ownerName = booking.owner?.name ?: "Unknown Owner",
                        ownerId = booking.normalizedOwnerId,
                        dateTime = try {
                            dateFormat.parse(dateTimeStr)?.let { displayDateFormat.format(it) } ?: dateTimeStr
                        } catch (e: Exception) {
                            dateTimeStr
                        },
                        reason = booking.serviceType ?: booking.description ?: "Appointment",
                        isToday = isToday
                    )
                }.sortedBy { it.dateTime }
                
                _uiState.value = VetMainUiState.Success("Appointments loaded")
            } catch (e: Exception) {
                _uiState.value = VetMainUiState.Error(e.message ?: "Network error")
            }
        }
    }
}

class VetMainViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VetMainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VetMainViewModel(TokenManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

