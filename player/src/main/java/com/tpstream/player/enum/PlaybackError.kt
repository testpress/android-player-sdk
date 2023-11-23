package com.tpstream.player.enum

import com.tpstream.player.PlaybackException
import com.tpstream.player.TPException

enum class PlaybackError {
    NETWORK_CONNECTION_FAILED,
    NETWORK_CONNECTION_TIMEOUT,
    INVALID_ASSETS_ID,
    INVALID_ACCESS_TOKEN_FOR_ASSETS,
    EXPIRED_ACCESS_TOKEN_FOR_ASSETS,
    INVALID_ACCESS_TOKEN_FOR_DRM_LICENSE,
    UNSPECIFIED
}

fun TPException.toError(): PlaybackError {
    return when {
        this.isNetworkError() -> PlaybackError.NETWORK_CONNECTION_FAILED
        this.response?.code == 404 -> PlaybackError.INVALID_ASSETS_ID
        this.isUnauthenticated() -> PlaybackError.INVALID_ACCESS_TOKEN_FOR_ASSETS
        this.isServerError() -> PlaybackError.UNSPECIFIED
        else -> PlaybackError.UNSPECIFIED
    }
}


fun PlaybackException.toError(): PlaybackError {
    return when (this.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> PlaybackError.NETWORK_CONNECTION_FAILED
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> PlaybackError.NETWORK_CONNECTION_TIMEOUT
        PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> PlaybackError.INVALID_ACCESS_TOKEN_FOR_DRM_LICENSE
        else -> PlaybackError.UNSPECIFIED
    }
}