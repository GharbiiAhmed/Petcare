package tn.petcare_android.viewmodel.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.repository.AuthRepository
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.storage.UserManager

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val repository = AuthRepository(RetrofitInstance.api)
    private val tokenManager = TokenManager(context.applicationContext)
    private val userManager = UserManager(context.applicationContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository, tokenManager, userManager, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
