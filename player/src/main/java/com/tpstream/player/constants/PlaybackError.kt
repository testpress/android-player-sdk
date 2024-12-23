package com.tpstream.player.constants

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
        this.isNetworkError() -> "Oops! It seems like you're not connected to the internet. Please check your connection and try again.\n Error code: 5004. Player Id: $playerId"
        this.response?.code == 404 -> "The video is not available. Please try another one.\n Error code: 5001. Player Id: $playerId"
        this.isUnauthenticated() -> "Sorry, you don't have permission to access this video. Please check your credentials and try again.\n Error code: 5002. Player Id: $playerId"
        this.isServerError() -> "We're sorry, but there's an issue on our server. Please try again later.\n Error code: 5005. Player Id: $playerId"
        else -> "Oops! Something went wrong. Please contact support for assistance and provide details about the issue.\n Error code: 5100. Player Id: $playerId"
    }
}


internal fun PlaybackException.getErrorMessage(playerId: String): String {
    return when (this.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Oops! It seems like you're not connected to the internet. Please check your connection and try again.\n Player code: ${this.errorCode}. Player Id: $playerId"
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "The request took too long to process due to a slow or unstable network connection. Please try again.\n Player code: ${this.errorCode}. Player Id: $playerId"
        PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> "There was an issue fetching the license key for this video. Please try again later.\n Player code: ${this.errorCode}. Player Id: $playerId"
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "There was an issue initializing the video decoder. Please try restarting your device or playing a different video. For more details, visit https://tpstreams.com/help/troubleshooting-steps-for-error-code-4001.\\n Player code: ${this.errorCode}. Player Id: $playerId"
        else -> "Oops! Something went wrong. Please contact support for assistance and provide details about the issue.\n Player code: ${this.errorCode}. Player Id: $playerId"
    }
}

internal fun TPException.getErrorMessageForDownload(): String {
    return when {
        this.isNetworkError() -> "Please check your connection and try again"
        this.response?.code == 404 -> "The video is not available. Please try another one."
        this.isUnauthenticated() -> "Sorry, you don't have permission to download this video"
        this.isServerError() -> "We're sorry, but there's an issue on our server. Please try again later."
        else -> "Oops! Something went wrong. Please contact support for assistance and provide details about the issue."
    }
}