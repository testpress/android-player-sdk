package com.tpstream.player

import okhttp3.Response
import java.io.IOException

data class TPException(
    val errorMessage: String?,
    val response: Response?,
    val errorType: ErrorType,
    val exception: Throwable?
) : RuntimeException() {

    private var statusCode: Int = 0

    init {
        if (response != null) statusCode = response.code
    }

    companion object {
        fun httpError(response: Response): TPException {
            val message = "${response.code} ${response.message}"
            return TPException(
                message,
                response,
                ErrorType.HTTP,
                null
            )
        }

        fun networkError(exception: IOException): TPException {
            return TPException(
                exception.message,
                null,
                ErrorType.NETWORK,
                exception
            )
        }
    }

    fun isNetworkError() = errorType == ErrorType.NETWORK

    fun isTimeoutError(): Boolean {
        var current: Throwable? = exception
        while (current != null) {
            if (current is java.net.SocketTimeoutException ||
                current is java.io.InterruptedIOException ||
                current is java.util.concurrent.TimeoutException) {
                return true
            }
            current = current.cause
        }
        return false
    }

    fun isUnauthenticated() = statusCode == 401

    fun isClientError() = statusCode in 400..499 && statusCode != 401

    fun isServerError() = statusCode in 500..599

}

enum class ErrorType {
    NETWORK,
    HTTP,
}
