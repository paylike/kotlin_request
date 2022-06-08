package com.github.paylike.kotlin_request

import com.github.paylike.kotlin_request.exceptions.VersionException
import org.http4k.core.Uri
import org.junit.Test
import org.junit.Assert.*
import java.util.function.Consumer

class RequestTest {
    @Test
    fun paylike_requester() {
        val opts = RequestOptions(
            query = mapOf("key1" to "value1", "key2" to "value2")
        ,method = "POST"
        )
        val requester = PaylikeRequester()
        requester.request(endpoint = "https://random-data-api.com/api/users/random_user", opts = opts)
    }

    @Test
    fun request_options() {
        assertThrows(VersionException::class.java) {
            RequestOptions(version = 0)
        };
    }
}
