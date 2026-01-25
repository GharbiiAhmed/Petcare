package tn.petcare_android.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.auth.User as AuthUser

interface UserApi {

    @GET("users")
    suspend fun getAllUsers(): List<tn.petcare_android.data.model.auth.AppUser>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<AuthUser>

    @GET("users/profile")
    suspend fun getProfile(): Response<AuthUser>

    @Multipart
    @PATCH("users/profile")
    suspend fun updateProfile(
        @Part("name") name: RequestBody? = null,
        @Part("phoneNumber") phoneNumber: RequestBody? = null,
        @Part("country") country: RequestBody? = null,
        @Part("city") city: RequestBody? = null,
        @Part("hasPhoto") hasPhoto: RequestBody? = null,
        @Part("hasPets") hasPets: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<AuthUser>

    @DELETE("users/profile")
    suspend fun deleteAccount(): Response<Unit>
}
