package com.tpstream.player

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.Util.getRendererIndex
import com.tpstream.player.views.Util.getRendererIndex
import androidx.media3.ui.PlayerView
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding
import com.tpstream.player.views.AdvancedResolutionSelectionSheet
import com.tpstream.player.views.ResolutionOptions
import com.tpstream.player.views.SimpleVideoResolutionSelectionSheet

@UnstableApi class TpStreamPlayerFragment : Fragment() {

//    companion object {
//        fun newInstance() = TpStreamPlayerFragment()
//    }

    private lateinit var viewModel: TpStreamPlayerViewModel
    private val playbackStateListener: Player.Listener = PlayerListener()
    private var player: TpStreamPlayer? = null
    private var _player: ExoPlayer? = null
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    private var initializationListener: InitializationListener? = null
    lateinit var trackSelector:DefaultTrackSelector
    var selectedResolution = ResolutionOptions.AUTO
    lateinit var fullScreenDialog: Dialog
    private var isFullScreen = false
    lateinit var orientationEventListener: OrientationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackSelector = DefaultTrackSelector(requireContext())
        fullScreenDialog = object :Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                super.onBackPressed()
                exitFullScreen()
            }
        }
    }

    fun enableAutoFullScreenOnRotate() {
        orientationEventListener = OrientationListener(requireContext())
        orientationEventListener.setOnChangeListener { isLandscape ->
            activity?.let {
                if(isLandscape) {
                    showFullScreen()
                } else {
                    exitFullScreen()
                }
            }
        }
        orientationEventListener.start()
    }

    fun disableAutoFullScreenOnRotate() {
        if(::orientationEventListener.isInitialized) {
            orientationEventListener.disable()
        }
    }

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
        addCustomPlayerControls()
    }

    private fun addCustomPlayerControls() {
        addResolutionChangeControl()
        addFullScreenControl()
        addDownloadControls()
    }

    private fun addFullScreenControl() {
        viewBinding.videoView.findViewById<ImageButton>(R.id.fullscreen).setOnClickListener {
            if(isFullScreen) {
                exitFullScreen()
            } else {
                showFullScreen()
            }
        }
    }

    fun exitFullScreen() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        (viewBinding.videoView.parent as ViewGroup).removeView(viewBinding.videoView)
        viewBinding.mainFrameLayout.addView(viewBinding.videoView)
        viewBinding.videoView.findViewById<ImageButton>(R.id.fullscreen).setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fullscreen_24));
        fullScreenDialog.dismiss()
        isFullScreen = false
    }

    fun showFullScreen() {
        (viewBinding.videoView.parent as ViewGroup).removeView(viewBinding.videoView)
        fullScreenDialog.addContentView(viewBinding.videoView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        viewBinding.videoView.findViewById<ImageButton>(R.id.fullscreen).setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fullscreen_exit_24));
        fullScreenDialog.show()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        isFullScreen = true
    }

    private fun addResolutionChangeControl() {
        val resolutionButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_resolution)

        resolutionButton.setOnClickListener {
            val simpleVideoResolutionSelector = initializeVideoResolutionSelectionSheets()
            simpleVideoResolutionSelector.show(requireActivity().supportFragmentManager, SimpleVideoResolutionSelectionSheet.TAG)
        }
    }

    private fun initializeVideoResolutionSelectionSheets(): SimpleVideoResolutionSelectionSheet {
        val simpleVideoResolutionSelector =
            SimpleVideoResolutionSelectionSheet(player!!, selectedResolution)
        val advancedVideoResolutionSelector =
            AdvancedResolutionSelectionSheet(player!!, trackSelector.parameters)
        advancedVideoResolutionSelector.onClickListener =
            onAdvancedVideoResolutionSelection(advancedVideoResolutionSelector)
        simpleVideoResolutionSelector.onClickListener =
            onVideoResolutionSelection(
                simpleVideoResolutionSelector,
                advancedVideoResolutionSelector
            )
        return simpleVideoResolutionSelector
    }

    private fun onVideoResolutionSelection(
        videoResolutionSelector: SimpleVideoResolutionSelectionSheet,
        advancedVideoResolutionSelector: AdvancedResolutionSelectionSheet
    ) = DialogInterface.OnClickListener { p0, p1 ->
        this@TpStreamPlayerFragment.selectedResolution =
            videoResolutionSelector.selectedResolution
        if (videoResolutionSelector.selectedResolution == ResolutionOptions.ADVANCED) {
            advancedVideoResolutionSelector.show(
                requireActivity().supportFragmentManager,
                "AdvancedSheet"
            )
            return@OnClickListener
        }

        val parameters = videoResolutionSelector.selectedResolution.getTrackSelectionParameter(
            requireContext(),
            null
        )
        trackSelector.setParameters(parameters)
    }

    private fun onAdvancedVideoResolutionSelection(advancedVideoResolutionSelector: AdvancedResolutionSelectionSheet) =
        DialogInterface.OnClickListener { p0, p1 ->
            val mappedTrackInfo = trackSelector.currentMappedTrackInfo
            mappedTrackInfo?.let {
                val rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
                if (advancedVideoResolutionSelector.overrides.isNotEmpty()) {
                    val params = TrackSelectionParameters.Builder(requireContext())
                        .clearOverridesOfType(rendererIndex)
                        .addOverride(advancedVideoResolutionSelector.overrides.values.elementAt(0))
                        .build()
                    trackSelector.setParameters(params)
                }
            }
        }

    private fun addDownloadControls(){
        val downloadButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_download)
        downloadButton.setOnClickListener{
            val sheet = DownloadResolutionSelectionSheet(trackSelector.parameters, _player!!.currentTracks.groups,player?.videoInfo!!,player?.params!!)
            sheet.onClickListener = DialogInterface.OnClickListener { p0, p1 ->
                val mappedTrackInfo = trackSelector.currentMappedTrackInfo
                mappedTrackInfo?.let {
                    val rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
                    if (sheet.overrides.isNotEmpty()) {
                        val params = TrackSelectionParameters.Builder(requireContext())
                            .clearOverridesOfType(rendererIndex)
                            .addOverride(sheet.overrides.values.elementAt(0))
                            .build()
                        trackSelector.setParameters(params)
                    }
                }
            }
            sheet.show(requireActivity().supportFragmentManager, "AdvancedSheet")
        }
    }

    fun setOnInitializationListener(listener: InitializationListener) {
        this.initializationListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disableAutoFullScreenOnRotate()
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
        player = TpStreamPlayerImpl(_player!!,requireContext())
        this.initializationListener?.onInitializationSuccess(player!!)
    }

    private fun initializeExoplayer(): ExoPlayer {
        return ExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(getMediaSourceFactory())
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
                exoPlayer.addListener(playbackStateListener)
            }
    }

    private fun getMediaSourceFactory(): MediaSource.Factory {

        val cacheDataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
            .setCache(VideoDownloadManager(requireContext()).getDownloadCache())
            .setUpstreamDataSourceFactory(VideoDownloadManager(requireContext()).getHttpDataSourceFactory())
            .setCacheWriteDataSinkFactory(null)

        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext())
            .setDataSourceFactory(cacheDataSourceFactory)
        mediaSourceFactory.setDrmSessionManagerProvider {
            DefaultDrmSessionManager.Builder().build(CustomHttpDrmMediaCallback(player?.params?.orgCode!!, player?.params?.videoId!!, player?.params?.accessToken!!))
        }
        return mediaSourceFactory
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

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)
            Log.d("TAG", "onTracksChanged: ${tracks.groups[0].isSelected}")
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