package tn.petcare_android.data.model.pet

import com.squareup.moshi.Json

/**
 * Owner information embedded in pet response
 */
data class PetOwner(
    @Json(name = "_id") val id: String,
    val email: String,
    val name: String
)

