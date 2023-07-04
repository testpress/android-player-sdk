package com.tpstream.player.ui

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
import androidx.core.view.isVisible
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
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import com.tpstream.player.*
import com.tpstream.player.offline.DRMLicenseFetchCallback
import com.tpstream.player.offline.DownloadCallback
import com.tpstream.player.offline.DownloadTask
import com.tpstream.player.EncryptionKeyRepository
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.offline.InternetConnectivityChecker
import com.tpstream.player.offline.OfflineDRMLicenseHelper
import com.tpstream.player.util.OrientationListener
import com.tpstream.player.R
import com.tpstream.player.util.SentryLogger
import com.tpstream.player.TpStreamPlayerImpl
import com.tpstream.player.ui.viewmodel.VideoViewModel
import com.tpstream.player.data.VideoRepository
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding
import com.tpstream.player.data.source.local.DownloadStatus
import com.tpstream.player.ui.Util.getRendererIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TpStreamPlayerFragment : Fragment(), DownloadCallback.Listener {
    private val _playbackStateListener: Player.Listener = PlayerListener()
    private lateinit var player: TpStreamPlayerImpl
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    private val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    private var initializationListener: InitializationListener? = null
    private var selectedResolution = ResolutionOptions.AUTO
    private lateinit var fullScreenDialog: Dialog
    private var isFullScreen = false
    private lateinit var orientationEventListener: OrientationListener
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var downloadButton : ImageButton
    private lateinit var resolutionButton : ImageButton
    private var downloadState : DownloadStatus? = null
    private var startPosition : Long = -1L
    private var drmLicenseRetries = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeFullScreenDialog()
        initializeViewModel()
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
        registerResolutionChangeListener()
        registerFullScreenListener()
        registerDownloadListener ()
        DownloadCallback.invoke().callback = this
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
        hideSystemUi()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            storeCurrentPlayTime()
            player?.release()
        }
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            storeCurrentPlayTime()
            player?.release()
        }
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disableAutoFullScreenOnRotate()
    }

    private fun initializeFullScreenDialog() {
        fullScreenDialog =
            object : Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                override fun onBackPressed() {
                    super.onBackPressed()
                    exitFullScreen()
                }
            }
    }

    private fun initializeViewModel() {
        videoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VideoViewModel(VideoRepository(requireContext())) as T
            }
        }).get(VideoViewModel::class.java)
    }

    private fun registerResolutionChangeListener() {
        resolutionButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_resolution)
        resolutionButton.setOnClickListener {
            onResolutionButtonClick()
        }
    }

    private fun onResolutionButtonClick() {
        if (downloadState == DownloadStatus.COMPLETE) {
            Toast.makeText(requireContext(), "Quality Unavailable", Toast.LENGTH_SHORT).show()
        } else {
            val simpleVideoResolutionSelector = initializeVideoResolutionSelectionSheets()
            simpleVideoResolutionSelector.show(
                requireActivity().supportFragmentManager,
                SimpleVideoResolutionSelectionSheet.TAG
            )
        }
    }

    private fun registerFullScreenListener () {
        viewBinding.videoView.findViewById<ImageButton>(R.id.fullscreen).setOnClickListener {
            if(isFullScreen) {
                exitFullScreen()
            } else {
                showFullScreen()
            }
        }
    }

    private fun registerDownloadListener () {
        downloadButton = viewBinding.videoView.findViewById(R.id.exo_download)
        downloadButton.setOnClickListener {
            onDownloadButtonClick()
        }
    }

    private fun onDownloadButtonClick() {
        when (downloadState) {
            DownloadStatus.COMPLETE -> {
                Toast.makeText(requireContext(), "Download complete", Toast.LENGTH_SHORT).show()
            }
            DownloadStatus.DOWNLOADING -> {
                Toast.makeText(requireContext(), "Downloading", Toast.LENGTH_SHORT).show()
            }
            else -> {
                EncryptionKeyRepository(requireContext()).fetchAndStore(
                    player?.params!!,
                    player?.video?.url!!
                )
                val downloadResolutionSelectionSheet = DownloadResolutionSelectionSheet(
                    player!!,
                    player!!.getTrackSelectionParameters(),
                )
                downloadResolutionSelectionSheet.show(
                    requireActivity().supportFragmentManager,
                    "DownloadSelectionSheet"
                )
                downloadResolutionSelectionSheet.setOnSubmitListener { downloadRequest, video ->
                    DownloadTask(requireContext()).start(downloadRequest)
                    video?.videoId = player?.params?.videoId!!
                    ImageSaver(requireContext()).save(
                        video?.thumbnail!!,
                        video.videoId
                    )
                    videoViewModel.insert(video)
                }
            }
        }
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

    private fun initializeVideoResolutionSelectionSheets(): SimpleVideoResolutionSelectionSheet {
        val simpleVideoResolutionSelector =
            SimpleVideoResolutionSelectionSheet(player!!, selectedResolution)
        val advancedVideoResolutionSelector =
            AdvancedResolutionSelectionSheet(player!!, player!!.getTrackSelectionParameters())
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
        player?.setTrackSelectionParameters(parameters)
    }

    private fun onAdvancedVideoResolutionSelection(advancedVideoResolutionSelector: AdvancedResolutionSelectionSheet) =
        DialogInterface.OnClickListener { p0, p1 ->
            val mappedTrackInfo = (player?.getTrackSelector() as DefaultTrackSelector).currentMappedTrackInfo
            mappedTrackInfo?.let {
            val rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
            if (advancedVideoResolutionSelector.overrides.isNotEmpty()) {
                val params = TrackSelectionParameters.Builder(requireContext())
                    .clearOverridesOfType(rendererIndex)
                    .addOverride(advancedVideoResolutionSelector.overrides.values.elementAt(0))
                    .build()
                player?.setTrackSelectionParameters(params)
                }
            }
        }

    private fun exitFullScreen() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        (viewBinding.videoView.parent as ViewGroup).removeView(viewBinding.videoView)
        viewBinding.mainFrameLayout.addView(viewBinding.videoView)
        viewBinding.videoView.findViewById<ImageButton>(R.id.fullscreen).setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_baseline_fullscreen_24
        ));
        fullScreenDialog.dismiss()
        isFullScreen = false
    }

    private fun showFullScreen() {
        (viewBinding.videoView.parent as ViewGroup).removeView(viewBinding.videoView)
        fullScreenDialog.addContentView(viewBinding.videoView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        viewBinding.videoView.findViewById<ImageButton>(R.id.fullscreen).setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_baseline_fullscreen_exit_24
        ));
        fullScreenDialog.show()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        isFullScreen = true
    }

    private fun initializePlayer() {
        player = TpStreamPlayerImpl(requireContext())
        player.setTpStreamPlayerImplCallBack(tpStreamPlayerImplCallBack)
        viewBinding.videoView.player = player.exoPlayer
        player.exoPlayer.addListener(_playbackStateListener)
        this.initializationListener?.onInitializationSuccess(player!!)
    }

    fun setOnInitializationListener(listener: InitializationListener) {
        this.initializationListener = listener
    }

    @Deprecated("Deprecated",ReplaceWith("TpStreamPlayer.load()"),DeprecationLevel.ERROR)
    fun load(parameters: TpInitParams) {
        if (player == null) {
            throw Exception("Player is not initialized yet. `load` method should be called onInitializationSuccess")
        }
        player?.load(parameters)
    }

    override fun onDownloadsSuccess(videoId:String?) {
        if (videoId == player?.params?.videoId){
            reloadVideo()
        }
    }

    private fun reloadVideo(){
        val currentPosition = player?.getCurrentTime()
        val tpImp = player as TpStreamPlayerImpl
        tpImp.playVideo(player?.video?.url!!,currentPosition!!)
    }

    private fun storeCurrentPlayTime(){
        startPosition = player?.getCurrentTime()?: -1L
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

    private inner class PlayerListener : Player.Listener, DRMLicenseFetchCallback {
        private val TAG = "PlayerListener"

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY) {
                viewBinding.errorMessage.visibility = View.GONE
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            SentryLogger.logPlaybackException(error, player?.params)
            if (isDRMException(error.cause!!)) {
                fetchDRMLicence()
            } else {
                showErrorMessage("Error occurred while playing video. \\n ${error.errorCode} ${error.errorCodeName}")
            }
        }

        private fun fetchDRMLicence(){
            if (!InternetConnectivityChecker.isNetworkAvailable(requireContext())) {
                showErrorMessage(getString(R.string.no_internet_to_sync_license))
                return
            }
            val url = player?.video?.url
            val downloadTask = DownloadTask(requireContext())
            drmLicenseRetries += 1
            if (drmLicenseRetries < 2 && downloadTask.isDownloaded(url!!)) {
                OfflineDRMLicenseHelper.renewLicense(url, player?.params!!, activity!!, this)
                showErrorMessage(getString(R.string.syncing_video))
            } else {
                showErrorMessage(getString(R.string.license_request_failed))
            }
        }

        override fun onLicenseFetchSuccess(keySetId: ByteArray) {
            CoroutineScope(Dispatchers.Main).launch {
                reloadVideo()
                drmLicenseRetries = 0
            }
        }

        override fun onLicenseFetchFailure() {
            showErrorMessage(getString(R.string.license_error))
        }

        private fun isDRMException(cause: Throwable): Boolean {
            return cause is DrmSession.DrmSessionException || cause is MediaCodec.CryptoException || cause is MediaDrmCallbackException
        }

    }

    private fun showErrorMessage(message: String) {
        viewBinding.errorMessage.visibility = View.VISIBLE
        viewBinding.errorMessage.text = message
    }

    private class PlayerAnalyticsListener : AnalyticsListener {
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

    private val tpStreamPlayerImplCallBack = object :TpStreamPlayerImplCallBack{

        private val seekBar get() = viewBinding.videoView.findViewById<DefaultTimeBar>(androidx.media3.ui.R.id.exo_progress)
        private var seekBarListener: TimeBar.OnScrubListener? = null

        override fun updateDownloadButton(showDownloadButton: Boolean, videoId: String) {
            requireActivity().runOnUiThread {
                if (showDownloadButton) {
                    downloadButton.isVisible = true
                    updateDownloadButtonImage(videoId)
                }
            }
        }

        override fun onPlaybackError(parameters: TpInitParams, exception: TPException) {
            requireActivity().runOnUiThread{
                showErrorMessage("\"Error Occurred while playing video. Error code ${exception.errorMessage}.\\n ID: ${parameters.videoId}\"")
                SentryLogger.logAPIException(exception,parameters)
            }
        }

        override fun onPlayerPrepare() {
            if (player != null && startPosition != -1L) {
                player.seekTo(startPosition)
                player.pause()
            }
        }

        override fun onSeekBarDisable(message: String) {
            if (seekBarListener != null) return
            seekBarListener = object : TimeBar.OnScrubListener {
                override fun onScrubStart(timeBar: TimeBar, position: Long) {
                    timeBar.setEnabled(false)
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
                }

                override fun onScrubMove(timeBar: TimeBar, position: Long) {}
                override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {}
            }
            seekBar.addListener(seekBarListener!!)
        }

        override fun onSeekBarEnable() {
            if (seekBarListener != null) {
                seekBar.removeListener(seekBarListener!!)
            }
        }

    }

    private fun updateDownloadButtonImage(videoId: String){
        videoViewModel.get(videoId).observe(viewLifecycleOwner) { offlineVideoInfo ->
            downloadState = when (offlineVideoInfo?.downloadState) {
                DownloadStatus.DOWNLOADING ->{
                    downloadButton.setImageResource(R.drawable.ic_baseline_downloading_24)
                    DownloadStatus.DOWNLOADING
                }
                DownloadStatus.COMPLETE ->{
                    downloadButton.setImageResource(R.drawable.ic_baseline_file_download_done_24)
                    DownloadStatus.COMPLETE
                }
                else -> {
                    downloadButton.setImageResource(R.drawable.ic_baseline_download_for_offline_24)
                    null
                }
            }
        }
    }
}

interface InitializationListener {
    fun onInitializationSuccess(player: TpStreamPlayer)
}