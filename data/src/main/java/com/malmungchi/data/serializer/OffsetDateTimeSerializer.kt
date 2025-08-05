package com.malmungchi.data.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OffsetDateTimeSerializer : JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime>,
    JsonAdapter<OffsetDateTime>() {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): OffsetDateTime {
        return OffsetDateTime.parse(json.asString, formatter)
    }

    override fun serialize(
        src: OffsetDateTime,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        return JsonPrimitive(src.format(formatter))
    }

    @FromJson
    override fun fromJson(jsonReader: JsonReader): OffsetDateTime? {
        val string = jsonReader.nextString()
        val localDateTime = LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        // ✅ 한국 시간대(UTC+9)로 변환
        return localDateTime.atOffset(ZoneOffset.ofHours(9))
    }

    @ToJson
    override fun toJson(jsonWriter: JsonWriter, value: OffsetDateTime?) {
        jsonWriter.value(value?.format(formatter))
    }
}