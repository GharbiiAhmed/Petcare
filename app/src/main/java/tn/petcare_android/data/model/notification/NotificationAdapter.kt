package tn.petcare_android.data.model.notification

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import tn.petcare_android.data.model.booking.BookingUser

/**
 * Custom adapter to handle recipient/sender fields that can be either String (ID) or Object (populated user)
 */
class RecipientAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BookingUser? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                // If it's a string (just an ID), skip it and return null
                reader.skipValue()
                null
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                // If it's an object, parse it as BookingUser
                reader.beginObject()
                var id: String? = null
                var _id: String? = null
                var name: String? = null
                var email: String? = null
                
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> id = reader.nextString()
                        "_id" -> _id = reader.nextString()
                        "name" -> name = reader.nextString()
                        "email" -> email = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                
                BookingUser(
                    id = id,
                    _id = _id,
                    name = name,
                    email = email
                )
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
    
    @ToJson
    fun toJson(writer: JsonWriter, value: BookingUser?) {
        if (value != null) {
            writer.beginObject()
            writer.name("id").value(value.id)
            writer.name("_id").value(value._id)
            writer.name("name").value(value.name)
            writer.name("email").value(value.email)
            writer.endObject()
        } else {
            writer.nullValue()
        }
    }
}

