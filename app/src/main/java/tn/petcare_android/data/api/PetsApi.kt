package tn.petcare_android.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.pet.Pet

interface PetsApi {

    @GET("pets/owner/{ownerId}")
    suspend fun getPetsByOwner(@Path("ownerId") ownerId: String): Response<List<Pet>>

    @GET("pets/{petId}")
    suspend fun getPetById(@Path("petId") petId: String): Response<Pet>

    // Add pet with optional photo upload
    @Multipart
    @POST("pets/owner/{ownerId}")
    suspend fun addPet(
        @Path("ownerId") ownerId: String,
        @Part("name") name: RequestBody,
        @Part("species") species: RequestBody,
        @Part("breed") breed: RequestBody? = null,
        @Part("age") age: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null,
        @Part("color") color: RequestBody? = null,
        @Part("weight") weight: RequestBody? = null,
        @Part("height") height: RequestBody? = null,
        @Part("microchipId") microchipId: RequestBody? = null,
        @Part photo: MultipartBody.Part? = null
    ): Response<Pet>

    // Update pet with optional photo upload
    @Multipart
    @PUT("pets/{petId}")
    suspend fun updatePet(
        @Path("petId") petId: String,
        @Part("name") name: RequestBody? = null,
        @Part("species") species: RequestBody? = null,
        @Part("breed") breed: RequestBody? = null,
        @Part("age") age: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null,
        @Part("color") color: RequestBody? = null,
        @Part("weight") weight: RequestBody? = null,
        @Part("height") height: RequestBody? = null,
        @Part("microchipId") microchipId: RequestBody? = null,
        @Part photo: MultipartBody.Part? = null
    ): Response<Pet>

    @DELETE("pets/{ownerId}/{petId}")
    suspend fun deletePet(
        @Path("ownerId") ownerId: String,
        @Path("petId") petId: String
    ): Response<Unit>
}

