package com.tpstream.player

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import com.tpstream.player.VideoPlayerUtil.getLowBitrateTrackIndex
import com.tpstream.player.models.VideoInfo
import java.io.IOException

class VideoDownloadRequestCreationHandler(
    val context: Context,
    private val videoInfo: VideoInfo
) :
    DownloadHelper.Callback {
    private val downloadHelper: DownloadHelper
    private val trackSelectionParameters: DefaultTrackSelector.Parameters
    var listener: Listener? = null
    private val mediaItem: MediaItem
    private var keySetId: ByteArray? = null

    init {
        val url = videoInfo.url
        trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        mediaItem = MediaItem.Builder()
            .setUri(url)
            .build()
        downloadHelper = getDownloadHelper()
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(): DownloadHelper {
        val dataSourceFactory = DefaultDataSource.Factory(context)
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
        listener?.onDownloadRequestHandlerPrepared(
            getMappedTrackInfo(),
            getRendererIndex(),
            getTrackSelectionOverrides()
        )
    }

    private fun getMappedTrackInfo(): MappingTrackSelector.MappedTrackInfo {
        return downloadHelper.getMappedTrackInfo(0)
    }

    private fun getRendererIndex(): Int {
        return VideoPlayerUtil.getRendererIndex(C.TRACK_TYPE_VIDEO, getMappedTrackInfo())
    }

    private fun getTrackSelectionOverrides(): List<DefaultTrackSelector.SelectionOverride> {
        val trackGroups = getMappedTrackInfo().getTrackGroups(getRendererIndex())
        if (trackGroups.length == 0) {
            return emptyList()
        }
        val (lowBandwithTrackIndex, lowBandwithGroupIndex) = getLowBitrateTrackIndex(trackGroups)
        return listOf(
            DefaultTrackSelector.SelectionOverride(
                lowBandwithGroupIndex,
                lowBandwithTrackIndex
            )
        )
    }

    override fun onPrepareError(helper: DownloadHelper, e: IOException) {
        listener?.onDownloadRequestHandlerPrepareError(helper, e)
    }

    fun buildDownloadRequest(overrides: List<TrackSelectionOverride>): DownloadRequest {
        setSelectedTracks(overrides)
        val name = videoInfo.title!!
        return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name)).copyWithKeySetId(keySetId)
    }

    private fun setSelectedTracks(overrides: List<TrackSelectionOverride>) {
        for (index in 0 until downloadHelper.periodCount) {
            downloadHelper.clearTrackSelections(index)
            var builder =
                DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS_WITHOUT_CONTEXT.buildUpon()

            for (i in overrides.indices) {
                builder.addOverride(overrides[i])
                downloadHelper.addTrackSelection(index, builder.build())
            }
        }
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