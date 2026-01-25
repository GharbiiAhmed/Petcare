package tn.petcare_android.data.model.profile

data class UpdateProfileRequest(
    val name: String,
    val phone: String? = null
)

