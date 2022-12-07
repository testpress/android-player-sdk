package com.tpstream.player

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.media.MediaCodec
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.*
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.drm.MediaDrmCallbackException
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.views.Util.getRendererIndex
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding
import com.tpstream.player.models.OfflineVideoState
import com.tpstream.player.views.AdvancedResolutionSelectionSheet
import com.tpstream.player.views.DownloadResolutionSelectionSheet
import com.tpstream.player.views.ResolutionOptions
import com.tpstream.player.views.SimpleVideoResolutionSelectionSheet

class TpStreamPlayerFragment : Fragment(), DownloadCallback.Listener {

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
    lateinit var trackSelector: DefaultTrackSelector
    var selectedResolution = ResolutionOptions.AUTO
    lateinit var fullScreenDialog: Dialog
    private var isFullScreen = false
    lateinit var orientationEventListener: OrientationListener
    private lateinit var offlineVideoInfoViewModel: OfflineVideoInfoViewModel
    private lateinit var downloadButton : ImageButton
    private lateinit var resolutionButton : ImageButton
    private var downloadState :OfflineVideoState? = null
    private var showDownloadButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackSelector = DefaultTrackSelector(requireContext())
        fullScreenDialog = object :Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                super.onBackPressed()
                exitFullScreen()
            }
        }
        offlineVideoInfoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineVideoInfoViewModel(OfflineVideoInfoRepository(requireContext())) as T
            }
        }).get(OfflineVideoInfoViewModel::class.java)
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
        //viewModel = ViewModelProvider(this).get(TpStreamPlayerViewModel::class.java)
        initializePlayer()
        updateDownloadButtonImage()
        addCustomPlayerControls()
        DownloadCallback.invoke().callback = this
    }

    private fun updateDownloadButtonImage(){
        offlineVideoInfoViewModel.get(player?.params?.videoId!!).observe(viewLifecycleOwner) { offlineVideoInfo ->
            downloadState = when (offlineVideoInfo?.downloadState) {
                OfflineVideoState.DOWNLOADING ->{
                    downloadButton.setImageResource(R.drawable.ic_baseline_downloading_24)
                    OfflineVideoState.DOWNLOADING
                }
                OfflineVideoState.COMPLETE ->{
                    downloadButton.setImageResource(R.drawable.ic_baseline_file_download_done_24)
                    OfflineVideoState.COMPLETE
                }
                else -> {
                    downloadButton.setImageResource(R.drawable.ic_baseline_download_for_offline_24)
                    null
                }
            }
        }
    }

    override fun onDownloadsSuccess(videoId:String?) {
        if (videoId == player?.params?.videoId){
            reloadVideo()
        }
    }

    private fun reloadVideo(){
        val currentPosition = player?.getCurrentTime()
        val url = player?.videoInfo?.dashUrl!!
        val tpImp = player as TpStreamPlayerImpl
        tpImp.load(url,currentPosition!!)
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
        resolutionButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_resolution)
        resolutionButton.setOnClickListener {
            if (downloadState == OfflineVideoState.COMPLETE){
                Toast.makeText(requireContext(),"Quality Unavailable",Toast.LENGTH_SHORT).show()
            } else {
                val simpleVideoResolutionSelector = initializeVideoResolutionSelectionSheets()
                simpleVideoResolutionSelector.show(requireActivity().supportFragmentManager, SimpleVideoResolutionSelectionSheet.TAG)
            }
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

    private fun addDownloadControls() {
        downloadButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_download)
        if (showDownloadButton){
            downloadButton.visibility = View.VISIBLE
        }
        downloadButton.setOnClickListener {
            when (downloadState) {
                OfflineVideoState.COMPLETE -> {
                    Toast.makeText(requireContext(),"Download complete",Toast.LENGTH_SHORT).show()
                }
                OfflineVideoState.DOWNLOADING -> {
                    Toast.makeText(requireContext(),"Downloading",Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val downloadResolutionSelectionSheet = DownloadResolutionSelectionSheet(
                        player!!,
                        trackSelector.parameters,
                        player?.getCurrentTrackGroups()!!,
                    )
                    downloadResolutionSelectionSheet.show(
                        requireActivity().supportFragmentManager,
                        "DownloadSelectionSheet"
                    )
                    downloadResolutionSelectionSheet.setOnSubmitListener { downloadRequest,offlineVideoInfo ->
                        DownloadTask(requireContext()).start(downloadRequest)
                        offlineVideoInfo?.videoId = player?.params?.videoId!!
                        offlineVideoInfoViewModel.insert(offlineVideoInfo!!)
                    }
                }
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
        player = TpStreamPlayerImpl(_player!!, requireContext())
        this.initializationListener?.onInitializationSuccess(player!!)
    }

    private fun initializeExoplayer(): ExoPlayer {
        return ExoPlayer.Builder(requireActivity())
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
                exoPlayer.addListener(playbackStateListener)
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
        showDownloadButton = parameters.isDownloadEnabled
    }

    inner class PlayerListener : Player.Listener, DRMLicenseFetchCallback {
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

            if (isDRMException(error.cause!!)) {
                onDownloadsSuccess(player?.videoInfo?.dashUrl!!)
            }
        }

        override fun onLicenseFetchSuccess(keySetId: ByteArray) {
            Log.d("TAG", "onLicenseFetchSuccess: ")
        }

        override fun onLicenseFetchFailure() {
            Log.d("TAG", "onLicenseFetchFailure: ")
        }

        private fun isDRMException(cause: Throwable): Boolean {
            return cause is DrmSession.DrmSessionException || cause is MediaCodec.CryptoException || cause is MediaDrmCallbackException
        }

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