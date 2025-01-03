package com.plcoding.bookpedia.core.data

import com.plcoding.bookpedia.core.domain.DataError
import com.plcoding.bookpedia.core.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/**
 * Executes a network call safely, catching common exceptions and returning a [Result] object.
 *
 * @param execute A lambda function that executes the network call and returns an [HttpResponse].
 * @return A [Result] object containing either the successful response body or an appropriate [DataError.Remote].
 */
suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Remote> {
    val response = try {
        execute()
    } catch(e: SocketTimeoutException) {
        return Result.Error(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: UnresolvedAddressException) {
        return Result.Error(DataError.Remote.NO_INTERNET)
    } catch (e: Exception) {
        // Ensure the coroutine is still active before returning the error
        // This is necessary because the coroutine might have been cancelled while waiting for the response
        coroutineContext.ensureActive()
        return Result.Error(DataError.Remote.UNKNOWN)
    }

    return responseToResult(response)
}

/**
 * Converts an [HttpResponse] to a [Result] object, handling different HTTP status codes and potential errors.
 *
 * @param response The [HttpResponse] to be converted.
 * @return A [Result] object containing either the successful response body or an appropriate [DataError.Remote].
 */
suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Remote> {
    return when(response.status.value) {
        // When it's a success, we try to parse the response body
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch(e: NoTransformationFoundException) {
                Result.Error(DataError.Remote.SERIALIZATION)
            }
        }
        // Request failed, we return the appropriate error - Timeout, Too Many Requests, Server Error, etc.
        408 -> Result.Error(DataError.Remote.REQUEST_TIMEOUT)
        429 -> Result.Error(DataError.Remote.TOO_MANY_REQUESTS)
        // Server set Error
        in 500..599 -> Result.Error(DataError.Remote.SERVER)
        // Everything else is set to unknown error if not described above
        else -> Result.Error(DataError.Remote.UNKNOWN)
    }
}