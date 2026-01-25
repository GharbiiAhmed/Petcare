package tn.petcare_android.data.model.pet

import com.squareup.moshi.Json

data class Pet(
    @Json(name = "_id") val id: String? = null,
    val name: String,
    val species: String, // e.g., 'dog', 'cat'
    val breed: String? = null,
    val age: Double? = null,
    val gender: String? = null,
    val color: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val photo: String? = null,
    val microchipId: String? = null,
    val owner: PetOwner? = null,
    val medicalHistory: MedicalHistory? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)