package com.tpstream.player

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TpInitParams (
    var autoPlay: Boolean? = null,
    var bufferingGoalMs: Int? = null,
    var endTimeMs: Int? = null,
    var forceHighestSupportedBitrate: Boolean? = null,
    var forceLowestBitrate: Boolean? = null,
    var maxVideoBitrateKbps: Int? = null,
    var mediaId: String? = null,
    var offlinePlayback: Boolean? = null,
    var accessToken: String? = null,
    var videoId: String? = null,
    var orgCode: String,
    var preferredCaptionsLanguage: String? = null,
    var resumeTimeMs: Int? = null,
    var signature: String? = null,
    var startTimeMs: Int? = null,
    var techOverride: Array<String>? = null
): Parcelable {
    
    class Builder {
        private var autoPlay: Boolean? = null
        private var bufferingGoalMs: Int? = null
        private var endTimeMs: Int? = null
        private var forceHighestSupportedBitrate: Boolean? = null
        private var forceLowestBitrate: Boolean? = null
        private var maxVideoBitrateKbps: Int? = null
        private var mediaId: String? = null
        private var offlinePlayback: Boolean? = null
        private var accessToken: String? = null
        private var videoId: String? = null
        private var orgCode: String? = null
        private var preferredCaptionsLanguage: String? = null
        private var resumeTimeMs: Int? = null
        private var signature: String? = null
        private var startTimeMs: Int? = null
        private var techOverride: Array<String>? = null

        fun setAutoPlay(autoPlay: Boolean) = apply { this.autoPlay = autoPlay }
        fun setBufferingGoalMs(bufferingGoalMs: Int) = apply { this.bufferingGoalMs = bufferingGoalMs }
        fun setForceHighestSupportedBitrate(forceHighestSupportedBitrate: Boolean) = apply { this.forceHighestSupportedBitrate = forceHighestSupportedBitrate }
        fun setForceLowestBitrate(forceLowestBitrate: Boolean) = apply { this.forceLowestBitrate = forceLowestBitrate }
        fun setMaxVideoBitrateKbps(maxVideoBitrateKbps: Int) = apply { this.maxVideoBitrateKbps = maxVideoBitrateKbps }
        fun setOfflinePlayback(offlinePlayback: Boolean) = apply { this.offlinePlayback = offlinePlayback }
        fun setAccessToken(accessToken: String) = apply { this.accessToken = accessToken }
        fun setVideoId(videoId: String) = apply { this.videoId = videoId }
        fun setOrgCode(subdomain: String) = apply { this.orgCode = subdomain }
        fun setPreferredCaptionsLanguage(preferredCaptionsLanguage: String) = apply { this.preferredCaptionsLanguage = preferredCaptionsLanguage }
        fun setResumeTime(resumeTimeMs: Int) = apply { this.resumeTimeMs = resumeTimeMs }
        fun setSignature(signature: String) = apply { this.signature = signature }
        fun setStartTimeMs(startTimeMs: Int) = apply { this.startTimeMs = startTimeMs }
        fun setEndTimeMs(endTimeMs: Int) = apply { this.endTimeMs = endTimeMs }
        fun setClips(startTimeMs: Int, endTimeMs: Int) = apply { this.startTimeMs = startTimeMs; this.endTimeMs = endTimeMs }
        fun setTechOverride(techOverride: Array<String>) = apply { this.techOverride = techOverride }

        fun build(): TpInitParams {
            if (orgCode == null) {
                throw Exception("orgCode should be provided")
            }

            return TpInitParams(
                autoPlay,
                bufferingGoalMs,
                endTimeMs,
                forceHighestSupportedBitrate,
                forceLowestBitrate,
                maxVideoBitrateKbps,
                mediaId,
                offlinePlayback,
                accessToken,
                videoId,
                orgCode!!,
                preferredCaptionsLanguage,
                resumeTimeMs,
                signature,
                startTimeMs,
                techOverride
            )
        }
    }

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
}
