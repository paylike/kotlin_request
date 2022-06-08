package com.github.paylike.kotlin_request.exceptions

/**
 * Describes a known error received from the server
 */
class PaylikeException(
    cause: String,
    val code: String,
    val statusCode: Int,
    val errors: List<String>
) : Exception(cause) {
}