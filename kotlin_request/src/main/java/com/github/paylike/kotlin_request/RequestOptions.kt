package com.github.paylike.kotlin_request

import com.github.paylike.kotlin_request.exceptions.VersionException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Describes options for a given request
 * @throws VersionException if the version is below 1
 */
data class RequestOptions(
    val version: Int = 1,
    val query: Map<String, String>? = null,
    val data: JsonObject = buildJsonObject {},
    val clientId: String = "kotlin-1",
    val method: String = "GET",
    val form: Boolean = false,
    val formFields: Map<String, String>? = null,
    val timeout: Duration = 20.toDuration(DurationUnit.SECONDS)
) {
    init {
        if (version < 1) {
            throw VersionException(version);
        }
    }
}