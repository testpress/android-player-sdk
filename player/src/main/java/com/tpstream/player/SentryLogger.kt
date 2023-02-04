package com.tpstream.player

import androidx.media3.common.PlaybackException
import io.sentry.Sentry

object SentryLogger {
    fun logAPIException(exception: TPException,params: TpInitParams?){
        Sentry.captureMessage("Server error" +
                " Code: ${exception.response?.code}" +
                " Message: ${exception.response?.message}" +
                " Video ID: ${params?.videoId}" +
                " AccessToken: ${params?.accessToken}" +
                " Org Code: ${params?.orgCode}")
    }

    fun logPlaybackException(error: PlaybackException,params: TpInitParams?){
        Sentry.captureMessage("Player error" +
                " Code: ${error.errorCode}" +
                " Code name: ${error.errorCodeName}" +
                " Message: ${error.message}" +
                " Video ID: ${params?.videoId}" +
                " AccessToken: ${params?.accessToken}" +
                " Org Code: ${params?.orgCode}")
    }
}