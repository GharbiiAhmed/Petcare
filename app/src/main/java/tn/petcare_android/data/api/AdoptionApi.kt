package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.adoption.*

interface AdoptionApi {

    // Listing Management
    @POST("adoption/listings")
    suspend fun createListing(
        @Body request: CreateAdoptionListingRequest
    ): Response<AdoptionListing>

    @GET("adoption/listings")
    suspend fun getAllListings(
        @Query("status") status: String?
    ): Response<List<AdoptionListing>>

    @GET("adoption/listings/search")
    suspend fun searchListings(
        @Query("q") query: String?,
        @Query("species") species: String?
    ): Response<List<AdoptionListing>>

    @GET("adoption/listings/{listingId}")
    suspend fun getListing(
        @Path("listingId") listingId: String
    ): Response<AdoptionListing>

    @GET("adoption/rescuer/{rescuerId}/listings")
    suspend fun getRescuerListings(
        @Path("rescuerId") rescuerId: String,
        @Query("status") status: String?
    ): Response<List<AdoptionListing>>

    @PUT("adoption/listings/{listingId}")
    suspend fun updateListing(
        @Path("listingId") listingId: String,
        @Body request: UpdateAdoptionListingRequest
    ): Response<AdoptionListing>

    @DELETE("adoption/listings/{listingId}")
    suspend fun deleteListing(
        @Path("listingId") listingId: String
    ): Response<Unit>

    @PUT("adoption/listings/{listingId}/adopted")
    suspend fun markListingAsAdopted(
        @Path("listingId") listingId: String,
        @Body request: MarkAsAdoptedRequest
    ): Response<AdoptionListing>

    @POST("adoption/listings/{listingId}/like")
    suspend fun toggleLike(
        @Path("listingId") listingId: String
    ): Response<AdoptionListing>

    // Application Management
    @POST("adoption/listings/{listingId}/applications")
    suspend fun createApplication(
        @Path("listingId") listingId: String,
        @Body request: CreateAdoptionApplicationRequest
    ): Response<AdoptionApplication>

    @GET("adoption/rescuer/applications")
    suspend fun getRescuerApplications(): Response<List<AdoptionApplication>>

    @GET("adoption/applicant/applications")
    suspend fun getApplicantApplications(): Response<List<AdoptionApplication>>

    @GET("adoption/applications/{applicationId}")
    suspend fun getApplication(
        @Path("applicationId") applicationId: String
    ): Response<Any>

    @PUT("adoption/applications/{applicationId}/approve")
    suspend fun approveApplication(
        @Path("applicationId") applicationId: String
    ): Response<Any>

    @PUT("adoption/applications/{applicationId}/reject")
    suspend fun rejectApplication(
        @Path("applicationId") applicationId: String,
        @Body request: RejectApplicationRequest
    ): Response<Any>

    @PUT("adoption/applications/{applicationId}/complete")
    suspend fun completeApplication(
        @Path("applicationId") applicationId: String
    ): Response<Any>
}

// Adoption Request Models
data class CreateAdoptionListingRequest(
    val petName: String,
    val species: String,
    val breed: String?,
    val age: Int?,
    val color: String?,
    val weight: Double?,
    val gender: String?,
    val description: String?,
    val images: List<String>?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val vaccinated: Boolean?,
    val neutered: Boolean?,
    val medicalHistory: String?,
    val traits: List<String>?,
    val adoptionRequirements: String?
)

data class UpdateAdoptionListingRequest(
    val petName: String?,
    val species: String?,
    val breed: String?,
    val age: Int?,
    val color: String?,
    val weight: Double?,
    val gender: String?,
    val description: String?,
    val images: List<String>?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val vaccinated: Boolean?,
    val neutered: Boolean?,
    val medicalHistory: String?,
    val traits: List<String>?,
    val adoptionRequirements: String?,
    val status: String?
)

data class CreateAdoptionApplicationRequest(
    val phoneNumber: String?,
    val address: String?,
    val housingType: String?,
    val ownRent: String?,
    val otherPets: String?,
    val familyDescription: String?,
    val workSchedule: String?,
    val motivation: String?,
    val experience: String?,
    val vetReference: String?,
    val additionalInfo: String?
)

data class MarkAsAdoptedRequest(
    val adopterId: String
)

data class RejectApplicationRequest(
    val rejectionReason: String
)
