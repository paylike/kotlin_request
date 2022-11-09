package com.github.paylike.kotlin_request.exceptions

import com.github.paylike.kotlin_request.exceptions.api.ApiCodes

/** Describes a known error received from the server */
class PaylikeException(
    cause: String,
    val code: ApiCodes,
    val statusCode: Int,
    val errors: List<String>
) : RequestException(cause)
