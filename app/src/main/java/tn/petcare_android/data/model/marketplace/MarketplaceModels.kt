package tn.petcare_android.data.model.marketplace

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketplaceListing(
    @Json(name = "_id") val id: String?,
    val seller: MarketplaceUser?,
    val buyer: MarketplaceUser?,
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
    val status: String?,
    val soldDate: String?,
    val views: Int?,
    val likes: Int?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class MarketplaceUser(
    @Json(name = "_id") val id: String?,
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val profileImage: String?,
    val location: String?
)

@JsonClass(generateAdapter = true)
data class MarketplaceInquiry(
    @Json(name = "_id") val id: String?,
    val buyer: MarketplaceUser?,
    val listing: MarketplaceListingInfo?,
    val message: String?,
    val phoneNumber: String?,
    val email: String?,
    val status: String?,
    val rejectionReason: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class MarketplaceListingInfo(
    @Json(name = "_id") val id: String?,
    val petName: String?,
    val species: String?,
    val price: Double?,
    val images: List<String>?,
    val seller: MarketplaceUser?
)
