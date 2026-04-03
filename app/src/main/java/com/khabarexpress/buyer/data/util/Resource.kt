package com.khabarexpress.buyer.data.util

/**
 * A wrapper class for handling different states of data operations
 * Used for network calls and database operations to represent Success, Error, and Loading states
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

/**
 * Extension function to safely execute a suspend function and wrap result in Resource
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(apiCall())
    } catch (e: Exception) {
        Resource.Error(
            message = e.message ?: "An unexpected error occurred",
            throwable = e
        )
    }
}
