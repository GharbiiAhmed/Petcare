package tn.petcare_android.data.model.pet

import com.squareup.moshi.Json
import tn.petcare_android.data.model.medical.MedicationItem

/**
 * Medical history information embedded in pet response
 */
data class MedicalHistory(
    @Json(name = "_id") val id: String,
    val pet: String, // Pet ID reference
    val vaccinations: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<MedicationItem> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

