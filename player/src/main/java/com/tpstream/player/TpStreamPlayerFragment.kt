package com.tpstream.player

import android.content.DialogInterface
import android.media.MediaCodec
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.drm.MediaDrmCallbackException
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.database.dao.VideoInfoDao
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding
import com.tpstream.player.models.VideoInfo
import com.tpstream.player.repository.VideoInfoRepository
import com.tpstream.player.viewmodels.VideoInfoViewModel
import com.tpstream.player.views.AdvancedResolutionSelectionSheet
import com.tpstream.player.views.DownloadResolutionSelectionSheet
import com.tpstream.player.views.ResolutionOptions
import com.tpstream.player.views.Util.getRendererIndex
import com.tpstream.player.views.VideoResolutionSelectionSheet
import kotlinx.coroutines.runBlocking

@UnstableApi
class TpStreamPlayerFragment : Fragment() {

//    companion object {
//        fun newInstance() = TpStreamPlayerFragment()
//    }

    //private lateinit var viewModel: TpStreamPlayerViewModel

    private var player: TpStreamPlayer? = null
    private var _player: ExoPlayer? = null
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    private var initializationListener: InitializationListener? = null
    lateinit var trackSelector: DefaultTrackSelector
    var selectedResolution = ResolutionOptions.AUTO
    //private lateinit var videoInfoDao: VideoInfoDao
    private var videoInfo: VideoInfo? = null

    private lateinit var viewModel: VideoInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackSelector = DefaultTrackSelector(requireContext())
        //videoInfoDao = TPStreamsDatabase.invoke(requireContext()).videoInfoDao()
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VideoInfoViewModel(VideoInfoRepository(requireContext())) as T
            }
        }).get(VideoInfoViewModel::class.java)
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
        ///viewModel = ViewModelProvider(this).get(TpStreamPlayerViewModel::class.java)
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
            val videoResolutionSelector = VideoResolutionSelectionSheet(player!!, selectedResolution)
            val advancedVideoResolutionSelector = AdvancedResolutionSelectionSheet(player!!, trackSelector.parameters)
            advancedVideoResolutionSelector.onClickListener =
                onAdvancedVideoResolutionSelection(advancedVideoResolutionSelector)
            videoResolutionSelector.onClickListener =
                onVideoResolutionSelection(videoResolutionSelector, advancedVideoResolutionSelector)

            videoResolutionSelector.show(requireActivity().supportFragmentManager, VideoResolutionSelectionSheet.TAG)
        }
    }

    private fun onVideoResolutionSelection(
        videoResolutionSelector: VideoResolutionSelectionSheet,
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

    private fun addDownloadControls() {
        val downloadButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_download)

        try {
            val downloadTask = DownloadTask(
                TPStreamsDatabase.invoke(requireContext()).videoInfoDao().getVideoUrlByVideoId(player?.params?.videoId!!)?.dashUrl!!,
                requireContext()
            )
            if (downloadTask.isDownloaded()) {
                downloadButton.setImageResource(R.drawable.ic_baseline_file_download_done_24)
                return
            }
        } catch (exception :Exception){
            Log.d("TAG", "addDownloadControls: Video Not Download")
        }
        
        downloadButton.setOnClickListener {
            val downloadResolutionSelectionSheet = DownloadResolutionSelectionSheet(
                player!!,
                trackSelector.parameters,
                _player!!.currentTracks.groups,
            )
            downloadResolutionSelectionSheet.show(requireActivity().supportFragmentManager, "AdvancedSheetDownload")
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
                exoPlayer.addListener(PlayerListener())
            }
    }

    private fun getMediaSourceFactory(): MediaSource.Factory {
        var downloadTask:DownloadTask? = null
        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext())
            .setDataSourceFactory(VideoDownloadManager(requireContext()).build())
        try {
            downloadTask = DownloadTask(
                TPStreamsDatabase.invoke(requireContext()).videoInfoDao().getVideoUrlByVideoId(player?.params?.videoId!!)?.dashUrl!!,
                requireContext()
            )
        } catch (exception : Exception){
            Log.d("TAG", "getMediaSourceFactory: Video Not Download")
        }
        if (downloadTask == null || !downloadTask.isDownloaded()) {
            mediaSourceFactory.setDrmSessionManagerProvider {
                DefaultDrmSessionManager.Builder().build(
                    CustomHttpDrmMediaCallback(
                        requireContext(),
                        player?.params?.orgCode!!,
                        player?.params?.videoId!!,
                        player?.params?.accessToken!!
                    )
                )
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

    inner class PlayerListener : Player.Listener, DRMLicenseFetchCallback {
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

        override fun onPlayerError(error: PlaybackException) {
            if (isDRMException(error.cause!!)) {
                val downloadTask = DownloadTask(player?.videoInfo?.dashUrl!!, requireActivity())
                drmLicenseRetries += 1
                if (drmLicenseRetries < 2 && downloadTask.isDownloaded()) {
                    OfflineDRMLicenseHelper.renewLicense(player?.videoInfo?.dashUrl!!,player?.params!!, requireActivity(), this)
                }
            }
        }

        override fun onLicenseFetchSuccess(keySetId: ByteArray) {
            requireActivity().runOnUiThread(Runnable {
                val mediaItem: MediaItem = TpStreamPlayerImpl(
                    _player!!,
                    requireContext()
                ).getMediaItem(player?.videoInfo?.dashUrl!!)
                _player?.setMediaItem(mediaItem)
                _player?.prepare()
                _player?.playWhenReady = true
            })
        }

        override fun onLicenseFetchFailure() {
            TODO("Not yet implemented")
        }

        private fun isDRMException(cause: Throwable): Boolean {
            return cause is DrmSession.DrmSessionException || cause is MediaCodec.CryptoException || cause is MediaDrmCallbackException
        }

        private var drmLicenseRetries = 0

    }


    class PlayerAnalyticsListener : AnalyticsListener {
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