package com.github.paylike.kotlin_request

import com.github.paylike.kotlin_request.exceptions.VersionException
import kotlinx.serialization.json.*
import org.http4k.core.*
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.Test
import org.junit.Assert.*

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
        val response = requester.request(
            endpoint = "http://localhost:9000",
            opts = opts
        )
        assertEquals(200, response.response.status.code)
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
        val response = requester.request(
            endpoint = "http://localhost:9000",
            opts = opts
        )
        assertEquals(200, response.response.status.code)
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
        val response = requester.request(
            endpoint = "http://localhost:9000",
            opts = opts
        )
        assertEquals(200, response.response.status.code)
        jettyServer.close()
    }

    @Test
    fun request_options() {
        assertThrows(VersionException::class.java) {
            RequestOptions(version = 0)
        };
    }
}
