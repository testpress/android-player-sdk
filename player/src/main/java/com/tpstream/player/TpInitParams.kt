package com.tpstream.player

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TpInitParams (
    var autoPlay: Boolean? = null,
    var bufferingGoalMs: Int? = null,
    var forceHighestSupportedBitrate: Boolean? = null,
    var forceLowestBitrate: Boolean? = null,
    var maxVideoBitrateKbps: Int? = null,
    var mediaId: String? = null,
    var offlinePlayback: Boolean? = null,
    var accessToken: String? = null,
    var videoId: String? = null,
    var orgCode: String,
    var preferredCaptionsLanguage: String? = null,
    var signature: String? = null,
    var techOverride: Array<String>? = null,
    var isDownloadEnabled: Boolean = false,
    var startAt: Long = 0L,
    internal var product: String? = null, // The product should be Testpress or TpStreams
    internal var isTPStreams: Boolean = false
): Parcelable {
    
    class Builder {
        private val testpress = "testpress"
        private var tpstreams = "tpstreams"
        private var autoPlay: Boolean? = null
        private var bufferingGoalMs: Int? = null
        private var forceHighestSupportedBitrate: Boolean? = null
        private var forceLowestBitrate: Boolean? = null
        private var maxVideoBitrateKbps: Int? = null
        private var mediaId: String? = null
        private var offlinePlayback: Boolean? = null
        private var accessToken: String? = null
        private var videoId: String? = null
        private var orgCode: String? = null
        private var preferredCaptionsLanguage: String? = null
        private var signature: String? = null
        private var techOverride: Array<String>? = null
        private var isDownloadEnabled: Boolean = false
        private var startAt: Long = 0L
        private var product: String = testpress // The product default value as testpress
        private var isTPStreams: Boolean = false

        fun setAutoPlay(autoPlay: Boolean) = apply { this.autoPlay = autoPlay }
        fun startAt(timeInSeconds: Long) = apply { this.startAt = timeInSeconds }
        fun setBufferingGoalMs(bufferingGoalMs: Int) = apply { this.bufferingGoalMs = bufferingGoalMs }
        fun setForceHighestSupportedBitrate(forceHighestSupportedBitrate: Boolean) = apply { this.forceHighestSupportedBitrate = forceHighestSupportedBitrate }
        fun setForceLowestBitrate(forceLowestBitrate: Boolean) = apply { this.forceLowestBitrate = forceLowestBitrate }
        fun setMaxVideoBitrateKbps(maxVideoBitrateKbps: Int) = apply { this.maxVideoBitrateKbps = maxVideoBitrateKbps }
        fun setOfflinePlayback(offlinePlayback: Boolean) = apply { this.offlinePlayback = offlinePlayback }
        fun setAccessToken(accessToken: String) = apply { this.accessToken = accessToken }
        fun setVideoId(videoId: String) = apply { this.videoId = videoId }
        fun setOrgCode(subdomain: String) = apply { this.orgCode = subdomain }
        fun setPreferredCaptionsLanguage(preferredCaptionsLanguage: String) = apply { this.preferredCaptionsLanguage = preferredCaptionsLanguage }
        fun setSignature(signature: String) = apply { this.signature = signature }
        fun setTechOverride(techOverride: Array<String>) = apply { this.techOverride = techOverride }
        fun enableDownloadSupport(isDownloadEnabled: Boolean) = apply { this.isDownloadEnabled = isDownloadEnabled }
        fun setProduct(product: String) = apply {
            this.product = if (product.lowercase() == tpstreams) tpstreams else testpress
        }

        fun build(): TpInitParams {
            if (orgCode == null) {
                throw Exception("orgCode should be provided")
            }
            validateProduct()

            return TpInitParams(
                autoPlay,
                bufferingGoalMs,
                forceHighestSupportedBitrate,
                forceLowestBitrate,
                maxVideoBitrateKbps,
                mediaId,
                offlinePlayback,
                accessToken,
                videoId,
                orgCode!!,
                preferredCaptionsLanguage,
                signature,
                techOverride,
                isDownloadEnabled,
                startAt,
                product,
                isTPStreams
            )
        }

        private fun validateProduct(){
            isTPStreams = product == tpstreams
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
