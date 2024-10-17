package com.tpstream.player.util

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.util.Log

internal class DeviceUtil {

    data class Resolution(val width: Int, val height: Int)

    data class CodecDetails(
        val codecName: String,
        val is1080pSupported: Boolean = false,
        val is4KSupported: Boolean = false,
        val is1080pAt1_75xSupported: Boolean = false,
        val is4KAtAt1_75xSupported: Boolean = false,
        val is1080pAt2xSupported: Boolean = false,
        val is4KAt2xSupported: Boolean = false,
        var isSelected: Boolean = false,
        val isSecure: Boolean = false
    ) {
        val priority: Int
            get() = when {
                is4KSupported -> 2
                is1080pSupported -> 1
                else -> 0
            }
    }

    companion object {
        val RESOLUTION_1080P = Resolution(1920, 1080)
        val RESOLUTION_4K = Resolution(3840, 2160)
        const val BASE_FRAME_RATE = 24.0
        const val FRAME_RATE_1_75X = BASE_FRAME_RATE * 1.75
        const val FRAME_RATE_2X = BASE_FRAME_RATE * 2

        fun getAvailableAVCCodecs(codecList: MediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)): List<CodecDetails> {
            return try {
                codecList.codecInfos
                    .filterNot { codecInfo -> codecInfo.isEncoder }
                    .filter { codecInfo -> codecInfo.supportedTypes.any { it == MediaFormat.MIMETYPE_VIDEO_AVC || it == MediaFormat.MIMETYPE_VIDEO_HEVC } }
                    .filter { codecInfo -> Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !codecInfo.isAlias }
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
                    is1080pSupported = it.isSupported(RESOLUTION_1080P),
                    is4KSupported = it.isSupported(RESOLUTION_4K),
                    is1080pAt1_75xSupported = it.isSupported(RESOLUTION_1080P, FRAME_RATE_1_75X),
                    is4KAtAt1_75xSupported = it.isSupported(RESOLUTION_4K, FRAME_RATE_1_75X),
                    is1080pAt2xSupported = it.isSupported(RESOLUTION_1080P, FRAME_RATE_2X),
                    is4KAt2xSupported = it.isSupported(RESOLUTION_4K, FRAME_RATE_2X),
                    isSecure = this.name.contains("secure")
                )
            }
        }

        private fun MediaCodecInfo.VideoCapabilities.isSupported(
            resolution: Resolution,
            frameRate: Double? = null
        ): Boolean {
            return if (frameRate == null) {
                isSizeSupported(resolution.width, resolution.height)
            } else {
                isSizeSupported(resolution.width, resolution.height) && areSizeAndRateSupported(
                    resolution.width,
                    resolution.height,
                    frameRate
                )
            }
        }
    }
}