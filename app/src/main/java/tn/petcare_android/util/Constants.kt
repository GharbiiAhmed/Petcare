package tn.petcare_android.util

import androidx.compose.ui.unit.dp

object Constants {

    object Api {
        const val BASE_URL = "http://10.0.2.2:3000/"
        const val TIMEOUT_SECONDS = 30L
    }

    object Stripe {
        const val PUBLISHABLE_KEY = "pk_test_51SWoRFK6f2Usc2LwQSOruB63GGE3lp85D6sZK9eMPpxgF8Y6ZuCb9ZLyGK9Jrzn026ncZnqxN1W5eXqwaC6qtFHQ00sxHei5uB"
    }

    object Splash {
        const val DELAY_MS = 2500L
        const val LOGO_SIZE_DP = 120
        const val APP_NAME = "Petcare"
        const val TAGLINE = "Pet Healthcare Platform"
    }

    object UI {
        object Card {
            val CORNER_RADIUS = 16.dp
            val ELEVATION = 2.dp
            val PADDING = 20.dp
        }

        object Button {
            val HEIGHT = 56.dp
            val CORNER_RADIUS = 12.dp
        }

        object TopBar {
            const val TITLE_FONT_SIZE = 28
            const val SUBTITLE_FONT_SIZE = 14
        }

        object Avatar {
            val SIZE = 120.dp
            val SMALL_SIZE = 60.dp
        }

        object Spacing {
            val SMALL = 8.dp
            val MEDIUM = 16.dp
            val LARGE = 24.dp
            val EXTRA_LARGE = 32.dp
        }
    }

    object Validation {
        const val MIN_PASSWORD_LENGTH = 6
        const val MIN_NAME_LENGTH = 2
        const val VERIFICATION_CODE_LENGTH = 6
    }

    object Pet {
        object Species {
            const val DOG = "dog"
            const val CAT = "cat"
            const val BIRD = "bird"
            const val FISH = "fish"
            const val RABBIT = "rabbit"
            const val HAMSTER = "hamster"
        }

        val SPECIES_LIST = listOf(
            "Dog", "Cat", "Bird", "Fish", "Rabbit", "Hamster", "Other"
        )

        val GENDER_LIST = listOf("Male", "Female")
    }

    object Navigation {
        const val LOGIN = "login"
        const val REGISTER = "register"
        const val VERIFY = "verify"
        const val MAIN = "main"
        const val HOME = "home"
        const val PROFILE = "profile"
        const val ADD_PET = "add_pet"
        const val PET_DETAIL = "pet_detail"
    }

    object Preferences {
        const val THEME_PREFS = "theme_preferences"
        const val TOKEN_PREFS = "token_preferences"
        const val USER_PREFS = "user_preferences"
    }
}

