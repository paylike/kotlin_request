package com.github.paylike.kotlin_request

import com.github.paylike.kotlin_request.exceptions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.http4k.asByteBuffer
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.length
import org.http4k.urlEncoded
import java.net.URLEncoder
import java.util.function.Consumer
import kotlin.coroutines.Continuation

/**
 * Used for executing requests towards the Paylike API
 */
class PaylikeRequester(
    private val log: Consumer<Any> = Consumer {
        println(it.toString())
    },
    private val client: HttpHandler = ApacheClient()
) {

    /**
     * Executes a prepared request
     */
    private suspend fun executeRequest(
        uri: Uri,
        opts: RequestOptions,
        request: Request
    ): PaylikeResponse {
        val op = mapOf(
            "t" to "request",
            "method" to opts.method,
            "url" to uri.toString(),
            "timeout" to opts.timeout.toString(),
            "form" to opts.form,
            "formFields" to opts.formFields,
        )
        log.accept(op)
        val execute = CoroutineScope(IO).async {
            client(request)
        }
        val resp = withTimeout(opts.timeout) {
            execute.await()
        }
        return when (resp.status.code) {
            200 -> PaylikeResponse(resp)
            429 -> {
                throw RateLimitException()
            }
            else -> {
                if (resp.status.code < 300) {
                    return PaylikeResponse(resp)
                }
                try {
                    val body = Json.parseToJsonElement(resp.bodyString())
                    val exceptionErrors = mutableListOf<String>()
                    val errors = body.jsonObject["errors"]
                    if (errors != null && !errors.jsonArray.isEmpty()) {
                        errors.jsonArray.forEach {
                            exceptionErrors.add(it.jsonPrimitive.content)
                        }
                    }
                    throw PaylikeException(
                        cause = body.jsonObject["message"]!!.jsonPrimitive.content,
                        code = body.jsonObject["code"]!!.jsonPrimitive.content,
                        statusCode = resp.status.code,
                        errors = exceptionErrors,
                    )
                } catch (e: Exception) {
                    throw if (e is PaylikeException) e else
                        ServerErrorException(
                            status = resp.status.code,
                            headers = resp.headers
                        )
                }

            }
        }
    }

    /**
     * URL Encodes a given query pair
     * @return Gives back an encoded version of the query in key=value& format
     */
    private fun encode(key: String, value: String): String {
        return "${URLEncoder.encode(key, "utf-8")}=${URLEncoder.encode(value, "utf-8")}&"
    }

    /**
     * Executes a request based on parameters towards an endpoint
     * @return Response wrapped in a utility class
     * @throws RateLimitException when the API indicates limitation
     * @throws PaylikeException when the API throws a known error
     * @throws ServerErrorException when the API responds with unexpected response
     * @throws TimeoutCancellationException when the API does not respond in the given time
     * @throws Exception when an invalid usage is detected (e.g. sending form without formFields)
     */
    suspend fun request(endpoint: String, opts: RequestOptions): PaylikeResponse {
        // val printingClient: HttpHandler = DebuggingFilters.PrintResponse().then(client)
        var uri = Uri.of(endpoint)
        val headers =
            mutableListOf(
                "X-Client" to opts.clientId,
                "Accept-Version" to opts.version.toString()
            )
        if (opts.form) {
            if (opts.formFields == null) {
                throw Exception("Cannot send a form with empty formFields")
            }
            val formBodyParts = opts.formFields.keys.map {
                "$it=${opts.formFields[it]?.urlEncoded()}"
            }.toList()
            val bodyBytes = formBodyParts.joinToString(separator = "&").asByteBuffer()
            headers.add("Content-Length" to bodyBytes.length().toString())
            headers.add("Content-Type" to "application/x-www-form-urlencoded")
            val request: Request = Request(Method.POST, uri).body(Body(bodyBytes)).headers(headers)
            return executeRequest(uri, opts, request)
        }
        if (opts.query != null) {
            var queries = "?"
            opts.query.keys.stream()
                .forEach {
                    queries += encode(it, opts.query.getValue(it))
                }
            queries = queries.substring(0, queries.length - 1)
            uri = Uri.of(endpoint + queries)
        }
        val request =
            when (opts.method.uppercase()) {
                Method.GET.name ->
                    Request(Method.GET, uri).headers(headers)
                Method.POST.name -> {
                    headers.add("Content-Type" to "application/json")
                    Request(Method.POST, uri).headers(headers).body(Json.encodeToString(opts.data))
                }
                else -> throw Exception("Unsupported method ${opts.method}")
            }
        return executeRequest(uri, opts, request)
    }
}