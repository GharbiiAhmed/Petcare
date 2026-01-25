package tn.petcare_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.petcare_android.data.model.ai.*
import retrofit2.http.Path
import tn.petcare_android.data.model.ai.AIRemindersResponse
import tn.petcare_android.data.model.ai.AIStatusResponse
import tn.petcare_android.data.model.ai.AITipsResponse
import tn.petcare_android.data.model.ai.AIResponseModel


interface AIApi {
    
    @GET("ai/pets/{petId}/tips")
    suspend fun getTips(
        @Path("petId") petId: String
    ): AITipsResponse
    
    @GET("ai/pets/{petId}/reminders")
    suspend fun getReminders(
        @Path("petId") petId: String
    ): AIRemindersResponse
    
    @GET("ai/pets/{petId}/status")
    suspend fun getStatus(
        @Path("petId") petId: String
    ): AIStatusResponse

    @POST("chatbot/message")
    suspend fun sendChatMessage(
        @Body request: ChatbotMessageRequest
    ): ChatbotResponse

    @GET("chatbot/history")
    suspend fun getHistory(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ChatbotHistoryResponse

    @DELETE("chatbot/history")
    suspend fun deleteHistory(): Response<Unit>
}