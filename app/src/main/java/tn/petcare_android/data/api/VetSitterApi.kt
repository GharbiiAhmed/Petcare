package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.auth.AppUser
import tn.petcare_android.data.model.vetsitter.*

interface VetSitterApi {



    @POST("veterinarians/register")
    suspend fun registerVet(
        @Body request: CreateVetRequest
    ): Response<AppUser>

    @POST("veterinarians/convert/{userId}")
    suspend fun convertUserToVet(
        @Path("userId") userId: String,
        @Body request: ConvertVetRequest
    ): Response<AppUser>

    @PUT("veterinarians/{vetId}")
    suspend fun updateVet(
        @Path("vetId") vetId: String,
        @Body request: UpdateVetRequest
    ): Response<AppUser>

    @GET("veterinarians")
    suspend fun getAllVets(): List<AppUser>

    @GET("veterinarians/{vetId}")
    suspend fun getVet(@Path("vetId") vetId: String): Response<AppUser>



    @POST("pet-sitters/register")
    suspend fun registerSitter(
        @Body request: CreateSitterRequest
    ): Response<AppUser>

    @POST("pet-sitters/convert/{userId}")
    suspend fun convertUserToSitter(
        @Path("userId") userId: String,
        @Body request: ConvertSitterRequest
    ): Response<AppUser>

    @PUT("pet-sitters/{sitterId}")
    suspend fun updateSitter(
        @Path("sitterId") sitterId: String,
        @Body request: UpdateSitterRequest
    ): Response<AppUser>

    @GET("pet-sitters")
    suspend fun getAllSitters(): List<AppUser>

    @GET("pet-sitters/{sitterId}")
    suspend fun getSitter(@Path("sitterId") sitterId: String): Response<AppUser>
}
