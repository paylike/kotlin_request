package com.github.paylike.sample

import com.github.paylike.kotlin_request.PaylikeRequester
import com.github.paylike.kotlin_request.RequestOptions
import com.github.paylike.kotlin_request.exceptions.PaylikeException
import com.github.paylike.kotlin_request.exceptions.ServerErrorException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

fun main() {
    val requester = PaylikeRequester()
    val opts = RequestOptions(
        query = mapOf("foo" to "bar"),
        data = buildJsonObject {
            put("foo", "bar")
        },
        method = "POST",
    )
    runBlocking {
        try {
            val response = requester.request("http://your_domain.com", opts)
        } catch (e: TimeoutCancellationException) {
            /// Handle timeout
        } catch (e: PaylikeException) {
            /// Handle known API response
        } catch (e: ServerErrorException) {
            /// Handle unexpected issues
        }
    }
}