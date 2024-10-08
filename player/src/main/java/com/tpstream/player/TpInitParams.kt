package com.tpstream.player

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

const val FIFTEEN_DAYS = 60 * 60 * 24 * 15

@Parcelize
data class TpInitParams (
    var autoPlay: Boolean = true,
    var accessToken: String? = null,
    var videoId: String? = null,
    var isDownloadEnabled: Boolean = false,
    var startAt: Long = 0L,
    var licenseDurationSeconds: Int = FIFTEEN_DAYS,
    var userId: String? = null,
    var initialResolutionHeight: Int? = null
): Parcelable {

    class Builder {
        private var autoPlay: Boolean = true
        private var accessToken: String? = null
        private var videoId: String? = null
        private var isDownloadEnabled: Boolean = false
        private var startAt: Long = 0L
        var licenseDurationSeconds: Int = FIFTEEN_DAYS
        var userId: String? = null
        private var initialResolutionHeight: Int? = null

        fun setAutoPlay(autoPlay: Boolean) = apply { this.autoPlay = autoPlay }
        fun startAt(timeInSeconds: Long) = apply { this.startAt = timeInSeconds }
        fun setAccessToken(accessToken: String) = apply { this.accessToken = accessToken }
        fun setVideoId(videoId: String) = apply { this.videoId = videoId }
        fun enableDownloadSupport(isDownloadEnabled: Boolean) = apply { this.isDownloadEnabled = isDownloadEnabled }
        fun setOfflineLicenseExpireTime(@androidx.annotation.IntRange(from = 300) expireTimeInSecond: Int) =
            apply { this.licenseDurationSeconds = expireTimeInSecond }
        fun setUserId(userId: String) = apply { this.userId = userId }
        fun setInitialResolution(height: Int) = apply { this.initialResolutionHeight = height }

        fun build(): TpInitParams {
            require(!accessToken.isNullOrBlank()) { "accessToken must not be null or empty" }
            require(!videoId.isNullOrBlank()) { "videoId must not be null or empty" }

            return TpInitParams(
                autoPlay = autoPlay,
                accessToken = accessToken!!,
                videoId = videoId!!,
                isDownloadEnabled = isDownloadEnabled,
                startAt = startAt,
                licenseDurationSeconds = licenseDurationSeconds,
                userId = userId,
                initialResolutionHeight = initialResolutionHeight
            )
        }
    }

    internal fun setNewAccessToken(newAccessToken: String){
        this.accessToken = newAccessToken
    }

    val startPositionInMilliSecs: Long
        get() = startAt * 1000L

    fun createParamsWithOtp(
        accessToken: String?,
        videoId: String?
    ): TpInitParams {
        return TpInitParams(
            accessToken = accessToken,
            videoId = videoId,
        )
    }

    companion object{
        fun createOfflineParams(videoId: String):TpInitParams{
            return TpInitParams(
                videoId = videoId,
                isDownloadEnabled = true,
                autoPlay = true,
                accessToken = "dummyAccessToken"
            )
        }

        fun createOfflineParams(videoId: String, userId: String):TpInitParams{
            return TpInitParams(
                videoId = videoId,
                isDownloadEnabled = true,
                autoPlay = true,
                accessToken = "dummyAccessToken",
                userId = userId
            )
        }
    }
}
