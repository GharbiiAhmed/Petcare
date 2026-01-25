package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.auth.AppUser
import tn.petcare_android.data.model.salon.*

interface SalonApi {
    @POST("salons")
    suspend fun registerSalon(
        @Body request: CreateSalonRequest
    ): Response<AppUser>

    @POST("salons/convert/{userId}")
    suspend fun convertUserToSalon(
        @Path("userId") userId: String,
        @Body request: ConvertSalonRequest
    ): Response<AppUser>

    @PUT("salons/{salonId}")
    suspend fun updateSalon(
        @Path("salonId") salonId: String,
        @Body request: UpdateSalonRequest
    ): Response<AppUser>

    @GET("salons")
    suspend fun getAllSalons(): List<AppUser>

    @GET("salons/{salonId}")
    suspend fun getSalon(@Path("salonId") salonId: String): Response<AppUser>
}


