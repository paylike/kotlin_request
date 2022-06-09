# kotlin_request - Paylike low-level request helper
Request implementation for Kotlin

<a href="https://jitpack.io/#paylike/kotlin_request" target="_blank">
    <img src="https://jitpack.io/v/paylike/kotlin_request.svg" />
</a>

*This implementation is based on [Paylike/JS-Request](https://github.com/paylike/js-request)*

This is a low-level library used for making HTTP(s) requests to Paylike APIs. It
incorporates the conventions described in the
[Paylike API reference](https://github.com/paylike/api-reference).

## Example

```kotlin
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
```

## PaylikeRequester

The main executor of the requests
```kotlin
val requester = PaylikeRequester()
```
By default the requester is initiated with the following parameters:
```kotlin
    val log: Consumer<Any> = Consumer {
        println(it.toString())
    }
    val client: HttpHandler = ApacheClient()
```
Log is called when a request is made and the client is used to execute requests. You can override both if your 
desired functionality requires it.

#### `request` fun

Responsible for executing requests, has the following footprint:
```kotlin
suspend fun request(endpoint: String, opts: RequestOptions): PaylikeResponse
```
As it provides a timeout functionality it has to be called from inside a [coroutine](https://kotlinlang.org/docs/coroutines-overview.html)

## RequestOptions
Responsible for describing the options for a given request (except for the endpoint)
```kotlin
data class RequestOptions(
    val version: Int = 1, /// [Optional] Describes the version of the API
    val query: Map<String, String>? = null, /// [Optional] Queries attached to the request
    val data: JsonObject = buildJsonObject {}, /// [Optional] Data of the request body
    val clientId: String = "kotlin-1", /// [Optional] Identification of the given SDK client
    val method: String = "GET", /// [Optional] Method, either should be "POST" or "GET"
    val form: Boolean = false, /// [Optional] Indicates if the body is a form
    val formFields: Map<String, String>? = null, /// [Optional] Describes the fields in the form
    val timeout: Duration = 20.toDuration(DurationUnit.SECONDS) /// [Optional] Timeout
)
```

For building json objects, you can use the kotlin [serialization](https://kotlinlang.org/docs/serialization.html) library.

## Response

The requester on request responds with an [http4k Response object](https://www.http4k.org/api/org.http4k.core/-response/)

## Error handling

We expose exceptions in `com.github.paylike.kotlin_request.exceptions`

`request` may throw the following errors:

* RateLimitException

Happens if the application receives 429 from the API. The client retries automatically if `retry-after` header is present.

* PaylikeException

Happens when the API responds with an error but that error can be classified using [Paylike error codes](https://github.com/paylike/api-reference/blob/main/status-codes.md)

* ServerErrorException

Happens when the API responds with an unexpected message 

* TimeoutCancellationException _(comes from org.jetbrains.kotlinx:kotlinx-coroutines-core)_

Happens when the request could not finish in time and the coroutine timeout kills the process