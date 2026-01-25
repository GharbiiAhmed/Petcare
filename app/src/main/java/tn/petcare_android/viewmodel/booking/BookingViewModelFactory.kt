package tn.petcare_android.viewmodel.booking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.repository.BookingRepository

class BookingViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {

            RetrofitInstance.initialize(context.applicationContext)
            val repo = BookingRepository(RetrofitInstance.bookingApi)
            @Suppress("UNCHECKED_CAST")
            return BookingViewModel(repo, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
