package tn.petcare_android.viewmodel.booking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.booking.Booking
import tn.petcare_android.data.model.booking.CreateBookingRequest
import tn.petcare_android.data.model.booking.UpdateBookingRequest
import tn.petcare_android.data.repository.BookingRepository
import tn.petcare_android.util.CalendarManager

class BookingViewModel(
    private val repository: BookingRepository,
    private val context: Context? = null
) : ViewModel() {

    private val calendarManager = context?.let { CalendarManager(it) }
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchBookings(role: String? = null, status: String? = null) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getBookings(role, status)
                _bookings.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchBookingById(bookingId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getBookingById(bookingId)
                _selectedBooking.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createBooking(request: CreateBookingRequest) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.createBooking(request)
                _selectedBooking.value = result
                _error.value = null


                if (result.displayStatus == "pending") {
                    syncToCalendar(result)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateBooking(bookingId: String, request: UpdateBookingRequest) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.updateBooking(bookingId, request)
                _selectedBooking.value = result
                _error.value = null


                when (result.displayStatus) {
                    "accepted" -> syncToCalendar(result)
                    "cancelled", "rejected" -> removeFromCalendar(result.normalizedId)
                    "completed" -> removeFromCalendar(result.normalizedId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteBooking(bookingId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                repository.deleteBooking(bookingId)
                removeFromCalendar(bookingId)
                _selectedBooking.value = null
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun syncToCalendar(booking: Booking) {
        viewModelScope.launch {
            try {
                calendarManager?.addBookingToCalendar(booking)
            } catch (e: Exception) {

                android.util.Log.e("BookingViewModel", "Calendar sync failed: ${e.message}")
            }
        }
    }

    private fun removeFromCalendar(bookingId: String) {
        viewModelScope.launch {
            try {
                calendarManager?.removeBookingFromCalendar(bookingId)
            } catch (e: Exception) {

                android.util.Log.e("BookingViewModel", "Calendar removal failed: ${e.message}")
            }
        }
    }
}
