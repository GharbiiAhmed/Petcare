package tn.petcare_android.viewmodel.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.repository.SubscriptionRepository

class SubscriptionViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            val repository = SubscriptionRepository(RetrofitInstance.subscriptionApi)
            return SubscriptionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

