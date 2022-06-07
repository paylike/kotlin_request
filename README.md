# kotlin_request - Paylike low-level request helper
Request implementation for Kotlin

<a href="https://jitpack.io/#paylike/kotlin_request" target="_blank">
    <img src="https://jitpack.io/v/paylike/kotlin_request.svg" />
</a>

*This implementation is based on [Paylike/JS-Request](https://github.com/paylike/js-request)*

This is a low-level library used for making HTTP(s) requests to Paylike APIs. It
incorporates the conventions described in the
[Paylike API reference](https://github.com/paylike/api-reference).

This function is usually put behind a retry mechanism. Paylike APIs _will_
expect any client to gracefully handle a rate limiting response and expects them
to retry.


## Example

```kotlin
  
```

