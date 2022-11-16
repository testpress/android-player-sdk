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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding

class TpStreamPlayerFragment : Fragment() {

//    companion object {
//        fun newInstance() = TpStreamPlayerFragment()
//    }

    private lateinit var viewModel: TpStreamPlayerViewModel
    private val playerListener = PlayerListener()
    private var player: TpStreamPlayer? = null
    private var _player: ExoPlayer? = null
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    private var initializationListener: InitializationListener? = null

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
        _player!!.addListener(playerListener)
        player = TpStreamPlayerImpl(_player!!)
        this.initializationListener?.onInitializationSuccess(player!!)
    }

    private fun initializeExoplayer(): ExoPlayer {
        return ExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(getMediaSourceFactory())
            .setTrackSelector(getTrackSelector(requireActivity()))
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
            }
    }

    private fun getMediaSourceFactory(): MediaSource.Factory {
        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext())
        mediaSourceFactory.setDrmSessionManagerProvider {
            DefaultDrmSessionManager.Builder().build(CustomHttpDrmMediaCallback(player?.params?.orgCode!!, player?.params?.videoId!!, player?.params?.accessToken!!))
        }
        return mediaSourceFactory
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

    fun load(parameters: TpInitParams) {
        if (player == null) {
            throw Exception("Player is not initialized yet. `load` method should be called onInitializationSuccess")
        }

        player?.load(parameters) { exception ->
            requireActivity().runOnUiThread {
                viewBinding.errorMessage.visibility = View.VISIBLE
                viewBinding.errorMessage.text = "Error Occurred while playing video. Error code ${exception.errorMessage}.\n ID: ${parameters.videoId}"
            }
        }
        player?.setPlayWhenReady(parameters.autoPlay==true)
    }

    inner class PlayerListener : Player.Listener {
        private val TAG = "PlayerListener"

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY) {
                viewBinding.errorMessage.visibility = View.GONE
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            viewBinding.errorMessage.visibility = View.VISIBLE
            viewBinding.errorMessage.text = "Error occurred while playing video. \n ${error.errorCode} ${error.errorCodeName}"
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