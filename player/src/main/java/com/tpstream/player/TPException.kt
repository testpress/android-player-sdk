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

    companion object{
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

    fun isNetworkError(): Boolean {
        return errorType == ErrorType.NETWORK
    }

    fun isUnauthenticated(): Boolean {
        return statusCode == 401
    }

    fun isClientError(): Boolean {
        return statusCode in 400..499 && statusCode != 401
    }

    fun isServerError(): Boolean {
        return statusCode in 500..599
    }

}

enum class ErrorType {
    NETWORK,
    HTTP,
}
