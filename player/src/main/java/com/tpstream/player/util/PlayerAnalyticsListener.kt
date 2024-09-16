package com.tpstream.player.util

import com.tpstream.player.AnalyticsListener
import com.tpstream.player.AnalyticsListenerEventTime
import com.tpstream.player.TpStreamPlayerImpl

internal class PlayerAnalyticsListener(private val streamPlayer: TpStreamPlayerImpl) :
    AnalyticsListener {
    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListenerEventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        updateSelectedCodec(decoderName)
    }

    private fun updateSelectedCodec(decoderName: String) {
        // Iterate over all codecs to update their selection status
        streamPlayer.codecs.forEach { codec ->
            codec.isSelected = (codec.codecName == decoderName)
        }
    }
}