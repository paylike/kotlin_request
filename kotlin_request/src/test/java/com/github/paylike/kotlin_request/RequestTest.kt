package com.github.paylike.kotlin_request

import com.github.paylike.kotlin_request.exceptions.PaylikeException
import com.github.paylike.kotlin_request.exceptions.RateLimitException
import com.github.paylike.kotlin_request.exceptions.ServerErrorException
import com.github.paylike.kotlin_request.exceptions.VersionException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.http4k.core.*
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.Test
import org.junit.Assert.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class RequestTest {
    @Test
    fun paylike_requester_form() {
        val app: HttpHandler = fun(req: Request): Response {
            assertEquals(Method.POST, req.method)
            val headers = req.headers.toMap()
            assertEquals("1", headers.getValue("Accept-Version"))
            assertEquals("kotlin-1", headers.getValue("X-Client"))
            assertEquals("application/x-www-form-urlencoded", headers.getValue("Content-Type"))
            assertEquals("foo=bar", req.bodyString())
            assertEquals("${"foo=bar".length}", headers.getValue("Content-Length"))
            return Response(Status.OK)
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions(
            form = true,
            formFields = mapOf("foo" to "bar")
        )
        val requester = PaylikeRequester()
        runBlocking {
            val response = requester.request(
                endpoint = "http://localhost:9000",
                opts = opts
            )
            assertEquals(200, response.response.status.code)
        }
        jettyServer.close()
    }


    @Test
    fun paylike_requester_get() {
        val app: HttpHandler = fun(req: Request): Response {
            assertEquals(Method.GET, req.method)
            val headers = req.headers.toMap()
            assertEquals("1", headers.getValue("Accept-Version"))
            assertEquals("kotlin-1", headers.getValue("X-Client"))
            assertEquals("bar1", req.query("foo1"))
            assertEquals("bar2", req.query("foo2"))
            return Response(Status.OK)
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions(
            query = mapOf("foo1" to "bar1", "foo2" to "bar2"),
            method = "GET",
        )
        val requester = PaylikeRequester()
        runBlocking {
            val response = requester.request(
                endpoint = "http://localhost:9000",
                opts = opts
            )
            assertEquals(200, response.response.status.code)
        }
        jettyServer.close()
    }

    @Test
    fun paylike_requester_post() {
        val app: HttpHandler = fun(req: Request): Response {
            assertEquals(Method.POST, req.method)
            val headers = req.headers.toMap()
            assertEquals("1", headers.getValue("Accept-Version"))
            assertEquals("kotlin-1", headers.getValue("X-Client"))
            assertEquals("application/json", headers.getValue("Content-Type"))
            val body = Json.parseToJsonElement(req.bodyString())
            assertEquals("bar", body.jsonObject.getValue("foo").jsonPrimitive.content)
            assertEquals("bar1", req.query("foo1"))
            assertEquals("bar2", req.query("foo2"))
            return Response(Status.OK)
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions(
            query = mapOf("foo1" to "bar1", "foo2" to "bar2"),
            method = "POST",
            data = buildJsonObject {
                put("foo", "bar")
            })
        val requester = PaylikeRequester()
        runBlocking {
            val response = requester.request(
                endpoint = "http://localhost:9000",
                opts = opts
            )
            assertEquals(200, response.response.status.code)
        }
        jettyServer.close()
    }

    @Test
    fun paylike_requester_auto_retry() {
        var retried = false
        val app: HttpHandler = fun(_: Request): Response {
            if (retried) {
                return Response(Status.OK)
            }
            retried = true
            return Response(
                Status.TOO_MANY_REQUESTS
            ).headers(listOf("retry-after" to "1"))
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions()
        val requester = PaylikeRequester()
        runBlocking {
            val response = requester.request(
                endpoint = "http://localhost:9000",
                opts = opts
            )
            assertEquals(200, response.response.status.code)
        }
        jettyServer.close()
    }

    @Test
    fun paylike_requester_rate_limit() {
        val app: HttpHandler = fun(_: Request): Response {
            return Response(
                Status.TOO_MANY_REQUESTS
            )
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions()
        val requester = PaylikeRequester()
        assertThrows(RateLimitException::class.java) {
            runBlocking {
                requester.request(
                    endpoint = "http://localhost:9000",
                    opts = opts
                )
            }

        }
        jettyServer.close()
    }

    @Test
    fun paylike_requester_timeout() {
        val ch = Channel<Int>()
        val app: HttpHandler = fun(_: Request): Response {
            runBlocking {
                delay(4.toDuration(DurationUnit.SECONDS))
                ch.send(0)
            }
            return Response(Status.OK)
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions(timeout = 2.toDuration(DurationUnit.SECONDS))
        val requester = PaylikeRequester()
        assertThrows(TimeoutCancellationException::class.java) {
            runBlocking {
                requester.request(
                    endpoint = "http://localhost:9000",
                    opts = opts,
                )
            }
        }
        runBlocking {
            ch.receive()
        }
        jettyServer.close()
    }

    @Test
    fun paylike_requester_paylike_exception() {
        val app: HttpHandler = fun(_: Request): Response {
            return Response(
                Status.BAD_REQUEST
            ).body(Body(buildJsonObject {
                put("message", "foo")
                put("code", "FOO")
                put("errors", buildJsonArray {
                    add("bar")
                })
            }.toString()))
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions()
        val requester = PaylikeRequester()
        assertThrows(PaylikeException::class.java) {
            runBlocking {
                requester.request(
                    endpoint = "http://localhost:9000",
                    opts = opts
                )
            }

        }
        jettyServer.close()
    }

    @Test
    fun paylike_requester_server_error() {
        val app: HttpHandler = fun(_: Request): Response {
            return Response(
                Status.INTERNAL_SERVER_ERROR
            ).headers(listOf("foo" to "bar"))
        }
        val jettyServer = app.asServer(Jetty(9000)).start()
        val opts = RequestOptions()
        val requester = PaylikeRequester()
        assertThrows(ServerErrorException::class.java) {
            runBlocking {
                requester.request(
                    endpoint = "http://localhost:9000",
                    opts = opts
                )
            }
        }
        jettyServer.close()
    }

    @Test
    fun request_options() {
        assertThrows(VersionException::class.java) {
            RequestOptions(version = 0)
        }
    }
}
