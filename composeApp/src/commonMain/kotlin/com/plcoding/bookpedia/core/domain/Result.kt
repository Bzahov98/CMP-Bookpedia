package com.plcoding.bookpedia.core.domain

/**
 * A sealed interface representing the result of an operation that can either succeed or fail.
 *
 * @param D The type of the successful result data.
 * @param E The type of the error, which extends the [Error] interface.
 */
sealed interface Result<out D, out E : Error> {
    /**
     * Represents a successful result.
     *
     * @param data The successful result data.
     */
    data class Success<out D>(val data: D) : Result<D, Nothing>

    /**
     * Represents an error result.
     *
     * @param error The error that occurred.
     */
    data class Error<out E : com.plcoding.bookpedia.core.domain.Error>(val error: E) :
        Result<Nothing, E>
}

/**
 * Transforms the data in a [Success] result using the provided mapping function.
 * If the result is an [Error], it returns the same error without modification.
 *
 * @param map The function to transform the successful result data.
 * @return A new [Result] with the transformed data or the same error.
 */
inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

/**
 * Converts a [Result] to an [EmptyResult], which is a type alias for `Result<Unit, E>`.
 * This is useful when the data is not needed, and only the error state is relevant.
 *
 * @return An [EmptyResult] with the same error or an empty success.
 */
fun <T, E : Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map { }
}

/**
 * Executes the given action if the result is a [Success], passing the data to the action.
 * Returns the original result.
 *
 * @param action The action to execute on the successful result data.
 * @return The original [Result].
 */
inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

/**
 * Executes the given action if the result is an [Error], passing the error to the action.
 * Returns the original result.
 *
 * @param action The action to execute on the error.
 * @return The original [Result].
 */
inline fun <T, E : Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }

        is Result.Success -> this
    }
}

/**
 * A type alias for `Result<Unit, E>`, representing a result
 * with no data and only an error state.
 *
 * @param E The type of the error, which extends the [Error] interface.
 */
typealias EmptyResult<E> = Result<Unit, E>