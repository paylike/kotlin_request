package com.github.paylike.kotlin_request.exceptions

import com.github.paylike.kotlin_request.exceptions.apistatuscodes.ApiCodesEnum

/** Describes a known error received from the server */
class PaylikeException(
    cause: String,
    val code: ApiCodesEnum,
    val statusCode: Int,
    val errors: List<String>
) : RequestException(cause)
