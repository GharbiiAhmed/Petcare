package tn.petcare_android.data.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*
import tn.petcare_android.data.model.chat.*

/**
 * Chat API endpoints
 * iOS Reference: ChatService.swift
 */
interface ChatApi {
    
    // iOS uses /messages/conversations not just /conversations
    @GET("messages/conversations")
    suspend fun getConversations(): List<Conversation>
    
    // Get or create conversation - iOS: POST /messages/conversations
    @POST("messages/conversations")
    suspend fun getOrCreateConversation(
        @Body request: CreateConversationRequest
    ): Conversation
    
    @GET("messages/conversations/{conversationId}")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String
    ): List<Message>  // Backend returns array directly
    
    // iOS uses POST /messages (not /conversations/{id}/messages)
    @POST("messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Message
    
    @PUT("messages/{messageId}")
    suspend fun editMessage(
        @Path("messageId") messageId: String,
        @Body request: EditMessageRequest
    ): Message
    
    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: String
    )
    
    @DELETE("messages/conversations/{conversationId}")
    suspend fun deleteConversation(
        @Path("conversationId") conversationId: String
    )
    
    @POST("messages/conversations/{conversationId}/read")
    suspend fun markConversationAsRead(
        @Path("conversationId") conversationId: String
    )
    
    /**
     * Send message with audio file
     * Backend: POST /messages with multipart/form-data
     * Field name for audio: "audio"
     * Also accepts CreateMessageDto fields: recipientId, content, conversationId
     */
    @Multipart
    @POST("messages")
    suspend fun sendAudioMessage(
        @Part recipientId: MultipartBody.Part,
        @Part content: MultipartBody.Part,
        @Part conversationId: MultipartBody.Part?,
        @Part audio: MultipartBody.Part
    ): Message
    
    /**
     * Download audio file
     * @param audioUrl Audio URL path
     * @return Audio file bytes
     */
    @GET
    @Streaming
    suspend fun downloadAudio(
        @Url audioUrl: String
    ): ResponseBody
}
