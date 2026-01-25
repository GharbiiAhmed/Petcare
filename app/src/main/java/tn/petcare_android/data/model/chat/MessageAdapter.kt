package tn.petcare_android.data.model.chat

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Custom adapter for Message to handle recipient field that can be either String or Object
 */
class MessageAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Message {
        reader.beginObject()
        
        var id: String? = null
        var _id: String? = null
        var conversation: String? = null
        var conversationId: String? = null
        var senderId: String? = null
        var recipientId: String? = null
        var content: String = ""
        var createdAt: String = ""
        var updatedAt: String? = null
        var deletedAt: String? = null
        var audioURL: String? = null
        var isEdited: Boolean = false
        var read: Boolean = false
        var isDeleted: Boolean = false
        var sender: ConversationParticipant? = null
        var recipient: ConversationParticipant? = null
        
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "_id" -> _id = reader.nextString()
                "conversation" -> conversation = reader.nextString()
                "conversationId" -> conversationId = reader.nextString()
                "senderId" -> senderId = reader.nextString()
                "recipientId" -> recipientId = reader.nextString()
                "content" -> content = reader.nextString()
                "createdAt" -> createdAt = reader.nextString()
                "updatedAt" -> updatedAt = reader.nextString()
                "deletedAt" -> deletedAt = reader.nextString()
                "audioURL" -> audioURL = reader.nextString()
                "isEdited" -> isEdited = reader.nextBoolean()
                "read" -> read = reader.nextBoolean()
                "isDeleted" -> isDeleted = reader.nextBoolean()
                "sender" -> {
                    sender = when (reader.peek()) {
                        JsonReader.Token.STRING -> ConversationParticipant(_id = reader.nextString())
                        JsonReader.Token.BEGIN_OBJECT -> parseConversationParticipant(reader)
                        else -> {
                            reader.skipValue()
                            null
                        }
                    }
                }
                "recipient" -> {
                    recipient = when (reader.peek()) {
                        JsonReader.Token.STRING -> ConversationParticipant(_id = reader.nextString())
                        JsonReader.Token.BEGIN_OBJECT -> parseConversationParticipant(reader)
                        else -> {
                            reader.skipValue()
                            null
                        }
                    }
                }
                else -> reader.skipValue()
            }
        }
        
        reader.endObject()
        
        return Message(
            id = id,
            _id = _id,
            conversation = conversation,
            conversationId = conversationId,
            senderId = senderId,
            recipientId = recipientId,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            audioURL = audioURL,
            isEdited = isEdited,
            read = read,
            isDeleted = isDeleted,
            sender = sender,
            recipient = recipient
        )
    }
    
    private fun parseConversationParticipant(reader: JsonReader): ConversationParticipant {
        reader.beginObject()
        var id: String? = null
        var _id: String? = null
        var name: String? = null
        var email: String? = null
        var avatarUrl: String? = null
        var profileImage: String? = null
        
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "_id" -> _id = reader.nextString()
                "name" -> name = reader.nextString()
                "email" -> email = reader.nextString()
                "avatarUrl" -> avatarUrl = reader.nextString()
                "profileImage" -> profileImage = reader.nextString()
                else -> reader.skipValue()
            }
        }
        
        reader.endObject()
        
        return ConversationParticipant(
            id = id,
            _id = _id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            profileImage = profileImage
        )
    }
    
    @ToJson
    fun toJson(writer: JsonWriter, value: Message) {
        writer.beginObject()
        value.id?.let { writer.name("id").value(it) }
        value._id?.let { writer.name("_id").value(it) }
        value.conversation?.let { writer.name("conversation").value(it) }
        value.conversationId?.let { writer.name("conversationId").value(it) }
        value.senderId?.let { writer.name("senderId").value(it) }
        value.recipientId?.let { writer.name("recipientId").value(it) }
        writer.name("content").value(value.content)
        writer.name("createdAt").value(value.createdAt)
        value.updatedAt?.let { writer.name("updatedAt").value(it) }
        value.deletedAt?.let { writer.name("deletedAt").value(it) }
        value.audioURL?.let { writer.name("audioURL").value(it) }
        writer.name("isEdited").value(value.isEdited)
        writer.name("read").value(value.read)
        writer.name("isDeleted").value(value.isDeleted)
        value.sender?.let { writeConversationParticipant(writer, "sender", it) }
        value.recipient?.let { writeConversationParticipant(writer, "recipient", it) }
        writer.endObject()
    }
    
    private fun writeConversationParticipant(writer: JsonWriter, name: String, participant: ConversationParticipant) {
        writer.name(name)
        writer.beginObject()
        participant.id?.let { writer.name("id").value(it) }
        participant._id?.let { writer.name("_id").value(it) }
        participant.name?.let { writer.name("name").value(it) }
        participant.email?.let { writer.name("email").value(it) }
        participant.avatarUrl?.let { writer.name("avatarUrl").value(it) }
        participant.profileImage?.let { writer.name("profileImage").value(it) }
        writer.endObject()
    }
}

