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
            scope.setContexts(
                "TPStreamsSDK",
                mapOf(
                    "SDK Version" to TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME,
                    "Provider" to TPStreamsSDK.provider.name,
                    "Player Id" to playerId,
                    "Error Type" to "Server error",
                    "Error Code" to exception.response?.code,
                    "Error Message" to exception.errorMessage,
                    "Org Code" to TPStreamsSDK.orgCode,
                    "Video ID" to params?.videoId,
                    "AccessToken" to params?.accessToken,
                    "userId" to params?.userId,
                )
            )
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
            scope.setContexts(
                "TPStreamsSDK",
                mapOf(
                    "SDK Version" to TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME,
                    "Provider" to TPStreamsSDK.provider.name,
                    "Player Id" to playerId,
                    "Error Type" to "Player error",
                    "Error Code" to error.errorCode,
                    "Error Message" to error.errorCodeName,
                    "Org Code" to TPStreamsSDK.orgCode,
                    "Video ID" to params?.videoId,
                    "AccessToken" to params?.accessToken,
                    "userId" to params?.userId,
                )
            )
        }
    }

    fun logDrmSessionException(error: DrmSessionException, params: TpInitParams?, playerId: String) {
        Sentry.captureMessage(
            "Player DRM error" +
                    " Code: ${error.errorCode}" +
                    " Message: ${error.message}" +
                    " Video ID: ${params?.videoId}" +
                    " AccessToken: ${params?.accessToken}" +
                    " Org Code: ${TPStreamsSDK.orgCode}"
        ) { scope ->
            scope.setTag("TPStreamsAndroidPlayerSDKVersion",TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME)
            scope.setTag("playerId", playerId)
            scope.setTag("userId", params?.userId ?: "")
            scope.setContexts(
                "TPStreamsSDK",
                mapOf(
                    "SDK Version" to TPSTREAMS_ANDROID_PALYER_SDK_VERSION_NAME,
                    "Provider" to TPStreamsSDK.provider.name,
                    "Player Id" to playerId,
                    "Error Type" to "Player DRM error",
                    "Error Code" to error.errorCode,
                    "Error Message" to error.message,
                    "Org Code" to TPStreamsSDK.orgCode,
                    "Video ID" to params?.videoId,
                    "AccessToken" to params?.accessToken,
                    "userId" to params?.userId,
                )
            )
        }
    }
}