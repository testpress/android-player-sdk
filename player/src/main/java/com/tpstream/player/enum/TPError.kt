package com.tpstream.player.enum

import com.tpstream.player.PlaybackException
import com.tpstream.player.TPException

enum class Error {
    NETWORK_CONNECTION_FAILED,
    NETWORK_CONNECTION_TIMEOUT,
    INVALID_ASSETS_ID,
    INVALID_ACCESS_TOKEN_FOR_ASSETS,
    EXPIRED_ACCESS_TOKEN_FOR_ASSETS,
    INVALID_ACCESS_TOKEN_FOR_DRM_LICENSE,
    UNSPECIFIED
}

fun TPException.toError(): Error {
    return when {
        this.isNetworkError() -> Error.NETWORK_CONNECTION_FAILED
        this.response?.code == 404 -> Error.INVALID_ASSETS_ID
        this.isUnauthenticated() -> Error.INVALID_ACCESS_TOKEN_FOR_ASSETS
        this.isServerError() -> Error.UNSPECIFIED
        else -> Error.UNSPECIFIED
    }
}


fun PlaybackException.toError(): Error {
    return when (this.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> Error.NETWORK_CONNECTION_FAILED
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> Error.NETWORK_CONNECTION_TIMEOUT
        PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> Error.INVALID_ACCESS_TOKEN_FOR_DRM_LICENSE
        else -> Error.UNSPECIFIED
    }
}