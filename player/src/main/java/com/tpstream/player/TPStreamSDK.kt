package com.tpstream.player

import android.media.MediaCodecList
import android.media.MediaFormat

object TPStreamsSDK {
    private var _provider: Provider = Provider.TPStreams
    private var _orgCode: String? = null
    val provider : Provider get() = _provider
    val orgCode: String
        get() {
            if (_orgCode == null) {
                throw IllegalStateException("TPStreamsSDK is not initialized. You must call initialize first.")
            }
            return _orgCode!!
        }
    internal var codecCapabilitiesList = listOf<CodecCapabilities>()

    fun initialize(provider: Provider = Provider.TPStreams, orgCode: String) {
        if (orgCode.isEmpty()) {
            throw IllegalArgumentException("orgCode cannot be empty.")
        }

        this._orgCode = orgCode
        this._provider = provider
        codecCapabilitiesList = fetchAVCCodecCapabilities()
    }

    private fun fetchAVCCodecCapabilities(): List<CodecCapabilities> {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecCapabilitiesList = mutableListOf<CodecCapabilities>()

        for (codecInfo in codecList.codecInfos) {
            // Check if the codec is a decoder and supports AVC (H.264) format
            if (!codecInfo.isEncoder && codecInfo.supportedTypes.contains(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                val capabilities = codecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)
                val codecName = codecInfo.name

                capabilities.videoCapabilities?.let { videoCapabilities ->
                    // Check resolution support
                    val is720pSupported = videoCapabilities.isSizeSupported(1280, 720)
                    val is1080pSupported = videoCapabilities.isSizeSupported(1920, 1080)
                    val is4KSupported = videoCapabilities.isSizeSupported(3840, 2160)

                    // Check resolution support at 2x speed (48 fps)
                    val is720pSupportedAt2xSpeed = if (is720pSupported) {
                        videoCapabilities.areSizeAndRateSupported(1280, 720, 48.00)
                    } else false

                    val is1080pSupportedAt2xSpeed = if (is1080pSupported) {
                        videoCapabilities.areSizeAndRateSupported(1920, 1080, 48.00)
                    } else false

                    val is4KSupportedAt2xSpeed = if (is4KSupported) {
                        videoCapabilities.areSizeAndRateSupported(3840, 2160, 48.00)
                    } else false

                    // Add codec capabilities to the list
                    codecCapabilitiesList.add(
                        CodecCapabilities(
                            codecName = codecName,
                            is720pSupported = is720pSupported,
                            is1080pSupported = is1080pSupported,
                            is4KSupported = is4KSupported,
                            is720pSupportedAt2xSpeed = is720pSupportedAt2xSpeed,
                            is1080pSupportedAt2xSpeed = is1080pSupportedAt2xSpeed,
                            is4KSupportedAt2xSpeed = is4KSupportedAt2xSpeed,
                            hardwareAcceleration = codecInfo.isHardwareAccelerated
                        )
                    )
                }
            }
        }
        return codecCapabilitiesList
    }

    fun constructVideoInfoUrl(contentId: String?, accessToken: String?): String {
        val url = when (provider) {
            Provider.TestPress -> "https://%s.testpress.in/api/v2.5/video_info/%s/?access_token=%s"
            Provider.TPStreams -> "https://app.tpstreams.com/api/v1/%s/assets/%s/?access_token=%s"
        }
        return url.format(orgCode, contentId, accessToken)
    }

    fun constructDRMLicenseUrl(contentId: String?, accessToken: String?): String {
        val url = when (provider) {
            Provider.TestPress -> "https://%s.testpress.in/api/v2.5/drm_license_key/%s/?access_token=%s"
            Provider.TPStreams -> "https://app.tpstreams.com/api/v1/%s/assets/%s/drm_license/?access_token=%s"
        }
        return url.format(orgCode, contentId, accessToken)
    }

    internal fun constructOfflineDRMLicenseUrl(
        contentId: String?,
        accessToken: String?,
        licenseDurationSeconds: Int
    ): String {
        return "${
            constructDRMLicenseUrl(
                contentId,
                accessToken
            )
        }&drm_type=widevine&download=true&rental_duration_seconds=$licenseDurationSeconds&license_duration_seconds=$licenseDurationSeconds"
    }

    enum class Provider {
        TestPress,
        TPStreams
    }
}

internal data class CodecCapabilities(
    val codecName: String,
    val is720pSupported: Boolean = false,
    val is1080pSupported: Boolean = false,
    val is4KSupported: Boolean = false,
    val is720pSupportedAt2xSpeed: Boolean = false,
    val is1080pSupportedAt2xSpeed: Boolean = false,
    val is4KSupportedAt2xSpeed: Boolean = false,
    val hardwareAcceleration: Boolean = false,
    var isSelected: Boolean = false
)