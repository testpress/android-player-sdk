package com.tpstream.player.util

import android.widget.Button
import androidx.media3.common.PlaybackParameters
import com.tpstream.player.AnalyticsListener
import com.tpstream.player.AnalyticsListenerEventTime
import com.tpstream.player.R
import com.tpstream.player.constants.PlaybackSpeed
import com.tpstream.player.ui.TPStreamPlayerView

internal class PlayerViewAnalyticsListener(private val tpStreamPlayerView: TPStreamPlayerView) :
    AnalyticsListener {

    override fun onPlaybackParametersChanged(
        eventTime: AnalyticsListenerEventTime,
        playbackParameters: PlaybackParameters
    ) {
        updatePlaybackSpeedText(playbackParameters.speed)
    }

    private fun updatePlaybackSpeedText(speed: Float) {
        val playbackSpeedButton = tpStreamPlayerView.findViewById<Button>(R.id.playback_speed)
        val playbackSpeedText = PlaybackSpeed.values().find { it.value == speed }
        playbackSpeedButton.text = playbackSpeedText?.text
    }
}