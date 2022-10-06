package com.tpstream.player

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
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

    private var player: ExoPlayer? = null
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private val playbackStateListener: Player.Listener = PlayerListener()
    private val playerAnalyticsListener: AnalyticsListener = PlayerAnalyticsListener()

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
        // TODO: Use the ViewModel
    }

    fun initialize(context: Context) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
        Log.d(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
        Log.d(TAG, "onResume: ")
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
        activity?.let { activity ->
            player = ExoPlayer.Builder(activity)
                .setTrackSelector(getTrackSelector(activity))
                .build()
                .also { exoPlayer ->
                    prepareExoPlayer(exoPlayer)
                    viewBinding.videoView.player = exoPlayer
                }
            Log.d(TAG, "initializePlayer: ")
        }
    }

    private fun getTrackSelector(activity: FragmentActivity): DefaultTrackSelector {
        return DefaultTrackSelector(activity).apply {
            setParameters(buildUponParameters().setMaxVideoSize(1920, 1080))
        }
    }

    private fun prepareExoPlayer(exoPlayer: ExoPlayer) {
        val mediaItem = MediaItem.Builder()
            .setUri(getString(R.string.media_url_dash))
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.seekTo(currentItem, playbackPosition)
        exoPlayer.addListener(playbackStateListener)
        exoPlayer.addAnalyticsListener(playerAnalyticsListener)
        exoPlayer.prepare()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
        Log.d(TAG, "onStop: ")
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.removeAnalyticsListener(playerAnalyticsListener)
            exoPlayer.release()
        }
        player = null
        Log.d(TAG, "releasePlayer: ")
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