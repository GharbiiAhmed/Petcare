package tn.petcare_android.viewmodel.pet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.repository.PetsRepository
import tn.petcare_android.data.storage.TokenManager

class PetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
            val tokenManager = TokenManager(context)
            val repository = PetsRepository(RetrofitInstance.petsApi)
            @Suppress("UNCHECKED_CAST")
            return PetViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

