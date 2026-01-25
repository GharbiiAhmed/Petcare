package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.*
import tn.petcare_android.data.model.auth.User
import tn.petcare_android.data.model.profile.UpdateProfileRequest

interface ProfileApi {

    @GET("users/{id}")
    suspend fun getProfile(@Path("id") userId: String): Response<User>

    @PUT("users/{id}")
    suspend fun updateProfile(
        @Path("id") userId: String,
        @Body request: UpdateProfileRequest
    ): Response<User>
}

