package tn.petcare_android.data.repository

import tn.petcare_android.data.api.*
import tn.petcare_android.data.model.marketplace.MarketplaceListing
import tn.petcare_android.data.model.marketplace.MarketplaceInquiry
import javax.inject.Inject

class MarketplaceRepository @Inject constructor(
    private val marketplaceApi: MarketplaceApi
) {

    suspend fun getAllListings(status: String? = null) = runCatching {
        marketplaceApi.getAllListings(status)
    }

    suspend fun searchListings(
        query: String? = null,
        species: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null
    ) = runCatching {
        marketplaceApi.searchListings(query, species, minPrice, maxPrice)
    }

    suspend fun getListing(listingId: String) = runCatching {
        marketplaceApi.getListing(listingId)
    }

    suspend fun getUserListings(userId: String, status: String? = null) = runCatching {
        marketplaceApi.getUserListings(userId, status)
    }

    suspend fun createListing(request: CreateMarketplaceListingRequest) = runCatching {
        marketplaceApi.createListing(request)
    }

    suspend fun updateListing(listingId: String, request: UpdateMarketplaceListingRequest) = runCatching {
        marketplaceApi.updateListing(listingId, request)
    }

    suspend fun deleteListing(listingId: String) = runCatching {
        marketplaceApi.deleteListing(listingId)
    }

    suspend fun toggleLike(listingId: String) = runCatching {
        marketplaceApi.toggleLike(listingId)
    }

    suspend fun createInquiry(listingId: String, request: CreateMarketplaceInquiryRequest) = runCatching {
        marketplaceApi.createInquiry(listingId, request)
    }

    suspend fun getSellerInquiries() = runCatching {
        marketplaceApi.getSellerInquiries()
    }

    suspend fun getBuyerInquiries() = runCatching {
        marketplaceApi.getBuyerInquiries()
    }

    suspend fun getInquiry(inquiryId: String) = runCatching {
        marketplaceApi.getInquiry(inquiryId)
    }

    suspend fun acceptInquiry(inquiryId: String) = runCatching {
        marketplaceApi.acceptInquiry(inquiryId)
    }

    suspend fun rejectInquiry(inquiryId: String, request: RejectInquiryRequest) = runCatching {
        marketplaceApi.rejectInquiry(inquiryId, request)
    }

    suspend fun completeInquiry(inquiryId: String) = runCatching {
        marketplaceApi.completeInquiry(inquiryId)
    }
}
