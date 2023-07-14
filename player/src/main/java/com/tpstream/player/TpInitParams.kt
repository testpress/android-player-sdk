package com.tpstream.player

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TpInitParams (
    var autoPlay: Boolean? = null,
    var accessToken: String? = null,
    var videoId: String? = null,
    var orgCode: String,
    var isDownloadEnabled: Boolean = false,
    var startAt: Long = 0L
): Parcelable {
    
    class Builder {
        private var autoPlay: Boolean? = null
        private var accessToken: String? = null
        private var videoId: String? = null
        private var orgCode: String? = null
        private var isDownloadEnabled: Boolean = false
        private var startAt: Long = 0L

        fun setAutoPlay(autoPlay: Boolean) = apply { this.autoPlay = autoPlay }
        fun startAt(timeInSeconds: Long) = apply { this.startAt = timeInSeconds }
        fun setAccessToken(accessToken: String) = apply { this.accessToken = accessToken }
        fun setVideoId(videoId: String) = apply { this.videoId = videoId }
        fun setOrgCode(subdomain: String) = apply { this.orgCode = subdomain }
        fun enableDownloadSupport(isDownloadEnabled: Boolean) = apply { this.isDownloadEnabled = isDownloadEnabled }

        fun build(): TpInitParams {
            if (orgCode == null) {
                throw Exception("orgCode should be provided")
            }

            return TpInitParams(
                autoPlay,
                accessToken,
                videoId,
                orgCode!!,
                isDownloadEnabled,
                startAt
            )
        }
    }

    val startPositionInMilliSecs: Long
        get() = startAt * 1000L

    fun createParamsWithOtp(
        orgCode: String,
        accessToken: String?,
        videoId: String?
    ): TpInitParams {
        return TpInitParams(
            accessToken = accessToken,
            videoId = videoId,
            orgCode = orgCode
        )
    }

    companion object{
        fun createOfflineParams(videoId: String):TpInitParams{
            return TpInitParams(
                videoId = videoId,
                orgCode = "",
                isDownloadEnabled = true,
                autoPlay = true
            )
        }
    }
}
