package tn.petcare_android.data.repository

import retrofit2.Response
import tn.petcare_android.data.api.ProfileApi
import tn.petcare_android.data.model.auth.User
import tn.petcare_android.data.model.profile.UpdateProfileRequest

class ProfileRepository(private val api: ProfileApi) {

    suspend fun getProfile(userId: String): Response<User> {
        return api.getProfile(userId)
    }

    suspend fun updateProfile(userId: String, request: UpdateProfileRequest): Response<User> {
        return api.updateProfile(userId, request)
    }
}

