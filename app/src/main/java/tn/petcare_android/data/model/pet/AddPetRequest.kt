package tn.petcare_android.data.model.pet

data class AddPetRequest(
    val name: String,
    val species: String, // 'dog', 'cat', etc.
    val breed: String? = null,
    val age: Double? = null,
    val gender: String? = null,
    val color: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val photo: String? = null,
    val microchipId: String? = null
)