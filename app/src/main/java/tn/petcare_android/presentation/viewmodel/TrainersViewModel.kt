package tn.petcare_android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.*
import tn.petcare_android.data.model.trainers.Trainer
import tn.petcare_android.data.model.trainers.TrainerBooking
import tn.petcare_android.data.repository.TrainersRepository

class TrainersViewModel : ViewModel() {

    private val trainersRepository = TrainersRepository(RetrofitInstance.trainersApi)

    private val _trainers = MutableLiveData<List<Any>>()
    val trainers: LiveData<List<Any>> = _trainers

    private val _selectedTrainer = MutableLiveData<Any>()
    val selectedTrainer: LiveData<Any> = _selectedTrainer

    private val _trainerBookings = MutableLiveData<List<Any>>()
    val trainerBookings: LiveData<List<Any>> = _trainerBookings

    private val _userBookings = MutableLiveData<List<Any>>()
    val userBookings: LiveData<List<Any>> = _userBookings

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getAllTrainers() {
        viewModelScope.launch {
            _loading.value = true
            trainersRepository.getAllTrainers()
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _trainers.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load trainers"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getTrainer(trainerId: String) {
        viewModelScope.launch {
            _loading.value = true
            trainersRepository.getTrainer(trainerId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _selectedTrainer.value = response.body()
                    } else {
                        _error.value = "Failed to load trainer"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getTrainerBookings(trainerId: String) {
        viewModelScope.launch {
            _loading.value = true
            trainersRepository.getTrainerBookings(trainerId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _trainerBookings.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load bookings"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun getUserBookings(ownerId: String) {
        viewModelScope.launch {
            _loading.value = true
            trainersRepository.getOwnerBookings(ownerId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _userBookings.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load your bookings"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun createBooking(trainerId: String, request: CreateTrainerBookingRequest) {
        viewModelScope.launch {
            _loading.value = true
            trainersRepository.createBooking(trainerId, request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Booking created successfully"
                    } else {
                        _error.value = "Failed to create booking"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _loading.value = false
        }
    }

    fun confirmBooking(bookingId: String) {
        viewModelScope.launch {
            trainersRepository.confirmBooking(bookingId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Booking confirmed"
                    } else {
                        _error.value = "Failed to confirm booking"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun rejectBooking(bookingId: String, reason: String) {
        viewModelScope.launch {
            val request = RejectBookingRequest(reason)
            trainersRepository.rejectBooking(bookingId, request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Booking rejected"
                    } else {
                        _error.value = "Failed to reject booking"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun cancelBooking(bookingId: String, reason: String) {
        viewModelScope.launch {
            val request = CancelBookingRequest(reason)
            trainersRepository.cancelBooking(bookingId, request)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Booking cancelled"
                    } else {
                        _error.value = "Failed to cancel booking"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    fun completeBooking(bookingId: String) {
        viewModelScope.launch {
            trainersRepository.completeBooking(bookingId)
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _error.value = "Booking completed"
                    } else {
                        _error.value = "Failed to complete booking"
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }
}
