package com.tpstream.player

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

typealias OnDownloadRequestCreation = (DownloadRequest, VideoInfo) -> Unit

class VideoDownloadRequestCreationHandler(
    val context: Context,
    private val player: TpStreamPlayer? = null,
    private var params: TpInitParams? = null
) :
    DownloadHelper.Callback, DRMLicenseFetchCallback {
    private lateinit var downloadHelper: DownloadHelper
    private lateinit var trackSelectionParameters: DefaultTrackSelector.Parameters
    var listener: Listener? = null
    private var keySetId: ByteArray? = null
    private lateinit var videoInfo:VideoInfo
    private var url: String? = null
    private var onDownloadRequestCreation :OnDownloadRequestCreation? = null

    fun init(): VideoDownloadRequestCreationHandler{
        if (player == null){
            CoroutineScope(Dispatchers.IO).launch {
                getVideoInfo(params!!)
            }
        }
        url = player?.videoInfo?.getPlaybackURL()!!
        params = player.params
        videoInfo = player.videoInfo!!
        trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        downloadHelper = getDownloadHelper()
        downloadHelper.prepare(this)
        return this
    }

    private fun getVideoInfo(params: TpInitParams){
    val url =
        "/api/v2.5/video_info/${params.videoId}/?access_token=${params.accessToken}"
    Network<VideoInfo>(params.orgCode).get(url, object : Network.TPResponse<VideoInfo> {
        override fun onSuccess(result: VideoInfo) {
            videoInfo = result
        }

        override fun onFailure(exception: TPException) {
            Toast.makeText(context,"Download Failed",Toast.LENGTH_SHORT).show()
        }
    })
    }

    private fun getDownloadHelper(): DownloadHelper {
        val sessionManager = DefaultDrmSessionManager.Builder()
            .build(CustomHttpDrmMediaCallback(context, params!!))
        sessionManager.setMode(DefaultDrmSessionManager.MODE_DOWNLOAD, null)
        val dataSourceFactory = VideoDownloadManager(context).build(params!!)
        val renderersFactory = DefaultRenderersFactory(context)
        return DownloadHelper.forMediaItem(
            getMediaItem(url!!),
            trackSelectionParameters,
            renderersFactory,
            dataSourceFactory,
            sessionManager
        )
    }

    private fun getMediaItem(url: String):MediaItem{
        return MediaItem.Builder()
            .setUri(url)
            .setDrmConfiguration(
                DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .build()
            )
            .build()
    }

    override fun onPrepared(helper: DownloadHelper) {
        val videoOrAudioData = VideoPlayerUtil.getAudioOrVideoInfoWithDrmInitData(helper)
        val isDRMProtectedVideo = videoOrAudioData != null
        if (isDRMProtectedVideo) {
            if (hasDRMSchemaData(videoOrAudioData!!.drmInitData!!)) {
                OfflineDRMLicenseHelper.fetchLicense(context, params!!, downloadHelper, this)
            } else {
                Toast.makeText(
                    context,
                    "Error in downloading video",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }
        listener?.onDownloadRequestHandlerPrepared(true, helper)
    }

    private fun hasDRMSchemaData(drmInitData: DrmInitData): Boolean {
        for (i in 0 until drmInitData.schemeDataCount) {
            if (drmInitData[i].hasData()) {
                return true
            }
        }
        return false
    }


    override fun onPrepareError(helper: DownloadHelper, e: IOException) {
        listener?.onDownloadRequestHandlerPrepareError(helper, e)
    }

    fun buildDownloadRequest(overrides: MutableMap<TrackGroup, TrackSelectionOverride>): DownloadRequest {
        setSelectedTracks(overrides)
        val name = videoInfo.title!!
        return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name)).copyWithKeySetId(keySetId)
    }

    private fun setSelectedTracks(overrides: MutableMap<TrackGroup, TrackSelectionOverride>) {
        val builder = trackSelectionParameters.buildUpon()
        builder.clearOverrides()
        builder.addOverride(overrides.values.first())

        for (index in 0 until downloadHelper.periodCount) {
            downloadHelper.clearTrackSelections(index)
            downloadHelper.addTrackSelection(index, builder.build())
        }
    }

    override fun onLicenseFetchSuccess(keySetId: ByteArray) {
        this.keySetId = keySetId
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("TAG", "onLicenseFetchSuccess: Success")
            listener?.onDownloadRequestHandlerPrepared(true, downloadHelper)
        }
    }

    override fun onLicenseFetchFailure() {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                "Error in starting video download (License fetch error)",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    interface Listener {
        fun onDownloadRequestHandlerPrepared(isPrepared: Boolean, downloadHelper: DownloadHelper)

        fun onDownloadRequestHandlerPrepareError(downloadHelper: DownloadHelper, e: IOException)
    }

    fun setOnDownloadRequestCreation(listener: OnDownloadRequestCreation) {
        onDownloadRequestCreation = listener
    }
}