package tn.petcare_android.data.model.booking

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Custom adapter to handle pet field that can be either String (ID) or Object (populated pet)
 */
class PetAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BookingPet? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                // If it's a string (just an ID), skip it and return null
                reader.skipValue()
                null
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                // If it's an object, parse it as BookingPet
                reader.beginObject()
                var id: String? = null
                var _id: String? = null
                var name: String? = null
                var species: String? = null
                
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> id = reader.nextString()
                        "_id" -> _id = reader.nextString()
                        "name" -> name = reader.nextString()
                        "species" -> species = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                
                BookingPet(
                    id = id,
                    _id = _id,
                    name = name,
                    species = species
                )
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
    
    @ToJson
    fun toJson(writer: JsonWriter, value: BookingPet?) {
        if (value != null) {
            writer.beginObject()
            writer.name("id").value(value.id)
            writer.name("_id").value(value._id)
            writer.name("name").value(value.name)
            writer.name("species").value(value.species)
            writer.endObject()
        } else {
            writer.nullValue()
        }
    }
}

