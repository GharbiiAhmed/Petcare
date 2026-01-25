package tn.petcare_android.data.repository

import tn.petcare_android.data.api.*
import tn.petcare_android.data.model.adoption.AdoptionListing
import tn.petcare_android.data.model.adoption.AdoptionApplication
import javax.inject.Inject

class AdoptionRepository @Inject constructor(
    private val adoptionApi: AdoptionApi
) {

    suspend fun getAllListings(status: String? = null) = runCatching {
        adoptionApi.getAllListings(status)
    }

    suspend fun searchListings(
        query: String? = null,
        species: String? = null
    ) = runCatching {
        adoptionApi.searchListings(query, species)
    }

    suspend fun getListing(listingId: String) = runCatching {
        adoptionApi.getListing(listingId)
    }

    suspend fun getRescuerListings(rescuerId: String, status: String? = null) = runCatching {
        adoptionApi.getRescuerListings(rescuerId, status)
    }

    suspend fun createListing(request: CreateAdoptionListingRequest) = runCatching {
        adoptionApi.createListing(request)
    }

    suspend fun updateListing(listingId: String, request: UpdateAdoptionListingRequest) = runCatching {
        adoptionApi.updateListing(listingId, request)
    }

    suspend fun deleteListing(listingId: String) = runCatching {
        adoptionApi.deleteListing(listingId)
    }

    suspend fun toggleLike(listingId: String) = runCatching {
        adoptionApi.toggleLike(listingId)
    }

    suspend fun createApplication(listingId: String, request: CreateAdoptionApplicationRequest) = runCatching {
        adoptionApi.createApplication(listingId, request)
    }

    suspend fun getRescuerApplications() = runCatching {
        adoptionApi.getRescuerApplications()
    }

    suspend fun getApplicantApplications() = runCatching {
        adoptionApi.getApplicantApplications()
    }

    suspend fun getApplication(applicationId: String) = runCatching {
        adoptionApi.getApplication(applicationId)
    }

    suspend fun approveApplication(applicationId: String) = runCatching {
        adoptionApi.approveApplication(applicationId)
    }

    suspend fun rejectApplication(applicationId: String, request: RejectApplicationRequest) = runCatching {
        adoptionApi.rejectApplication(applicationId, request)
    }

    suspend fun completeApplication(applicationId: String) = runCatching {
        adoptionApi.completeApplication(applicationId)
    }
}
