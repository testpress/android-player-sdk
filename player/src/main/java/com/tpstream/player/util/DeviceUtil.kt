package com.tpstream.player.util

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.util.Log

internal class DeviceUtil {
    data class CodecDetails(
        val codecName: String,
        val is1080pSupported: Boolean = false,
        val is4KSupported: Boolean = false,
        val is1080pAt2xSupported: Boolean = false,
        val is4KAt2xSupported: Boolean = false,
        var isSelected: Boolean = false
    )

    companion object {
        private const val WIDTH_1080P = 1920
        private const val HEIGHT_1080P = 1080
        private const val WIDTH_4K = 3840
        private const val HEIGHT_4K = 2160
        private const val FRAME_RATE_2X = 48.0

        fun getAvailableAVCCodecs(codecList: MediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)): List<CodecDetails> {
            return try {
                codecList.codecInfos
                    .filterNot { codecInfo -> codecInfo.isEncoder }
                    .filter { codecInfo -> MediaFormat.MIMETYPE_VIDEO_AVC in codecInfo.supportedTypes }
                    .mapNotNull { it.toCodecDetails() }
            } catch (e: Exception) {
                Log.d("DeviceUtils", "Error fetching codec capabilities: ${e.message}")
                emptyList()
            }
        }

        fun MediaCodecInfo.toCodecDetails(): CodecDetails? {
            val videoCapabilities =
                this.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)?.videoCapabilities
            return videoCapabilities?.let {
                CodecDetails(
                    codecName = this.name,
                    is1080pSupported = it.isSizeSupported(WIDTH_1080P, HEIGHT_1080P),
                    is4KSupported = it.isSizeSupported(WIDTH_4K, HEIGHT_4K),
                    is1080pAt2xSupported = it.isSizeSupported(
                        WIDTH_1080P,
                        HEIGHT_1080P
                    ) && it.areSizeAndRateSupported(
                        WIDTH_1080P,
                        HEIGHT_1080P,
                        FRAME_RATE_2X
                    ),
                    is4KAt2xSupported = it.isSizeSupported(
                        WIDTH_4K,
                        HEIGHT_4K
                    ) && it.areSizeAndRateSupported(
                        WIDTH_4K,
                        HEIGHT_4K,
                        FRAME_RATE_2X
                    )
                )
            }
        }
    }
}