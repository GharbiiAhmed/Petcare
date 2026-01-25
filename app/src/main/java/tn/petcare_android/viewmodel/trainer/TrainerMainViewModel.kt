package tn.petcare_android.viewmodel.trainer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.screens.role.TrainingPet
import tn.petcare_android.ui.screens.role.TrainingEvent
import tn.petcare_android.util.JwtDecoder
import java.text.SimpleDateFormat
import java.util.*

sealed class TrainerMainUiState {
    object Idle : TrainerMainUiState()
    object Loading : TrainerMainUiState()
    data class Success(val message: String) : TrainerMainUiState()
    data class Error(val message: String) : TrainerMainUiState()
}

class TrainerMainViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrainerMainUiState>(TrainerMainUiState.Idle)
    val uiState: StateFlow<TrainerMainUiState> = _uiState

    private val _trainingPets = MutableStateFlow<List<TrainingPet>>(emptyList())
    val trainingPets: StateFlow<List<TrainingPet>> = _trainingPets

    private val _calendarEvents = MutableStateFlow<List<TrainingEvent>>(emptyList())
    val calendarEvents: StateFlow<List<TrainingEvent>> = _calendarEvents

    private suspend fun getUserId(): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (token.isNullOrBlank()) null else JwtDecoder.getUserIdFromToken(token)
    }

    fun loadTrainingPets() {
        viewModelScope.launch {
            _uiState.value = TrainerMainUiState.Loading
            try {
                val userId = getUserId()
                if (userId.isNullOrBlank()) {
                    _uiState.value = TrainerMainUiState.Error("User not authenticated")
                    return@launch
                }

                // Fetch trainer bookings
                val response = RetrofitInstance.trainersApi.getTrainerBookings(userId)
                if (response.isSuccessful) {
                    val bookings = response.body() as? List<Map<String, Any>> ?: emptyList()
                    
                    // Group bookings by pet to create training pets list
                    val petsMap = mutableMapOf<String, TrainingPet>()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    
                    bookings.forEach { booking ->
                        val petId = (booking["pet"] as? Map<*, *>)?.get("_id")?.toString() 
                            ?: (booking["pet"] as? Map<*, *>)?.get("id")?.toString() ?: ""
                        val petName = (booking["pet"] as? Map<*, *>)?.get("name")?.toString() ?: "Unknown Pet"
                        val ownerId = (booking["owner"] as? Map<*, *>)?.get("_id")?.toString()
                            ?: (booking["owner"] as? Map<*, *>)?.get("id")?.toString() ?: ""
                        val ownerName = (booking["owner"] as? Map<*, *>)?.get("name")?.toString() ?: "Unknown Owner"
                        val status = booking["status"]?.toString() ?: "pending"
                        val startDateTime = booking["startDateTime"]?.toString() ?: ""
                        val trainingType = booking["sessionType"]?.toString() ?: "Training"
                        val startDate = startDateTime.substringBefore("T")
                        
                        if (!petsMap.containsKey(petId) && petId.isNotEmpty()) {
                            petsMap[petId] = TrainingPet(
                                id = petId,
                                petName = petName,
                                ownerName = ownerName,
                                ownerId = ownerId,
                                status = when (status) {
                                    "completed" -> "completed"
                                    "confirmed" -> "active"
                                    "pending" -> "active"
                                    else -> "active"
                                },
                                startDate = startDate,
                                trainingType = trainingType
                            )
                        }
                    }
                    
                    _trainingPets.value = petsMap.values.toList()
                    _uiState.value = TrainerMainUiState.Success("Training pets loaded")
                } else {
                    _uiState.value = TrainerMainUiState.Error(
                        response.errorBody()?.string() ?: "Failed to load training pets"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TrainerMainUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadCalendarEvents() {
        viewModelScope.launch {
            _uiState.value = TrainerMainUiState.Loading
            try {
                val userId = getUserId()
                if (userId.isNullOrBlank()) {
                    _uiState.value = TrainerMainUiState.Error("User not authenticated")
                    return@launch
                }

                // Fetch trainer bookings
                val response = RetrofitInstance.trainersApi.getTrainerBookings(userId)
                if (response.isSuccessful) {
                    val bookings = response.body() as? List<Map<String, Any>> ?: emptyList()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val displayDateFormat = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault())
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    
                    // Calculate this week's range
                    val calendar = Calendar.getInstance()
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    calendar.add(Calendar.DAY_OF_MONTH, Calendar.SUNDAY - dayOfWeek)
                    val weekStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    calendar.add(Calendar.DAY_OF_MONTH, 6)
                    val weekEnd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    
                    _calendarEvents.value = bookings.mapNotNull { booking ->
                        val startDateTime = booking["startDateTime"]?.toString() ?: return@mapNotNull null
                        val petName = (booking["pet"] as? Map<*, *>)?.get("name")?.toString() ?: "Unknown Pet"
                        val ownerName = (booking["owner"] as? Map<*, *>)?.get("name")?.toString() ?: "Unknown Owner"
                        val ownerId = (booking["owner"] as? Map<*, *>)?.get("_id")?.toString()
                            ?: (booking["owner"] as? Map<*, *>)?.get("id")?.toString() ?: ""
                        val trainingType = booking["sessionType"]?.toString() ?: "Training"
                        val bookingId = booking["_id"]?.toString() ?: booking["id"]?.toString() ?: ""
                        
                        val dateStr = startDateTime.substringBefore("T")
                        val isToday = dateStr == today
                        val isThisWeek = dateStr >= weekStart && dateStr <= weekEnd
                        
                        val displayDateTime = try {
                            dateFormat.parse(startDateTime)?.let { displayDateFormat.format(it) } ?: startDateTime
                        } catch (e: Exception) {
                            startDateTime
                        }
                        
                        TrainingEvent(
                            id = bookingId,
                            petName = petName,
                            ownerName = ownerName,
                            ownerId = ownerId,
                            dateTime = displayDateTime,
                            trainingType = trainingType,
                            isToday = isToday,
                            isThisWeek = isThisWeek
                        )
                    }.sortedBy { it.dateTime }
                    
                    _uiState.value = TrainerMainUiState.Success("Calendar loaded")
                } else {
                    _uiState.value = TrainerMainUiState.Error(
                        response.errorBody()?.string() ?: "Failed to load calendar"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TrainerMainUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun navigateToMessage(ownerId: String) {
        // TODO: Navigate to chat screen with owner
    }
}

class TrainerMainViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainerMainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainerMainViewModel(TokenManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

