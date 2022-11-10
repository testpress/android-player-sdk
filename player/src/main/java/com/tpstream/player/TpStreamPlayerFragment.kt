package com.tpstream.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding

class TpStreamPlayerFragment : Fragment() {

//    companion object {
//        fun newInstance() = TpStreamPlayerFragment()
//    }

    private lateinit var viewModel: TpStreamPlayerViewModel

    private var player: TpStreamPlayer? = null
    private var _player: ExoPlayer? = null
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    lateinit var initializationListener: InitializationListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentTpStreamPlayerBinding.inflate(layoutInflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TpStreamPlayerViewModel::class.java)
        initializePlayer()
    }

    fun setOnInitializationListener(listener: InitializationListener) {
        this.initializationListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    private fun hideSystemUi() {
        activity?.let { activity ->
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            WindowInsetsControllerCompat(activity.window, viewBinding.videoView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            Log.d(TAG, "hideSystemUi: ")
        }
    }

    private fun initializePlayer() {
        _player = initializeExoplayer()
        player = TpStreamPlayerImpl(_player!!)
        this.initializationListener.onInitializationSuccess(player!!)
    }

    private fun initializeExoplayer(): ExoPlayer {
        return ExoPlayer.Builder(requireActivity())
            .setTrackSelector(getTrackSelector(requireActivity()))
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
            }
    }

    private fun getTrackSelector(activity: FragmentActivity): DefaultTrackSelector {
        return DefaultTrackSelector(activity).apply {
            setParameters(buildUponParameters().setMaxVideoSize(1920, 1080))
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            player?.release()
        }
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            player?.release()
        }
        Log.d(TAG, "onStop: ")
    }

    class PlayerListener : Player.Listener {
        private val TAG = "PlayerListener"

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString")
        }
    }


    class PlayerAnalyticsListener: AnalyticsListener {
        private val TAG = "AnalyticsListener"
        override fun onRenderedFirstFrame(
            eventTime: AnalyticsListener.EventTime,
            output: Any,
            renderTimeMs: Long
        ) {
            Log.d(TAG, "onRenderedFirstFrame: ")
            super.onRenderedFirstFrame(eventTime, output, renderTimeMs)
        }

        override fun onAudioUnderrun(
            eventTime: AnalyticsListener.EventTime,
            bufferSize: Int,
            bufferSizeMs: Long,
            elapsedSinceLastFeedMs: Long
        ) {
            Log.d(TAG, "onAudioUnderrun: ")
            super.onAudioUnderrun(eventTime, bufferSize, bufferSizeMs, elapsedSinceLastFeedMs)
        }

        override fun onDroppedVideoFrames(
            eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long
        ) {
            Log.d(TAG, "onDroppedVideoFrames: ")
            super.onDroppedVideoFrames(eventTime, droppedFrames, elapsedMs)
        }
    }

}

interface InitializationListener {
    fun onInitializationSuccess(player: TpStreamPlayer)
}