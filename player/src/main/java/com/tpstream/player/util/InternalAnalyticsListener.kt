package com.tpstream.player.util

import com.tpstream.player.AnalyticsListener
import com.tpstream.player.AnalyticsListenerEventTime
import com.tpstream.player.TpStreamPlayerImpl

internal class InternalAnalyticsListener(private val playerImpl: TpStreamPlayerImpl) : AnalyticsListener {
    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListenerEventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        playerImpl.codecCapabilitiesList.takeIf { it.isNotEmpty() }
            ?.firstOrNull { it.codecName == decoderName }
            ?.apply { isSelected = true }
    }
}