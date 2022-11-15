package com.tpstream.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import com.tpstream.player.Util.getRendererIndex
import java.io.IOException

class VideoDownloadRequestCreationHandler(
    val context: Context
):DownloadHelper.Callback {

    private val downloadHelper: DownloadHelper
    private val trackSelectionParameters: DefaultTrackSelector.Parameters
    var listener: Listener? = null
    private val mediaItem:MediaItem
    private var keySetId:ByteArray? = null

    init {
        val url ="https://verandademo-cdn.testpress.in/institute/demoveranda/courses/my-course/videos/transcoded/5b38cef3dd3f48938021c40203749ab3/720p/video.m3u8"
        trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        mediaItem = MediaItem.Builder()
            .setUri(url)
            .setDrmUuid(C.WIDEVINE_UUID)
            .setDrmMultiSession(true)
            .build()
        downloadHelper = getDownloadHelper()
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(): DownloadHelper {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val renderersFactory = DefaultRenderersFactory(context)
        return DownloadHelper.forMediaItem(mediaItem, trackSelectionParameters, renderersFactory, dataSourceFactory, null)
    }

    override fun onPrepared(helper: DownloadHelper) {
//        val videoOrAudioData = getAudioOrVideoInfoWithDrmInitData(helper)
//        val isDRMProtectedVideo = videoOrAudioData != null
//
//
//        listener?.onDownloadRequestHandlerPrepared(
//            downloadHelper.getMappedTrackInfo(0),
//            getRendererIndex(),
//            getTrackSelectionOverrides()
//        )
    }


    override fun onPrepareError(helper: DownloadHelper, e: IOException) {
        listener?.onDownloadRequestHandlerPrepareError(helper, e)
    }



    interface Listener {
        fun onDownloadRequestHandlerPrepared(
            mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
            rendererIndex: Int,
            overrides: List<DefaultTrackSelector.SelectionOverride>
        )

        fun onDownloadRequestHandlerPrepareError(helper: DownloadHelper, e: IOException)
    }
}