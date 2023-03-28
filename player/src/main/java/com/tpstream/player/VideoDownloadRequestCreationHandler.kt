package com.tpstream.player

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.offline.DRMLicenseFetchCallback
import com.tpstream.player.offline.OFFLINE_DRM_LICENSE_PARAMS
import com.tpstream.player.offline.OfflineDRMLicenseHelper
import com.tpstream.player.offline.VideoDownloadManager
import com.tpstream.player.offline.VideoPlayerUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

internal class VideoDownloadRequestCreationHandler(
    val context: Context,
    private val player: TpStreamPlayerImpl
) :
    DownloadHelper.Callback, DRMLicenseFetchCallback {
    private val downloadHelper: DownloadHelper
    private val trackSelectionParameters: DefaultTrackSelector.Parameters
    var listener: Listener? = null
    private val mediaItem: MediaItem
    private var keySetId: ByteArray? = null

    init {
        val url = player.video?.url!!
        trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        val drmLicenseURL = "${BuildConfig.DRM_LICENSE_URL.format(player.params.orgCode,player.params.videoId,player.params.accessToken)}$OFFLINE_DRM_LICENSE_PARAMS"
        mediaItem = MediaItem.Builder()
            .setUri(url)
            .setDrmConfiguration(
                DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .setLicenseUri(drmLicenseURL)
                    .build()
            )
            .build()
        downloadHelper = getDownloadHelper()
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(): DownloadHelper {
        val dataSourceFactory = VideoDownloadManager(context).build(player.params)
        val renderersFactory = DefaultRenderersFactory(context)
        return DownloadHelper.forMediaItem(
            mediaItem,
            trackSelectionParameters,
            renderersFactory,
            dataSourceFactory,
            null
        )
    }

    override fun onPrepared(helper: DownloadHelper) {
        val videoOrAudioData = VideoPlayerUtil.getAudioOrVideoInfoWithDrmInitData(helper)
        val isDRMProtectedVideo = videoOrAudioData != null
        if (isDRMProtectedVideo) {
            if (hasDRMSchemaData(videoOrAudioData!!.drmInitData!!)) {
                OfflineDRMLicenseHelper.fetchLicense(context, player.params, downloadHelper, this)
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
        val name = player.video?.title!!
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
}