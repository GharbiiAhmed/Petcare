package tn.petcare_android.viewmodel.medical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.medical.*

/**
 * ViewModel for Medical History Management
 * iOS Reference: Pet management with medical history
 */
class MedicalHistoryViewModel : ViewModel() {

    private val medicalApi = RetrofitInstance.medicalHistoryApi

    private val _medicalHistory = MutableStateFlow<MedicalHistoryResponse?>(null)
    val medicalHistory: StateFlow<MedicalHistoryResponse?> = _medicalHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedRecordType = MutableStateFlow<MedicalRecordType?>(null)
    val selectedRecordType: StateFlow<MedicalRecordType?> = _selectedRecordType

    /**
     * Load medical history for a pet
     */
    fun loadMedicalHistory(petId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.getMedicalHistory(petId)
                _medicalHistory.value = result
            } catch (e: Exception) {
                _error.value = "Failed to load medical history: ${e.message}"
                android.util.Log.e("MedicalHistoryVM", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add vaccination to pet's record
     */
    fun addVaccination(petId: String, vaccinationName: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.addVaccination(
                    petId,
                    mapOf("name" to vaccinationName)
                )
                _medicalHistory.value = result
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add vaccination: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add chronic condition to pet's record
     */
    fun addCondition(petId: String, conditionName: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.addCondition(
                    petId,
                    mapOf("name" to conditionName)
                )
                _medicalHistory.value = result
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add condition: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add medication to pet's record
     */
    fun addMedication(
        petId: String,
        name: String,
        dosage: String,
        frequency: String = "Daily",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.addMedication(
                    petId,
                    mapOf(
                        "name" to name,
                        "dosage" to dosage,
                        "frequency" to frequency
                    )
                )
                _medicalHistory.value = result
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add medication: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove vaccination from pet's record
     */
    fun removeVaccination(petId: String, vaccination: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.removeVaccination(petId, vaccination)
                _medicalHistory.value = result
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to remove vaccination: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove chronic condition from pet's record
     */
    fun removeCondition(petId: String, condition: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.removeCondition(petId, condition)
                _medicalHistory.value = result
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to remove condition: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove medication from pet's record
     */
    fun removeMedication(petId: String, medication: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.removeMedication(petId, medication)
                _medicalHistory.value = result
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to remove medication: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update complete medical history
     */
    fun updateMedicalHistory(
        petId: String,
        request: MedicalHistoryRequest,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = medicalApi.updateMedicalHistory(petId, request)
                _medicalHistory.value = result
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to update medical history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filter records by type
     */
    fun setFilterType(type: MedicalRecordType?) {
        _selectedRecordType.value = type
    }

    /**
     * Get filtered medical records for UI display
     */
    fun getFilteredRecords(): List<MedicalRecordGroup> {
        val history = _medicalHistory.value ?: return emptyList()
        val groups = mutableListOf<MedicalRecordGroup>()

        // Vaccinations
        if (selectedRecordType.value == null || selectedRecordType.value == MedicalRecordType.VACCINATION) {
            val vaccinations = history.vaccinations?.mapIndexed { index, vac ->
                MedicalRecordItem(
                    id = "vac_$index",
                    type = MedicalRecordType.VACCINATION,
                    title = vac,
                    subtitle = "Vaccination",
                    date = history.updatedAt
                )
            } ?: emptyList()

            if (vaccinations.isNotEmpty()) {
                groups.add(MedicalRecordGroup(MedicalRecordType.VACCINATION, vaccinations))
            }
        }

        // Chronic Conditions
        if (selectedRecordType.value == null || selectedRecordType.value == MedicalRecordType.CONDITION) {
            val conditions = history.chronicConditions?.mapIndexed { index, condition ->
                MedicalRecordItem(
                    id = "cond_$index",
                    type = MedicalRecordType.CONDITION,
                    title = condition,
                    subtitle = "Chronic Condition",
                    date = history.updatedAt
                )
            } ?: emptyList()

            if (conditions.isNotEmpty()) {
                groups.add(MedicalRecordGroup(MedicalRecordType.CONDITION, conditions))
            }
        }

        // Medications
        if (selectedRecordType.value == null || selectedRecordType.value == MedicalRecordType.MEDICATION) {
            val medications = history.currentMedications?.mapIndexed { index, med ->
                MedicalRecordItem(
                    id = "med_$index",
                    type = MedicalRecordType.MEDICATION,
                    title = med.name,
                    subtitle = med.dosage,
                    details = med.frequency,
                    date = med.startDate
                )
            } ?: emptyList()

            if (medications.isNotEmpty()) {
                groups.add(MedicalRecordGroup(MedicalRecordType.MEDICATION, medications))
            }
        }

        return groups
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

