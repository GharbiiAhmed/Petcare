package tn.petcare_android.data.model.chat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Chat and Conversation data models matching iOS ChatModels
 */

@JsonClass(generateAdapter = true)
data class Conversation(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "participants") val participants: List<ConversationParticipant>? = null,
    @Json(name = "lastMessage") val lastMessage: Message? = null,
    @Json(name = "lastMessageAt") val lastMessageAt: String? = null,
    @Json(name = "unreadCount") val unreadCount: Int? = null
) {
    // Helper to get normalized ID
    val normalizedId: String
        get() = id ?: _id ?: ""
}

@JsonClass(generateAdapter = true)
data class ConversationParticipant(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "avatarUrl") val avatarUrl: String? = null,
    @Json(name = "profileImage") val profileImage: String? = null
) {
    // Helper to get normalized ID
    val normalizedId: String
        get() = id ?: _id ?: ""
        
    // Helper to get avatar URL (backend uses 'profileImage')
    val normalizedAvatarUrl: String?
        get() = avatarUrl ?: profileImage
}

@JsonClass(generateAdapter = false)  // Using custom MessageAdapter
data class Message(
    val id: String? = null,
    val _id: String? = null,
    val conversation: String? = null,  // Backend returns "conversation" not "conversationId"
    val conversationId: String? = null,
    val senderId: String? = null,
    val recipientId: String? = null,
    val content: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val deletedAt: String? = null,
    val audioURL: String? = null,
    val isEdited: Boolean = false,
    val read: Boolean = false,
    val isDeleted: Boolean = false,
    val sender: ConversationParticipant? = null,
    val recipient: ConversationParticipant? = null  // Handled by MessageAdapter
) {
    // Helper to get normalized ID
    val normalizedId: String
        get() = id ?: _id ?: ""
    
    // Helper to get normalized conversation ID
    val normalizedConversationId: String
        get() = conversationId ?: conversation ?: ""
    
    // Helper to get normalized sender ID
    val normalizedSenderId: String
        get() = senderId ?: sender?.normalizedId ?: ""
    
    // Helper to get normalized recipient ID
    val normalizedRecipientId: String
        get() = recipientId ?: recipient?.normalizedId ?: ""
}

/**
 * ChatMessage for Socket.IO real-time events
 * iOS Reference: ChatModels.swift ChatMessage
 */
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val audioURL: String? = null,
    val sender: ConversationParticipant? = null
)

@JsonClass(generateAdapter = true)
data class CreateConversationRequest(
    @Json(name = "participantId") val participantId: String  // iOS uses single participantId
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "recipientId") val recipientId: String,           // iOS requires recipientId
    @Json(name = "content") val content: String,
    @Json(name = "conversationId") val conversationId: String? = null, // Optional - backend creates if null
    @Json(name = "audioURL") val audioURL: String? = null
)

@JsonClass(generateAdapter = true)
data class EditMessageRequest(
    @Json(name = "content") val content: String
)

data class ConversationResponse(
    val conversation: Conversation
)

data class MessagesResponse(
    val messages: List<Message>
)
