package com.github.paylike.kotlin_request.exceptions

/**
 * Thrown when an unexpected server error happens during communication
 */
class ServerErrorException(
    val status: Int?,
    val headers: List<Pair<String, String?>> = emptyList()
) : Exception("Unexpected server error")