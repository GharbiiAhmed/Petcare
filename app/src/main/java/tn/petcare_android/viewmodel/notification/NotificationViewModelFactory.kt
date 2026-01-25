package tn.petcare_android.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.data.api.NotificationApi
import tn.petcare_android.data.repository.NotificationRepository

/**
 * Factory for creating NotificationViewModel instances
 */
class NotificationViewModelFactory(
    private val api: NotificationApi
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            val repository = NotificationRepository(api)
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

