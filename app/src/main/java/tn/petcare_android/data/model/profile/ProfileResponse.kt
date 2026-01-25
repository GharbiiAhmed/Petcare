package tn.petcare_android.data.model.profile

import tn.petcare_android.data.model.auth.User
import tn.petcare_android.data.model.pet.Pet

data class ProfileResponse(
    val user: User,
    val pets: List<Pet>
)

