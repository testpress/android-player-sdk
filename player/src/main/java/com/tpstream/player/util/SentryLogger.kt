package com.tpstream.player.util

import com.tpstream.player.*
import com.tpstream.player.BuildConfig.TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME
import com.tpstream.player.PlaybackException
import io.sentry.Sentry

internal object SentryLogger {

    fun generatePlayerIdString(): String {
        return (1..10)
            .map { ('a'..'z').toList() + ('0'..'9').toList() }
            .map { it.random() }
            .joinToString("")
    }

    fun logAPIException(exception: TPException, params: TpInitParams?, playerId: String) {
        Sentry.captureMessage(
            "Server error" +
                    " Code: ${exception.response?.code}" +
                    " Message: ${exception.response?.message}" +
                    " Video ID: ${params?.videoId}" +
                    " AccessToken: ${params?.accessToken}" +
                    " Org Code: ${TPStreamsSDK.orgCode}"
        ) { scope ->
            scope.setTag("TPStreamsAndroidPlayerSDKVersion",TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME)
            scope.setTag("playerId", playerId)
            scope.setTag("userId", params?.userId ?: "")
        }
    }

    fun logPlaybackException(error: PlaybackException, params: TpInitParams?, playerId: String) {
        Sentry.captureMessage(
            "Player error" +
                    " Code: ${error.errorCode}" +
                    " Code name: ${error.errorCodeName}" +
                    " Message: ${error.message}" +
                    " Video ID: ${params?.videoId}" +
                    " AccessToken: ${params?.accessToken}" +
                    " Org Code: ${TPStreamsSDK.orgCode}"
        ) { scope ->
            scope.setTag("TPStreamsAndroidPlayerSDKVersion",TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME)
            scope.setTag("playerId", playerId)
            scope.setTag("userId", params?.userId ?: "")
        }
    }

    fun logDrmSessionException(error: DrmSessionException, params: TpInitParams?, playerId: String) {
        Sentry.captureMessage(
            "Player error" +
                    " Code: ${error.errorCode}" +
                    " Message: ${error.message}" +
                    " Video ID: ${params?.videoId}" +
                    " AccessToken: ${params?.accessToken}" +
                    " Org Code: ${TPStreamsSDK.orgCode}"
        ) { scope ->
            scope.setTag("TPStreamsAndroidPlayerSDKVersion",TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME)
            scope.setTag("playerId", playerId)
            scope.setTag("userId", params?.userId ?: "")
        }
    }
}