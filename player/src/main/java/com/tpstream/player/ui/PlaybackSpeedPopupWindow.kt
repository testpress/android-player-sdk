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
    private val tpStreamPlayerView: TPStreamPlayerView
) {
    var playbackSpeedPopupWindow: PopupWindow? = null

    fun show() {
        val popupView = inflatePopupView(tpStreamPlayerView.context)
        playbackSpeedPopupWindow = createPopupWindow(popupView)
        setupRecyclerView(popupView, tpStreamPlayerView.context)
        adjustPopupWindowHeight()
        increaseControllerTimeoutForPopup()
        showPopupWindow()
    }

    private fun inflatePopupView(context: Context): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflater.inflate(R.layout.popup_window_layout, null)
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
        recyclerView.adapter = createAdapter(context)
    }

    private fun createAdapter(context: Context) = PlaybackSpeedAdapter(
        context,
        player.exoPlayer.playbackParameters.speed,
        PlaybackSpeed.values().toList()
    ) { playbackSpeed ->
        setPlayerPlaybackSpeed(playbackSpeed.value)
        updatePlaybackSpeedText(playbackSpeed.value)
        dismiss()
    }

    private fun setPlayerPlaybackSpeed(speed: Float) {
        player.exoPlayer.playbackParameters =
            player.exoPlayer.playbackParameters.withSpeed(speed)
    }

    private fun updatePlaybackSpeedText(speed: Float) {
        val playbackSpeedButton = tpStreamPlayerView.findViewById<Button>(R.id.playback_speed)
        val playbackSpeedText = PlaybackSpeed.values().find { it.value == speed }
        playbackSpeedButton.text = playbackSpeedText?.text
    }

    private fun adjustPopupWindowHeight() {
        playbackSpeedPopupWindow?.height =
            (tpStreamPlayerView.height * POPUP_WINDOW_HEIGHT_RATIO).toInt()
    }

    private fun increaseControllerTimeoutForPopup() {
        // Since we are adding the custom playback speed selection option, we don't have access
        // to control the player controller auto-hide option. So whenever the user clicks the
        // playback speed option, we set the player controller timeout duration to 10 seconds.
        // This helps the user select the speed option without interruption.
        tpStreamPlayerView.playerView.controllerShowTimeoutMs =
            PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS * INCREASED_TIMEOUT_TWICE
    }

    private fun showPopupWindow() {
        playbackSpeedPopupWindow?.showAsDropDown(
            tpStreamPlayerView,
            Int.MAX_VALUE,
            -((playbackSpeedPopupWindow?.height ?: 0) + POPUP_WINDOW_OFFSET)
        )
    }

    fun dismiss() {
        restoreDefaultControllerTimeout()
        playbackSpeedPopupWindow?.dismiss()
        playbackSpeedPopupWindow = null
    }

    private fun restoreDefaultControllerTimeout() {
        // We set 10 seconds for the player controller hide timeout when opening the
        // playback speed option. We reset the value to the default when dismissing the
        // playback speed options.
        tpStreamPlayerView.playerView.controllerShowTimeoutMs =
            PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
    }

    companion object {
        private const val POPUP_WINDOW_HEIGHT_RATIO = 0.75
        private const val INCREASED_TIMEOUT_TWICE = 2
        private const val POPUP_WINDOW_OFFSET = 16
    }
}