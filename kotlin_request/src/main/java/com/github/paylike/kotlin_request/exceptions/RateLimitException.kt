package com.github.paylike.kotlin_request.exceptions

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Describe an exception thrown when the request got rate limited
 * by the API
 */
class RateLimitException(
    private val retryAfterMilliseconds: String?
) : Exception(if (retryAfterMilliseconds != null) "Request got rate limited for $retryAfterMilliseconds" else "Request got rate limited") {
    val retryAfter: Duration
        get() {
            return retryAfterMilliseconds?.toInt()?.toDuration(DurationUnit.MILLISECONDS)
                ?: Duration.ZERO
        }
}