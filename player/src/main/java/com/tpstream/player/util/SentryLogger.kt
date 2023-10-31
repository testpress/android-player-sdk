package com.tpstream.player.util

import androidx.media3.common.PlaybackException
import com.tpstream.player.TPException
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import io.sentry.Sentry

internal object SentryLogger {
    fun logAPIException(exception: TPException, params: TpInitParams?){
        Sentry.captureMessage("Server error" +
                " Code: ${exception.response?.code}" +
                " Message: ${exception.response?.message}" +
                " Video ID: ${params?.videoId}" +
                " AccessToken: ${params?.accessToken}" +
                " Org Code: ${TPStreamsSDK.orgCode}")
    }

    fun logPlaybackException(error: PlaybackException,params: TpInitParams?){
        Sentry.captureMessage("Player error" +
                " Code: ${error.errorCode}" +
                " Code name: ${error.errorCodeName}" +
                " Message: ${error.message}" +
                " Video ID: ${params?.videoId}" +
                " AccessToken: ${params?.accessToken}" +
                " Org Code: ${TPStreamsSDK.orgCode}")
    }
}