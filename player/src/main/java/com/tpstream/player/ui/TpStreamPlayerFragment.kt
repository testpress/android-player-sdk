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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.tpstream.player.*
import com.tpstream.player.Util
import com.tpstream.player.offline.DRMLicenseFetchCallback
import com.tpstream.player.offline.DownloadCallback
import com.tpstream.player.offline.DownloadTask
import com.tpstream.player.offline.InternetConnectivityChecker
import com.tpstream.player.offline.OfflineDRMLicenseHelper
import com.tpstream.player.util.OrientationListener
import com.tpstream.player.R
import com.tpstream.player.util.SentryLogger
import com.tpstream.player.TpStreamPlayerImpl
import com.tpstream.player.databinding.FragmentTpStreamPlayerBinding
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
    private var drmLicenseRetries = 0
    lateinit var tpStreamPlayerView: TPStreamPlayerView

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

    private fun registerFullScreenListener () {
        tpStreamPlayerView.findViewById<ImageButton>(R.id.fullscreen).setOnClickListener {
            if(isFullScreen) {
                exitFullScreen()
            } else {
                showFullScreen()
            }
        }
    }

    private fun hideSystemUi() {
        activity?.let { activity ->
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            WindowInsetsControllerCompat(activity.window, tpStreamPlayerView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            Log.d(TAG, "hideSystemUi: ")
        }
    }

    private fun exitFullScreen() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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

    private fun showFullScreen() {
        (tpStreamPlayerView.parent as ViewGroup).removeView(tpStreamPlayerView)
        fullScreenDialog.addContentView(tpStreamPlayerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        tpStreamPlayerView.findViewById<ImageButton>(R.id.fullscreen).setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_baseline_fullscreen_exit_24
        ));
        fullScreenDialog.show()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        isFullScreen = true
        player?._listener?.onFullScreenChanged(true)
    }

    private fun initializePlayer() {
        player = TpStreamPlayerImpl(requireContext())
        player.setTpStreamPlayerImplCallBack(tpStreamPlayerImplCallBack)
        tpStreamPlayerView.setPlayer(player)
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

    private inner class InternalPlayerListener : PlayerListener, DRMLicenseFetchCallback {
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
            return cause is DrmSessionException || cause is MediaCodec.CryptoException || cause is MediaDrmCallbackException
        }

    }

    private fun showErrorMessage(message: String) {
        viewBinding.errorMessage.visibility = View.VISIBLE
        viewBinding.errorMessage.text = message
    }

    private val tpStreamPlayerImplCallBack = object :TpStreamPlayerImplCallBack{

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
    }

}

interface InitializationListener {
    fun onInitializationSuccess(player: TpStreamPlayer)
}