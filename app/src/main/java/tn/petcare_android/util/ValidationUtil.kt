package tn.petcare_android.util

object ValidationUtil {

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }

    fun isValidVerificationCode(code: String): Boolean {
        return code.matches(Regex("^\\d{6}$"))
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateRegistrationInput(
        email: String,
        password: String,
        name: String,
        role: String
    ): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Name is required")
            !isValidName(name) -> ValidationResult(false, "Name must be at least 2 characters")
            email.isBlank() -> ValidationResult(false, "Email is required")
            !isValidEmail(email) -> ValidationResult(false, "Invalid email format")
            password.isBlank() -> ValidationResult(false, "Password is required")
            !isValidPassword(password) -> ValidationResult(false, "Password must be at least 8 characters")
            role.isBlank() -> ValidationResult(false, "Please select a role")
            else -> ValidationResult(true)
        }
    }

    fun validateLoginInput(email: String, password: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !isValidEmail(email) -> ValidationResult(false, "Invalid email format")
            password.isBlank() -> ValidationResult(false, "Password is required")
            else -> ValidationResult(true)
        }
    }

    fun validateVerificationInput(code: String): ValidationResult {
        return when {
            code.isBlank() -> ValidationResult(false, "Verification code is required")
            !isValidVerificationCode(code) -> ValidationResult(false, "Verification code must be 6 digits")
            else -> ValidationResult(true)
        }
    }
}

