package dev.hotwire.strada

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class StradaJsonConverter {
    companion object {
        const val NO_CONVERTER =
            "A Strada.config.jsonConverter must be set to encode or decode json"

        const val INVALID_CONVERTER =
            "The configured json converter must implement a StradaJsonTypeConverter " +
                "or use the provided KotlinXJsonConverter."

        inline fun <reified T> toObject(jsonData: String): T? {
            val converter = requireNotNull(Strada.config.jsonConverter) { NO_CONVERTER }

            return when (converter) {
                is KotlinXJsonConverter -> converter.toObject<T>(jsonData)
                is StradaJsonTypeConverter -> converter.toObject(jsonData, T::class.java)
                else -> throw IllegalStateException(INVALID_CONVERTER)
            }
        }

        inline fun <reified T> toJson(data: T): String {
            val converter = requireNotNull(Strada.config.jsonConverter) { NO_CONVERTER }

            return when (converter) {
                is KotlinXJsonConverter -> converter.toJson(data)
                is StradaJsonTypeConverter -> converter.toJson(data, T::class.java)
                else -> throw IllegalStateException(INVALID_CONVERTER)
            }
        }
    }
}

abstract class StradaJsonTypeConverter : StradaJsonConverter() {
    abstract fun <T> toObject(jsonData: String, type: Class<T>): T?
    abstract fun <T> toJson(data: T, type: Class<T>): String
}

class KotlinXJsonConverter : StradaJsonConverter() {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
    }

    inline fun <reified T> toObject(jsonData: String): T? {
        return try {
            json.decodeFromString(jsonData)
        } catch (e: Exception) {
            logException(e)
            null
        }
    }

    inline fun <reified T> toJson(data: T): String {
        return json.encodeToString(data)
    }

    fun logException(e: Exception) {
        logError("kotlinXJsonConverterFailedWithError", e)
    }
}
