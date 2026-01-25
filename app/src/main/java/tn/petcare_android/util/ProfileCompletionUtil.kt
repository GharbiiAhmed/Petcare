package tn.petcare_android.util

import tn.petcare_android.data.model.auth.User

/**
 * Utility to check if user profile is complete
 * iOS Reference: SessionManager.swift requiresProfileCompletion
 */
object ProfileCompletionUtil {
    /**
     * Checks if profile needs completion
     * Profile is considered incomplete if missing:
     * - Photo (profileImage)
     * - Phone number
     * - Location (city or country)
     */
    fun requiresProfileCompletion(user: User?): Boolean {
        if (user == null) return true
        
        val hasPhoto = !user.profileImage.isNullOrBlank() || (user.hasPhoto == true)
        val hasPhone = !user.phone.isNullOrBlank() || !user.phoneNumber.isNullOrBlank()
        val hasLocation = !user.city.isNullOrBlank() || !user.country.isNullOrBlank()
        
        return !hasPhoto || !hasPhone || !hasLocation
    }
    
    /**
     * Gets the missing fields message
     */
    fun getMissingFieldsMessage(user: User?): String {
        if (user == null) return "Please complete your profile."
        
        val missing = mutableListOf<String>()
        val hasPhoto = !user.profileImage.isNullOrBlank() || (user.hasPhoto == true)
        val hasPhone = !user.phone.isNullOrBlank() || !user.phoneNumber.isNullOrBlank()
        val hasLocation = !user.city.isNullOrBlank() || !user.country.isNullOrBlank()
        
        if (!hasPhoto) missing.add("photo")
        if (!hasPhone) missing.add("phone number")
        if (!hasLocation) missing.add("location")
        
        return if (missing.isEmpty()) {
            "Profile is complete."
        } else {
            "Add your ${missing.joinToString(", ")} to unlock all features."
        }
    }
}

