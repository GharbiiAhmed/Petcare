package tn.petcare_android.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import tn.petcare_android.data.api.UserApi
import java.io.File

class UserRepository(private val api: UserApi) {

    suspend fun getUserById(userId: String) = api.getUserById(userId)

    suspend fun getProfile() = api.getProfile()

    suspend fun updateProfile(
        name: String? = null,
        phoneNumber: String? = null,
        country: String? = null,
        city: String? = null,
        hasPhoto: Boolean? = null,
        hasPets: Boolean? = null,
        photoFile: File? = null
    ) = api.updateProfile(
        name = name?.toRequestBody("text/plain".toMediaTypeOrNull()),
        phoneNumber = phoneNumber?.toRequestBody("text/plain".toMediaTypeOrNull()),
        country = country?.toRequestBody("text/plain".toMediaTypeOrNull()),
        city = city?.toRequestBody("text/plain".toMediaTypeOrNull()),
        hasPhoto = hasPhoto?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        hasPets = hasPets?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        image = photoFile?.let {
            MultipartBody.Part.createFormData(
                "image",
                photoFile.name,
                photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }
    )

    suspend fun deleteAccount() = api.deleteAccount()
}
