package com.tpstream.player.offline

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.tpstream.player.*
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.data.Asset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

internal class VideoDownloadRequestCreationHandler(
    val context: Context,
    private val asset: Asset,
    private val params: TpInitParams
) :
    DownloadHelperCallback, DRMLicenseFetchCallback {
    private val downloadHelper: DownloadHelper
    private val trackSelectionParameters: DefaultTrackSelectorParameters
    var listener: Listener? = null
    private val mediaItem: MediaItem
    private var keySetId: ByteArray? = null

    init {
        val url = asset.video.url
        trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        val drmLicenseURL = TPStreamsSDK.constructOfflineDRMLicenseUrl(params.videoId, params.accessToken)
        mediaItem = MediaItemBuilder()
            .setUri(url)
            .setDrmConfiguration(
                DrmConfigurationBuilder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .setLicenseUri(drmLicenseURL)
                    .build()
            )
            .build()
        downloadHelper = getDownloadHelper()
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(): DownloadHelper {
        val dataSourceFactory = VideoDownloadManager(context).build(params)
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
                OfflineDRMLicenseHelper.fetchLicense(context, params, videoOrAudioData, this)
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
        val name = asset.title
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

    override fun onLicenseFetchFailure(error: DrmSessionException) {
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