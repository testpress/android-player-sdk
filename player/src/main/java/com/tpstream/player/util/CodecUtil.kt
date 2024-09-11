package com.tpstream.player.util

import android.media.MediaCodecList
import android.media.MediaFormat
import android.util.Log

internal data class CodecCapabilities(
    val codecName: String,
    val is1080pSupported: Boolean = false,
    val is4KSupported: Boolean = false,
    val is1080pSupportedAt2xSpeed: Boolean = false,
    val is4KSupportedAt2xSpeed: Boolean = false,
    var isSelected: Boolean = false
)

internal fun fetchAVCCodecCapabilities(): List<CodecCapabilities> {
    val codecCapabilitiesList = mutableListOf<CodecCapabilities>()

    try {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)

        for (codecInfo in codecList.codecInfos) {
            if (!codecInfo.isEncoder && MediaFormat.MIMETYPE_VIDEO_AVC in codecInfo.supportedTypes) {
                val videoCapabilities =
                    codecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC).videoCapabilities

                if (videoCapabilities != null) {
                    // Check support for different resolutions
                    val is1080pSupported = videoCapabilities.isSizeSupported(1920, 1080)
                    val is4KSupported = videoCapabilities.isSizeSupported(3840, 2160)

                    // Check support for resolutions at 2x speed (48 fps)
                    val is1080pSupportedAt2xSpeed =
                        is1080pSupported && videoCapabilities.areSizeAndRateSupported(
                            1920,
                            1080,
                            48.0
                        )
                    val is4KSupportedAt2xSpeed =
                        is4KSupported && videoCapabilities.areSizeAndRateSupported(
                            3840,
                            2160,
                            48.0
                        )

                    codecCapabilitiesList.add(
                        CodecCapabilities(
                            codecName = codecInfo.name,
                            is1080pSupported = is1080pSupported,
                            is4KSupported = is4KSupported,
                            is1080pSupportedAt2xSpeed = is1080pSupportedAt2xSpeed,
                            is4KSupportedAt2xSpeed = is4KSupportedAt2xSpeed
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e("CodecCapabilities", "Error fetching codec capabilities: ${e.message}")
        return emptyList()
    }

    return codecCapabilitiesList
}