package tn.petcare_android.ui.screens.role

// Data classes for Pet Sitter role
data class SittingRequest(
    val id: String,
    val petName: String,
    val ownerName: String,
    val ownerId: String,
    val startDate: String,
    val endDate: String,
    val hourlyRate: Double,
    val status: String // "pending", "accepted", "rejected"
)

data class SittingBooking(
    val id: String,
    val petName: String,
    val ownerName: String,
    val ownerId: String,
    val startDate: String,
    val endDate: String,
    val status: String = "active"
)

// Data classes for Trainer role
data class TrainingPet(
    val id: String,
    val petName: String,
    val ownerName: String,
    val ownerId: String,
    val status: String, // "active", "completed"
    val startDate: String,
    val trainingType: String = ""
)

data class TrainingEvent(
    val id: String,
    val petName: String,
    val ownerName: String,
    val ownerId: String,
    val dateTime: String,
    val trainingType: String = "",
    val isToday: Boolean = false,
    val isThisWeek: Boolean = false
)








