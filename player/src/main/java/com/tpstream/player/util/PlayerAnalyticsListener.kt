package com.tpstream.player.util

import android.util.Log
import com.tpstream.player.AnalyticsListener
import com.tpstream.player.AnalyticsListenerEventTime
import com.tpstream.player.TpStreamPlayer.PLAYBACK_STATE.STATE_READY
import com.tpstream.player.TpStreamPlayerImpl
import com.tpstream.player.constants.PlaybackSpeed

internal class PlayerAnalyticsListener(private val streamPlayer: TpStreamPlayerImpl) :
    AnalyticsListener {
    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListenerEventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        Log.d("TAG", "onVideoDecoderInitialized: $decoderName")
        updateSelectedCodec(decoderName)
    }

    private fun updateSelectedCodec(decoderName: String) {
        // Iterate over all codecs to update their selection status
        streamPlayer.codecs.forEach { codec ->
            codec.isSelected = (codec.codecName == decoderName)
        }
    }

    override fun onPlaybackStateChanged(
        eventTime: AnalyticsListenerEventTime,
        state: Int
    ) {
        if (state == STATE_READY) {
            updatePlaybackSpeedIfNecessary()
        }
    }

    private fun updatePlaybackSpeedIfNecessary() {
        val exoPlayer = streamPlayer.exoPlayer
        val currentPlaybackSpeed = exoPlayer.playbackParameters.speed
        val currentResolutionHeight = exoPlayer.videoFormat?.height ?: return
        val selectedCodec = streamPlayer.codecs.firstOrNull { it.isSelected } ?: return

        if (!isPlaybackSpeedSupported(
                selectedCodec,
                currentPlaybackSpeed,
                currentResolutionHeight
            )
        ) {
            selectAppropriatePlaybackSpeed(
                currentPlaybackSpeed,
                selectedCodec,
                currentResolutionHeight
            )
        }
    }

    private fun isPlaybackSpeedSupported(
        selectedCodec: DeviceUtil.CodecDetails,
        playbackSpeed: Float,
        resolutionHeight: Int
    ): Boolean {
        val codecSupport = when (resolutionHeight) {
            DeviceUtil.RESOLUTION_1080P.height -> mapOf(
                PlaybackSpeed.PLAYBACK_SPEED_1_75.value to selectedCodec.is1080pAt1_75xSupported,
                PlaybackSpeed.PLAYBACK_SPEED_2_0.value to selectedCodec.is1080pAt2xSupported
            )

            DeviceUtil.RESOLUTION_4K.height -> mapOf(
                PlaybackSpeed.PLAYBACK_SPEED_1_75.value to selectedCodec.is4KAtAt1_75xSupported,
                PlaybackSpeed.PLAYBACK_SPEED_2_0.value to selectedCodec.is4KAt2xSupported
            )

            else -> return true
        }

        return codecSupport[playbackSpeed] ?: false
    }

    private fun selectAppropriatePlaybackSpeed(
        currentPlaybackSpeed: Float,
        selectedCodec: DeviceUtil.CodecDetails,
        resolutionHeight: Int
    ) {
        val maxSupportedSpeed =
            getMaxSupportedPlaybackSpeedForResolution(selectedCodec, resolutionHeight)
        val newPlaybackSpeed = minOf(currentPlaybackSpeed, maxSupportedSpeed)
        setPlayerPlaybackSpeed(newPlaybackSpeed)
    }

    private fun getMaxSupportedPlaybackSpeedForResolution(
        selectedCodec: DeviceUtil.CodecDetails,
        resolutionHeight: Int
    ): Float {
        return when (resolutionHeight) {
            DeviceUtil.RESOLUTION_1080P.height -> getMaxSpeedForCodec(
                selectedCodec.is1080pAt2xSupported,
                selectedCodec.is1080pAt1_75xSupported
            )

            DeviceUtil.RESOLUTION_4K.height -> getMaxSpeedForCodec(
                selectedCodec.is4KAt2xSupported,
                selectedCodec.is4KAtAt1_75xSupported
            )

            else -> PlaybackSpeed.PLAYBACK_SPEED_1_5.value
        }
    }

    private fun getMaxSpeedForCodec(is2xSupported: Boolean, is1_75xSupported: Boolean): Float {
        return when {
            is2xSupported -> PlaybackSpeed.PLAYBACK_SPEED_2_0.value
            is1_75xSupported -> PlaybackSpeed.PLAYBACK_SPEED_1_75.value
            else -> PlaybackSpeed.PLAYBACK_SPEED_1_5.value
        }
    }

    private fun setPlayerPlaybackSpeed(playbackSpeed: Float) {
        val playbackParameters = streamPlayer.exoPlayer.playbackParameters.withSpeed(playbackSpeed)
        streamPlayer.exoPlayer.playbackParameters = playbackParameters
    }
}