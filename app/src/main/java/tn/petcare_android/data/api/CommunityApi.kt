package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.community.*

interface CommunityApi {
    
    @GET("community/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostsResponse>
    
    @GET("community/my-posts")
    suspend fun getMyPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostsResponse>
    
    @Multipart
    @POST("community/posts")
    suspend fun createPost(
        @Part("caption") caption: String?,
        @Part petImage: okhttp3.MultipartBody.Part
    ): Response<CreatePostResponse>
    
    @POST("community/posts/{postId}/react")
    suspend fun reactToPost(
        @Path("postId") postId: String,
        @Body request: ReactRequest
    ): Response<ReactResponse>
    
    @DELETE("community/posts/{postId}/react")
    suspend fun removeReaction(
        @Path("postId") postId: String,
        @Query("reactionType") reactionType: String
    ): Response<ReactResponse>
    
    @DELETE("community/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String
    ): Response<Unit>
    
    @POST("community/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body request: AddCommentRequest
    ): Response<AddCommentResponse>
    
    @DELETE("community/posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String
    ): Response<AddCommentResponse>
    @POST("community/posts/{postId}/report")
    suspend fun reportPost(
        @Path("postId") postId: String
    ): Response<ReportPostResponse>}
