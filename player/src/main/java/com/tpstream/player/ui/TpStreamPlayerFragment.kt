package com.tpstream.player.ui

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.media.MediaCodec
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.tpstream.player.*
import com.tpstream.player.Util
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding
import com.tpstream.player.constants.getErrorMessage
import com.tpstream.player.constants.toError
import com.tpstream.player.offline.*
import com.tpstream.player.offline.VideoDownload.getDownload
import com.tpstream.player.util.OrientationListener
import com.tpstream.player.util.SentryLogger
import com.tpstream.player.util.SystemBars
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TpStreamPlayerFragment : Fragment(), DownloadCallback.Listener {
    private val _playbackStateListener: PlayerListener = InternalPlayerListener()
    private lateinit var player: TpStreamPlayerImpl
    private var _viewBinding: FragmentTpStreamPlayerBinding? = null
    private val viewBinding get() = _viewBinding!!
    private val TAG = "TpStreamPlayerFragment"
    private var initializationListener: InitializationListener? = null
    private lateinit var fullScreenDialog: Dialog
    private var isFullScreen = false
    private lateinit var orientationEventListener: OrientationListener
    private var startPosition : Long = -1L
    private var drmLicenseErrorCount = 0
    lateinit var tpStreamPlayerView: TPStreamPlayerView
    private var preferredFullscreenExitOrientation  = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    private var assetApiCallCount = 0
    private var drmLicenseApiCallCount = 0
    private var offlineLicenseApiCallCount = 0
    private var enableSecureView = false
    private var useSoftwareDecoder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeFullScreenDialog()
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
        tpStreamPlayerView = viewBinding.tpStreamPlayerView
        if (enableSecureView) {
            tpStreamPlayerView.setSecureSurfaceView()
        }
        tpStreamPlayerView.setTPStreamPlayerViewCallBack(tPStreamPlayerViewCallBack)
        registerFullScreenListener()
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
        // Ensure that the full-screen dialog is dismissed when the Activity is destroyed
        // to prevent a WindowLeaked exception. This is necessary to handle scenarios
        // where the dialog is still visible when the Activity is no longer in the foreground.
        if (fullScreenDialog.isShowing)
            fullScreenDialog.onBackPressed()
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
                    exitFullScreen()
                    super.onBackPressed()
                }
            }
    }

    private fun registerFullScreenListener () {
        tpStreamPlayerView.findViewById<ImageButton>(R.id.fullscreen).setOnClickListener {
            if(isFullScreen) {
                exitFullScreen()
            } else {
                showFullScreen()
            }
        }
    }

    fun setPreferredFullscreenExitOrientation(orientation :Int){
        preferredFullscreenExitOrientation  = orientation
    }

    fun exitFullScreen() {
        SystemBars.setVisibility(fullScreenDialog.window, false)
        
        requireActivity().requestedOrientation = preferredFullscreenExitOrientation
        (tpStreamPlayerView.parent as ViewGroup).removeView(tpStreamPlayerView)
        viewBinding.mainFrameLayout.addView(tpStreamPlayerView)
        tpStreamPlayerView.findViewById<ImageButton>(R.id.fullscreen).setImageDrawable(
            ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_baseline_fullscreen_24
        ));
        fullScreenDialog.dismiss()
        isFullScreen = false
        player?._listener?.onFullScreenChanged(false)
    }

    fun showFullScreen() {
        (tpStreamPlayerView.parent as ViewGroup).removeView(tpStreamPlayerView)
        fullScreenDialog.addContentView(tpStreamPlayerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        tpStreamPlayerView.findViewById<ImageButton>(R.id.fullscreen).setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_baseline_fullscreen_exit_24
        ));
        fullScreenDialog.show()
        SystemBars.setVisibility(fullScreenDialog.window, true)
        
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        isFullScreen = true
        player?._listener?.onFullScreenChanged(true)
    }

    private fun initializePlayer() {
        player = TpStreamPlayerImpl(requireContext())
        player.init(useSoftwareDecoder)
        player.setTpStreamPlayerImplCallBack(tpStreamPlayerImplCallBack)
        tpStreamPlayerView.setPlayer(player)
        player.exoPlayer.addListener(_playbackStateListener)
        this.initializationListener?.onInitializationSuccess(player!!)
    }

    fun setOnInitializationListener(listener: InitializationListener) {
        this.initializationListener = listener
    }

    @Deprecated("Deprecated",ReplaceWith("TpStreamPlayer.load()"),DeprecationLevel.ERROR)
    fun load(parameters: TpInitParams, metadata: Map<String, String>? = null) {
        if (player == null) {
            throw Exception("Player is not initialized yet. `load` method should be called onInitializationSuccess")
        }
        player?.load(parameters, metadata)
    }

    fun enableSecureView() {
        enableSecureView = true
    }

    fun useSoftwareDecoder() {
        useSoftwareDecoder = true
    }

    override fun onDownloadsSuccess(videoId: String?) {
        if (!this.isVisible) return
        if (player == null) return
        if (player?.isParamsInitialized() == true && videoId == player?.params?.videoId) {
            reloadVideo()
        }
    }

    private fun reloadVideo(){
        val currentPosition = player?.getCurrentTime()
        val tpImp = player as TpStreamPlayerImpl
        tpImp.playVideo(player?.asset?.video?.url!!,currentPosition!!)
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

    private inner class InternalPlayerListener : PlayerListener, DRMLicenseFetchCallback {
        private val TAG = "PlayerListener"

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY || playbackState == ExoPlayer.STATE_BUFFERING) {
                viewBinding.errorMessage.visibility = View.GONE
                tpStreamPlayerView.hideReplayButton()
                tpStreamPlayerView.showPlayButton()
            }
            if (playbackState == ExoPlayer.STATE_ENDED) {
                tpStreamPlayerView.hidePlayButton()
                tpStreamPlayerView.showReplayButton()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            val errorPlayerId = SentryLogger.generatePlayerIdString()

            if (isDisconnectedLiveStream(error)) {
                player.load(player.params)
                return
            }

            SentryLogger.logPlaybackException(error, player?.params, errorPlayerId)

            when (error.errorCode) {
                PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> handleDrmLicenseAcquisitionFailed(error, errorPlayerId)
                else -> handleOtherErrors(error, errorPlayerId)
            }
        }

        private fun handleDrmLicenseAcquisitionFailed(error: PlaybackException, errorPlayerId: String) {
            drmLicenseApiCallCount++
            player?._listener?.onAccessTokenExpired(player?.params?.videoId!!) { newAccessToken ->
                if (newAccessToken.isNotEmpty() && drmLicenseApiCallCount < 2) {
                    player?.params?.setNewAccessToken(newAccessToken)
                    player?.playVideoInUIThread(player.asset?.video?.url!!, player.getCurrentTime())
                } else {
                    showErrorAndNotifyListener(error, errorPlayerId)
                }
            }
        }

        private fun handleOtherErrors(error: PlaybackException, errorPlayerId: String) {
            if (isDRMException(error.cause!!)) {
                handleDrmException(error, errorPlayerId)
            } else {
                showErrorAndNotifyListener(error, errorPlayerId)
            }
        }

        private fun handleDrmException(error: PlaybackException, errorPlayerId: String) {
            if (isOfflineLicenseExpired()) {
                renewDRMLicence(error)
            } else {
                if (drmLicenseErrorCount < 2) {
                    drmLicenseErrorCount++
                    reloadVideo()
                } else {
                    showErrorAndNotifyListener(error, errorPlayerId)
                }
            }
        }

        private fun showErrorAndNotifyListener(error: PlaybackException, errorPlayerId: String) {
            showErrorMessage(error.getErrorMessage(errorPlayerId))
            player?._listener?.onPlayerError(error.toError())
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            if (!isLoading && player.getPlaybackState() == TpStreamPlayer.PLAYBACK_STATE.STATE_READY) {
                restrictVideoToMaximumResolution()
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
            if (playWhenReady) viewBinding.errorMessage.visibility = View.GONE
        }

        private fun restrictVideoToMaximumResolution() {
            val maxResolution = player.getMaxResolution()
            val videoHeight = player.getVideoFormat()?.height
            if (maxResolution != null && videoHeight != null && videoHeight > maxResolution) {
                player.setAutoResolution()
            }
        }

        private fun renewDRMLicence(error: PlaybackException){
            val errorPlayerId = SentryLogger.generatePlayerIdString()
            if (!InternetConnectivityChecker.isNetworkAvailable(requireContext())) {
                showErrorMessage(getString(R.string.no_internet_to_sync_license))
                return
            }
            player._listener?.onAccessTokenExpired(player.params.videoId!!){ newAccessToken ->
                if (newAccessToken.isNotEmpty()) {
                    player.params.setNewAccessToken(newAccessToken)
                    OfflineDRMLicenseHelper.renewLicense(player.asset?.video?.url!!, player.params, activity!!, this)
                    showErrorMessage(getString(R.string.syncing_video))
                } else {
                    SentryLogger.logPlaybackException(error, player?.params, errorPlayerId)
                    showErrorMessage(getString(R.string.license_error)+"\n Player Id: $errorPlayerId")
                }
            }
        }

        override fun onLicenseFetchSuccess(keySetId: ByteArray) {
            if (!this@TpStreamPlayerFragment.isAdded) return
            OfflineDRMLicenseHelper.replaceKeysInExistingDownloadedVideo(
                player.asset?.video?.url!!,
                requireContext(),
                keySetId
            )
            CoroutineScope(Dispatchers.Main).launch {
                reloadVideo()
            }
        }

        override fun onLicenseFetchFailure(error: DrmSessionException) {
            if (!this@TpStreamPlayerFragment.isAdded) return
            offlineLicenseApiCallCount++
            requireActivity().runOnUiThread{
                player._listener?.onAccessTokenExpired(player.params.videoId!!){ newAccessToken ->
                    if (newAccessToken.isNotEmpty() && offlineLicenseApiCallCount < 2) {
                        player.params.setNewAccessToken(newAccessToken)
                        OfflineDRMLicenseHelper.renewLicense(player.asset?.video?.url!!, player.params, activity!!, this)
                        showErrorMessage(getString(R.string.syncing_video))
                    } else {
                        val errorPlayerId = SentryLogger.generatePlayerIdString()
                        SentryLogger.logDrmSessionException(error, player?.params, errorPlayerId)
                        showErrorMessage(getString(R.string.license_error))
                    }
                }
            }
        }

        private fun isDRMException(cause: Throwable): Boolean {
            return cause is DrmSessionException || cause is MediaCodec.CryptoException || cause is MediaDrmCallbackException
        }

    }

    private fun isOfflineLicenseExpired(): Boolean {
        val download =
            getDownload(player.asset?.getPlaybackURL()!!, requireActivity()) ?: return false
        return OfflineDRMLicenseHelper.isOfflineLicenseExpired(
            player.params,
            requireContext(),
            download.request
        )
    }

    private fun isDisconnectedLiveStream(error: PlaybackException): Boolean {
        player.asset?.liveStream?.let {
            return it.isNotEnded && error.errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED
        }
        return false
    }

    private fun showErrorMessage(message: String) {
        if (!this@TpStreamPlayerFragment.isAdded) return
        viewBinding.errorMessage.visibility = View.VISIBLE
        viewBinding.errorMessage.text = message
    }

    private val tpStreamPlayerImplCallBack = object :TpStreamPlayerImplCallBack{

        override fun onPlaybackError(parameters: TpInitParams, exception: TPException) {
            if (!isAdded) return
            assetApiCallCount++
            requireActivity().runOnUiThread {
                val errorPlayerId = SentryLogger.generatePlayerIdString()
                if (exception.isUnauthenticated()) {
                    player?._listener?.onAccessTokenExpired(parameters.videoId!!) { newAccessToken ->
                        if (newAccessToken.isNotEmpty() && assetApiCallCount < 2) {
                            parameters.setNewAccessToken(newAccessToken)
                            player?.load(parameters)
                        } else {
                            showErrorMessage(exception.getErrorMessage(errorPlayerId))
                            player?._listener?.onPlayerError(exception.toError())
                            SentryLogger.logAPIException(exception, parameters, errorPlayerId)
                        }
                    }
                } else {
                    showErrorMessage(exception.getErrorMessage(errorPlayerId))
                    player?._listener?.onPlayerError(exception.toError())
                    SentryLogger.logAPIException(exception, parameters, errorPlayerId)
                }
            }
        }

        override fun onPlayerPrepare() {
            if (player != null && startPosition != -1L) {
                player.seekTo(startPosition)
                player.pause()
            }
        }
    }

    private val tPStreamPlayerViewCallBack = object : TPStreamPlayerViewCallBack {

        override fun hideErrorView() {
            viewBinding.errorMessage.visibility = View.GONE
        }

        override fun showErrorMessage(message: String) {
            this@TpStreamPlayerFragment.showErrorMessage(message)
        }
    }

}

interface InitializationListener {
    fun onInitializationSuccess(player: TpStreamPlayer)
}