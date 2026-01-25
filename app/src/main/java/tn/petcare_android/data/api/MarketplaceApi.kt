package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.marketplace.*

interface MarketplaceApi {

    // Listing Management
    @POST("marketplace/listings")
    suspend fun createListing(
        @Body request: CreateMarketplaceListingRequest
    ): Response<MarketplaceListing>

    @GET("marketplace/listings")
    suspend fun getAllListings(
        @Query("status") status: String?
    ): Response<List<MarketplaceListing>>

    @GET("marketplace/listings/search")
    suspend fun searchListings(
        @Query("q") query: String?,
        @Query("species") species: String?,
        @Query("minPrice") minPrice: Int?,
        @Query("maxPrice") maxPrice: Int?
    ): Response<List<MarketplaceListing>>

    @GET("marketplace/listings/{listingId}")
    suspend fun getListing(
        @Path("listingId") listingId: String
    ): Response<MarketplaceListing>

    @GET("marketplace/user/{userId}/listings")
    suspend fun getUserListings(
        @Path("userId") userId: String,
        @Query("status") status: String?
    ): Response<List<MarketplaceListing>>

    @PUT("marketplace/listings/{listingId}")
    suspend fun updateListing(
        @Path("listingId") listingId: String,
        @Body request: UpdateMarketplaceListingRequest
    ): Response<MarketplaceListing>

    @DELETE("marketplace/listings/{listingId}")
    suspend fun deleteListing(
        @Path("listingId") listingId: String
    ): Response<Unit>

    @PUT("marketplace/listings/{listingId}/sold")
    suspend fun markListingAsSold(
        @Path("listingId") listingId: String,
        @Body request: MarkAsSoldRequest
    ): Response<MarketplaceListing>

    @POST("marketplace/listings/{listingId}/like")
    suspend fun toggleLike(
        @Path("listingId") listingId: String
    ): Response<MarketplaceListing>

    // Inquiry Management
    @POST("marketplace/listings/{listingId}/inquiries")
    suspend fun createInquiry(
        @Path("listingId") listingId: String,
        @Body request: CreateMarketplaceInquiryRequest
    ): Response<MarketplaceInquiry>

    @GET("marketplace/seller/inquiries")
    suspend fun getSellerInquiries(): Response<List<MarketplaceInquiry>>

    @GET("marketplace/buyer/inquiries")
    suspend fun getBuyerInquiries(): Response<List<MarketplaceInquiry>>

    @GET("marketplace/inquiries/{inquiryId}")
    suspend fun getInquiry(
        @Path("inquiryId") inquiryId: String
    ): Response<Any>

    @PUT("marketplace/inquiries/{inquiryId}/accept")
    suspend fun acceptInquiry(
        @Path("inquiryId") inquiryId: String
    ): Response<Any>

    @PUT("marketplace/inquiries/{inquiryId}/reject")
    suspend fun rejectInquiry(
        @Path("inquiryId") inquiryId: String,
        @Body request: RejectInquiryRequest
    ): Response<Any>

    @PUT("marketplace/inquiries/{inquiryId}/complete")
    suspend fun completeInquiry(
        @Path("inquiryId") inquiryId: String
    ): Response<Any>
}

// Marketplace Request Models
data class CreateMarketplaceListingRequest(
    val petName: String,
    val species: String,
    val breed: String?,
    val age: Int?,
    val color: String?,
    val weight: Double?,
    val gender: String?,
    val description: String?,
    val price: Double,
    val images: List<String>?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val vaccinated: Boolean?,
    val neutered: Boolean?,
    val medicalHistory: String?,
    val traits: List<String>?
)

data class UpdateMarketplaceListingRequest(
    val petName: String?,
    val species: String?,
    val breed: String?,
    val age: Int?,
    val color: String?,
    val weight: Double?,
    val gender: String?,
    val description: String?,
    val price: Double?,
    val images: List<String>?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val vaccinated: Boolean?,
    val neutered: Boolean?,
    val medicalHistory: String?,
    val traits: List<String>?,
    val status: String?
)

data class CreateMarketplaceInquiryRequest(
    val message: String?,
    val phoneNumber: String?,
    val email: String?
)

data class MarkAsSoldRequest(
    val buyerId: String
)

data class RejectInquiryRequest(
    val rejectionReason: String
)
