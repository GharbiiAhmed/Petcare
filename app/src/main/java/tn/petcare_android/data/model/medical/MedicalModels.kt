package tn.petcare_android.data.model.medical

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Medical History Models
 * iOS Reference: Pet.swift MedicalHistory & Medication
 */

@JsonClass(generateAdapter = true)
data class MedicalHistoryResponse(
    @Json(name = "_id") val id: String?,
    val pet: String?, // Pet ID
    val vaccinations: List<String>? = emptyList(),
    val chronicConditions: List<String>? = emptyList(),
    val currentMedications: List<MedicationItem>? = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class MedicalHistoryRequest(
    val vaccinations: List<String>? = null,
    val chronicConditions: List<String>? = null,
    val currentMedications: List<MedicationItem>? = null
)

@JsonClass(generateAdapter = true)
data class MedicationItem(
    val name: String,
    val dosage: String,
    val frequency: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

/**
 * UI Models for Medical History Display
 */
data class MedicalRecordGroup(
    val type: MedicalRecordType,
    val items: List<MedicalRecordItem>
)

data class MedicalRecordItem(
    val id: String,
    val type: MedicalRecordType,
    val title: String,
    val subtitle: String? = null,
    val date: String? = null,
    val details: String? = null
)

enum class MedicalRecordType(val displayName: String, val icon: String) {
    VACCINATION("Vaccinations", "💉"),
    MEDICATION("Medications", "💊"),
    CONDITION("Chronic Conditions", "🩺"),
    ALLERGY("Allergies", "⚠️"),
    SURGERY("Surgeries", "🏥"),
    VISIT("Vet Visits", "👨‍⚕️")
}

/**
 * Add Medical Record Requests
 */
@JsonClass(generateAdapter = true)
data class AddVaccinationRequest(
    val name: String,
    val date: String,
    val nextDueDate: String? = null
)

@JsonClass(generateAdapter = true)
data class AddConditionRequest(
    val name: String,
    val diagnosedDate: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class AddMedicationRequest(
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: String,
    val endDate: String? = null
)

