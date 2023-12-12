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

internal fun TPException.toError(): PlaybackError {
    return when {
        this.isNetworkError() -> PlaybackError.NETWORK_CONNECTION_FAILED
        this.response?.code == 404 -> PlaybackError.INVALID_ASSETS_ID
        this.isUnauthenticated() -> PlaybackError.INVALID_ACCESS_TOKEN_FOR_ASSETS
        this.isServerError() -> PlaybackError.UNSPECIFIED
        else -> PlaybackError.UNSPECIFIED
    }
}


internal fun PlaybackException.toError(): PlaybackError {
    return when (this.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> PlaybackError.NETWORK_CONNECTION_FAILED
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> PlaybackError.NETWORK_CONNECTION_TIMEOUT
        PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> PlaybackError.INVALID_ACCESS_TOKEN_FOR_DRM_LICENSE
        else -> PlaybackError.UNSPECIFIED
    }
}

internal fun TPException.getErrorMessage(playerId: String): String {
    return when {
        this.isNetworkError() -> "5004\n No Internet Connection.\n playerId: $playerId"
        this.response?.code == 404 -> "5001\n Resource Not Found.\n playerId: $playerId"
        this.isUnauthenticated() -> "5002\n Unauthorized Access.\n playerId: $playerId"
        this.isServerError() -> "5005\n Server Error.\n playerId: $playerId"
        else -> "5100\n Unknown Error.\n playerId: $playerId"
    }
}


internal fun PlaybackException.getErrorMessage(playerId: String): String {
    return when (this.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "5004\n No Internet Connection.\n playerId: $playerId"
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "5006\n Network Timeout, Please try again.\n playerId: $playerId"
        PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> "5004\n Couldn't fetch license key.\n playerId: $playerId"
        else -> "5100\n Unknown Error.\n playerId: $playerId"
    }
}