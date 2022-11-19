package com.tpstream.player

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.views.Util.getRendererIndex
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding
import com.tpstream.player.views.AdvancedResolutionSelectionSheet
import com.tpstream.player.views.ResolutionOptions
import com.tpstream.player.views.VideoResolutionSelectionSheet

@UnstableApi class TpStreamPlayerFragment : Fragment() {

//    companion object {
//        fun newInstance() = TpStreamPlayerFragment()
//    }

    private lateinit var viewModel: TpStreamPlayerViewModel

    private var player: TpStreamPlayer? = null
    private var _player: ExoPlayer? = null
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    private var initializationListener: InitializationListener? = null
    lateinit var trackSelector:DefaultTrackSelector
    var selectedResolution = ResolutionOptions.AUTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackSelector = DefaultTrackSelector(requireContext())
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
        addDownloadControls()
    }

    private fun addResolutionChangeControl() {
        val resolutionButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_resolution)

        resolutionButton.setOnClickListener {
            val modalBottomSheet = VideoResolutionBottomSheet(trackSelector, _player!!.currentTracks.groups, selectedResolution)
            val sheet = AdvancedResolutionSelectionSheet(trackSelector.parameters, _player!!.currentTracks.groups)
            sheet.onClickListener = object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
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
            }
//            modalBottomSheet.onClickListener = onResolutionClickListener(modalBottomSheet)
            modalBottomSheet.onClickListener = object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    this@TpStreamPlayerFragment.selectedResolution = modalBottomSheet.selectedResolution
                    if (modalBottomSheet.selectedResolution == ResolutionOptions.ADVANCED) {
                        sheet.show(requireActivity().supportFragmentManager, "AdvancedSheet")
                        return
                    }
                    val parameters = modalBottomSheet.selectedResolution.getTrackSelectionParameter(requireContext(), null)
                    trackSelector.setParameters(parameters)
                    val rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO,
                        trackSelector.currentMappedTrackInfo!!
                    )
                    val currentTrackGroup = _player!!.currentTracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }[0].mediaTrackGroup
                    val index = trackSelector.currentMappedTrackInfo?.getTrackGroups(rendererIndex)?.indexOf(currentTrackGroup)
                    Log.d("TAG", "onClick: ${index}")
                }
            }
            modalBottomSheet.show(requireActivity().supportFragmentManager, VideoResolutionBottomSheet.TAG)
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
            sheet.show(requireActivity().supportFragmentManager, "AdvancedSheetDownload")
        }
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

        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext())
            .setDataSourceFactory(VideoDownloadManager(requireContext()).build())
        val downloadTask = DownloadTask("https://verandademo-cdn.testpress.in/institute/demoveranda/courses/my-course/videos/transcoded/697662f1cafb40f099b64c3562537c1b/video.mpd", requireContext())
        if (!downloadTask.isDownloaded()) {
            mediaSourceFactory.setDrmSessionManagerProvider {
                DefaultDrmSessionManager.Builder().build(CustomHttpDrmMediaCallback(
                    player?.params?.orgCode!!,
                    player?.params?.videoId!!,
                    player?.params?.accessToken!!
                ))
            }
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