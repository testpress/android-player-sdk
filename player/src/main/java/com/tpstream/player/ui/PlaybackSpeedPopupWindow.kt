package com.tpstream.player.ui

import com.tpstream.player.TpStreamPlayerImpl
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tpstream.player.PlayerControlView
import com.tpstream.player.R
import com.tpstream.player.constants.PlaybackSpeed
import com.tpstream.player.ui.adapter.PlaybackSpeedAdapter

internal class PlaybackSpeedPopupWindow(
    private val player: TpStreamPlayerImpl,
    private val playerView: TPStreamPlayerView
) {
    private var popupWindow: PopupWindow? = null

    fun show() {
        val inflater =
            playerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_layout, null)

        popupWindow = createPopupWindow(popupView)
        setupRecyclerView(popupView, playerView.context)

        popupWindow?.height = (playerView.height * 0.75).toInt()
        popupWindow?.showAsDropDown(playerView, Int.MAX_VALUE, -((popupWindow?.height ?: 0) + 16))
    }

    private fun createPopupWindow(popupView: View): PopupWindow {
        return PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
    }

    private fun setupRecyclerView(popupView: View, context: Context) {
        val recyclerView: RecyclerView = popupView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = PlaybackSpeedAdapter(
            context,
            player.exoPlayer.playbackParameters.speed,
            PlaybackSpeed.values().toList()
        ) { playbackSpeed ->
            player.exoPlayer.playbackParameters =
                player.exoPlayer.playbackParameters.withSpeed(playbackSpeed.value)
            setPlaybackSpeedText(playbackSpeed.value)
            // Reset Controller show timeout to default
            playerView.playerView.controllerShowTimeoutMs = PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
            dismiss()
        }

        recyclerView.adapter = adapter
    }

    private fun setPlaybackSpeedText(speed: Float) {
        val playbackSpeedButton = playerView.findViewById<Button>(R.id.playback_speed)
        val playbackSpeed = PlaybackSpeed.values().find { it.value == speed }
        playbackSpeedButton.text = playbackSpeed?.text
    }

    fun dismiss() {
        popupWindow?.dismiss()
        popupWindow = null
    }
}
