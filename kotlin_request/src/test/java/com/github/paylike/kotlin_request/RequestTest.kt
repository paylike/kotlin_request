package com.github.paylike.kotlin_request

import com.github.paylike.kotlin_request.exceptions.VersionException
import org.junit.Test
import org.junit.Assert.*

class RequestTest {
    @Test
    fun request_options() {
        assertThrows(VersionException::class.java) {
            RequestOptions(version = 0);
        };
    }
}
