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
        fun getAvailableAVCCodecs(): List<CodecDetails> {
            return try {
                val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
                codecList.codecInfos
                    .filterNot { it.isEncoder }
                    .filter { MediaFormat.MIMETYPE_VIDEO_AVC in it.supportedTypes }
                    .mapNotNull { createCodecDetails(it) }
            } catch (e: Exception) {
                Log.e("DeviceUtils", "Error fetching codec capabilities: ${e.message}")
                emptyList()
            }
        }

        private fun createCodecDetails(codecInfo: MediaCodecInfo): CodecDetails? {
            val videoCapabilities =
                codecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)?.videoCapabilities
            return videoCapabilities?.let {
                CodecDetails(
                    codecName = codecInfo.name,
                    is1080pSupported = it.isSizeSupported(1920, 1080),
                    is4KSupported = it.isSizeSupported(3840, 2160),
                    is1080pAt2xSupported = it.isSizeSupported(
                        1920,
                        1080
                    ) && it.areSizeAndRateSupported(
                        1920,
                        1080,
                        48.0
                    ),
                    is4KAt2xSupported = it.isSizeSupported(
                        3840,
                        2160
                    ) && it.areSizeAndRateSupported(
                        3840,
                        2160,
                        48.0
                    )
                )
            }
        }
    }
}