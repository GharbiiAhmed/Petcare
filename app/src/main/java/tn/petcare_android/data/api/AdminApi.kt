package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.auth.User

interface AdminApi {
    @GET("admin/users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("admin/users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<User>

    @GET("admin/role-approvals/pending")
    suspend fun getPendingRoleApprovals(): Response<List<User>>

    @POST("admin/role-approvals/{userId}/approve")
    suspend fun approveRole(
        @Path("userId") userId: String,
        @Body body: ApproveRoleRequest
    ): Response<User>

    @POST("admin/role-approvals/{userId}/reject")
    suspend fun rejectRole(
        @Path("userId") userId: String,
        @Body body: RejectRoleRequest
    ): Response<User>

    @DELETE("admin/users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): Response<Unit>
}

data class ApproveRoleRequest(
    val notes: String? = null
)

data class RejectRoleRequest(
    val notes: String
)








