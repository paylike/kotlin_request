package com.github.paylike.kotlin_request.exceptions

/**
 * Describe an exception thrown when the request got rate limited
 * by the API
 */
class RateLimitException(
) : Exception("Request got rate limited")