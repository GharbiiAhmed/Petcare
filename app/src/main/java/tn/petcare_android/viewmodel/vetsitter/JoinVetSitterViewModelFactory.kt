package tn.petcare_android.viewmodel.vetsitter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.data.storage.TokenManager

class JoinVetSitterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val tokenManager = TokenManager(context.applicationContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JoinVetSitterViewModel::class.java)) {
            return JoinVetSitterViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
