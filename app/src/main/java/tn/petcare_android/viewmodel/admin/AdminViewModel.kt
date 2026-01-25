package tn.petcare_android.viewmodel.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.auth.User
import tn.petcare_android.data.storage.TokenManager

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

class AdminViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState

    private val _pendingApprovals = MutableStateFlow<List<User>>(emptyList())
    val pendingApprovals: StateFlow<List<User>> = _pendingApprovals

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    fun loadPendingApprovals() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val response = RetrofitInstance.adminApi.getPendingRoleApprovals()
                if (response.isSuccessful) {
                    _pendingApprovals.value = response.body() ?: emptyList()
                    _uiState.value = AdminUiState.Idle
                } else {
                    _uiState.value = AdminUiState.Error(
                        response.errorBody()?.string() ?: "Failed to load pending approvals"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val response = RetrofitInstance.adminApi.getAllUsers()
                if (response.isSuccessful) {
                    _allUsers.value = response.body() ?: emptyList()
                    _uiState.value = AdminUiState.Idle
                } else {
                    _uiState.value = AdminUiState.Error(
                        response.errorBody()?.string() ?: "Failed to load users"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun approveRole(userId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val response = RetrofitInstance.adminApi.approveRole(
                    userId,
                    tn.petcare_android.data.api.ApproveRoleRequest(notes = null)
                )
                if (response.isSuccessful) {
                    _uiState.value = AdminUiState.Success("Role approved successfully")
                    loadPendingApprovals()
                    loadAllUsers()
                } else {
                    _uiState.value = AdminUiState.Error(
                        response.errorBody()?.string() ?: "Failed to approve role"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun rejectRole(userId: String, notes: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val response = RetrofitInstance.adminApi.rejectRole(
                    userId,
                    tn.petcare_android.data.api.RejectRoleRequest(notes = notes)
                )
                if (response.isSuccessful) {
                    _uiState.value = AdminUiState.Success("Role rejected")
                    loadPendingApprovals()
                    loadAllUsers()
                } else {
                    _uiState.value = AdminUiState.Error(
                        response.errorBody()?.string() ?: "Failed to reject role"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetState() {
        _uiState.value = AdminUiState.Idle
    }
}

class AdminViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(TokenManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


