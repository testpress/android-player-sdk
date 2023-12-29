package com.tpstream.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.common.collect.ImmutableList
import com.tpstream.player.data.Video
import com.tpstream.player.data.VideoRepository
import com.tpstream.player.data.source.network.VideoNetworkDataSource
import com.tpstream.player.offline.DownloadTask
import com.tpstream.player.offline.VideoDownload
import com.tpstream.player.offline.VideoDownloadManager
import java.util.concurrent.TimeUnit
import kotlin.math.abs

public interface TpStreamPlayer {
    object PLAYBACK_STATE {
        val STATE_IDLE = 1
        val STATE_BUFFERING = 2
        val STATE_READY = 3
        val STATE_ENDED = 4
    }

    fun getPlaybackState(): Int
    fun getCurrentTime(): Long
    fun getBufferedTime(): Long
    fun setPlaybackSpeed(speed: Float)
    fun seekTo(seconds: Long)
    fun getVideoFormat(): Format?
    fun getCurrentTrackGroups(): ImmutableList<TracksGroup>
    fun setMaxVideoSize(maxVideoWidth: Int, maxVideoHeight: Int)
    fun getDuration(): Long
    fun setListener(listener: TPStreamPlayerListener?)
    fun getMaxResolution(): Int?
    fun setMaxResolution(resolution: Int)
    fun play()
    fun pause()
    fun load(parameters: TpInitParams)
    fun release()
    fun getPlayBackSpeed(): Float
    fun getPlayWhenReady(): Boolean
    fun setPlayWhenReady(playWhenReady: Boolean)

    class Builder(private val context: Context) {
        fun build(): TpStreamPlayer {
            return TpStreamPlayerImpl(context)
        }
    }
}

internal class TpStreamPlayerImpl(val context: Context) : TpStreamPlayer {
    lateinit var params: TpInitParams
    var video: Video? = null
    var _listener: TPStreamPlayerListener? = null
    lateinit var exoPlayer: ExoPlayer
    private val exoPlayerListener:ExoPlayerListenerWrapper = ExoPlayerListenerWrapper(this)
    private var videoRepository: VideoRepository = VideoRepository(context)
    private var tpStreamPlayerImplCallBack : TpStreamPlayerImplCallBack? = null
    private var loadCompleteListener : LoadCompleteListener? = null
    private var markerListener: MarkerListener? = null
    var maximumResolution: Int? = null

    init {
        initializeExoplayer()
    }

    private fun initializeExoplayer() {
        exoPlayer = ExoPlayerBuilder(context)
            .setSeekForwardIncrementMs(context.resources.getString(R.string.tp_streams_player_seek_forward_increment_ms).toLong())
            .setSeekBackIncrementMs(context.resources.getString(R.string.tp_streams_player_seek_back_increment_ms).toLong())
            .build()
            .also { exoPlayer ->
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
            }
    }

    override fun load(parameters: TpInitParams) {
        params = parameters
        exoPlayer.playWhenReady = parameters.autoPlay?:true
        videoRepository.getVideo(parameters, object : VideoNetworkDataSource.TPResponse<Video> {
            override fun onSuccess(result: Video) {
                video = result
                playVideoInUIThread(result.url, parameters.startPositionInMilliSecs)
                loadCompleteListener?.onComplete()
            }

            override fun onFailure(exception: TPException) {
                tpStreamPlayerImplCallBack?.onPlaybackError(parameters,exception)
            }
        })
    }

    fun addMarker(timesInMs: Long, deleteAfterDelivery: Boolean) {
        Handler(Looper.getMainLooper()).post {
            exoPlayer.createMessage { _, _ ->
                markerListener?.onMarkerCall(timesInMs)
                _listener?.onMarkerCallback(TimeUnit.MILLISECONDS.toSeconds(timesInMs))
            }.setPosition(timesInMs)
                .setLooper(Looper.getMainLooper())
                .setDeleteAfterDelivery(deleteAfterDelivery)
                .send()
        }
    }

    private fun playVideoInUIThread(url: String,startPosition: Long = 0) {
        Handler(Looper.getMainLooper()).post {
            playVideo(url, startPosition)
        }
    }

    internal fun playVideo(url: String,startPosition: Long = 0) {
        exoPlayer.playWhenReady = params.autoPlay?: true
        exoPlayer.setMediaSource(getMediaSourceFactory().createMediaSource(getMediaItem(url)))
        exoPlayer.seekTo(startPosition)
        exoPlayer.prepare()
        tpStreamPlayerImplCallBack?.onPlayerPrepare()
    }

    private fun getMediaSourceFactory(): MediaSourceFactory {
        return DefaultMediaSourceFactory(context)
            .setDataSourceFactory(VideoDownloadManager(context).build(params))
    }

    private fun getMediaItem(url: String): MediaItem {
        val downloadRequest: DownloadRequest? = VideoDownload.getDownloadRequest(url, context)
        if (DownloadTask(context).isDownloaded(url) && downloadRequest != null) {
            return buildDownloadedMediaItem(downloadRequest)
        }
        return buildMediaItem(url)
    }

    private fun buildMediaItem(url: String): MediaItem {
        val drmLicenseURL = TPStreamsSDK.constructDRMLicenseUrl(params.videoId, params.accessToken)
        return MediaItemBuilder()
            .setUri(url)
            .setDrmConfiguration(
                DrmConfigurationBuilder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .setLicenseUri(drmLicenseURL)
                    .build()
            ).build()
    }

    private fun buildDownloadedMediaItem(downloadRequest: DownloadRequest):MediaItem{
        val builder = MediaItemBuilder()
        builder
            .setMediaId(downloadRequest.id)
            .setUri(downloadRequest.uri)
            .setCustomCacheKey(downloadRequest.customCacheKey)
            .setMimeType(downloadRequest.mimeType)
            .setStreamKeys(downloadRequest.streamKeys)
            .setDrmConfiguration(
                DrmConfigurationBuilder(C.WIDEVINE_UUID)
                    .setKeySetId(downloadRequest.keySetId)
                    .build()
            )
        return builder.build()
    }

    override fun getPlayWhenReady() = exoPlayer.playWhenReady

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady
    }

    fun getTrackSelectionParameters(): TrackSelectionParameters = exoPlayer.trackSelectionParameters

    fun setTrackSelectionParameters(parameters: TrackSelectionParameters){
        exoPlayer.trackSelectionParameters = parameters
    }

    fun getTrackSelector(): TrackSelector? = exoPlayer.trackSelector

    override fun release() {
        exoPlayer.release()
    }

    override fun getPlayBackSpeed() = exoPlayer.playbackParameters.speed

    override fun getPlaybackState(): Int = exoPlayer.playbackState

    override fun getCurrentTime(): Long = exoPlayer.currentPosition

    override fun getBufferedTime(): Long = exoPlayer.bufferedPosition

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    override fun seekTo(seconds: Long) {
        exoPlayer.seekTo(seconds)
    }

    override fun getVideoFormat(): Format? = exoPlayer.videoFormat

    override fun getCurrentTrackGroups(): ImmutableList<TracksGroup> = exoPlayer.currentTracks.groups

    override fun setMaxVideoSize(maxVideoWidth: Int, maxVideoHeight: Int) {
        val trackSelectionParameters = TrackSelectionParametersBuilder(context)
            .setMaxVideoSize(maxVideoWidth,maxVideoHeight)
            .build()
        setTrackSelectionParameters(trackSelectionParameters)
    }

    override fun getDuration(): Long = exoPlayer.duration

    override fun setListener(listener: TPStreamPlayerListener?) {
        this._listener = listener
        this.exoPlayerListener.listener = listener
        if (listener != null) {
            exoPlayer.addListener(exoPlayerListener)
        } else {
            exoPlayer.removeListener(exoPlayerListener)
        }
    }

    override fun getMaxResolution(): Int? {
        return maximumResolution
    }

    override fun setMaxResolution(resolution: Int) {
        maximumResolution = resolution
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    fun setTpStreamPlayerImplCallBack(tpStreamPlayerImplCallBack: TpStreamPlayerImplCallBack){
        this.tpStreamPlayerImplCallBack = tpStreamPlayerImplCallBack
    }

    fun setLoadCompleteListener(listener: LoadCompleteListener) {
        loadCompleteListener = listener
    }

    fun setMarkerListener(listener: MarkerListener) {
        markerListener = listener
    }

    fun setAutoResolution() {
        if (maximumResolution == null) {
            setTrackSelectionParameters(
                TrackSelectionParametersBuilder(context).build()
            )
        } else {
            val trackIndices = mutableListOf<Int>()
            for (tracksGroup in getVideoTracksGroup()) {
                val mediaTrackGroup = tracksGroup.mediaTrackGroup
                for (resolution in 0 until mediaTrackGroup.length) {
                    if (maximumResolution!! >= mediaTrackGroup.getFormat(resolution).height) {
                        trackIndices.add(resolution)
                    }
                }
                setTrackSelectionParameters(
                    TrackSelectionParametersBuilder(context)
                        .addOverride(TrackSelectionOverride(mediaTrackGroup, trackIndices))
                        .build()
                )
            }
        }
    }

    fun setLowResolution() {
        setTrackSelectionParameters(
            TrackSelectionParametersBuilder(context).setForceLowestBitrate(true).build()
        )
    }

    fun setHighResolution() {
        if (maximumResolution == null) {
            setTrackSelectionParameters(
                TrackSelectionParametersBuilder(context).setForceHighestSupportedBitrate(true)
                    .build()
            )
        } else {
            for (tracksGroup in getVideoTracksGroup()) {
                val mediaTrackGroup = tracksGroup.mediaTrackGroup
                val selectedResolutionIndex = findClosestResolutionIndex(mediaTrackGroup)
                if (selectedResolutionIndex != -1) {
                    val trackSelectorParameters = TrackSelectionParametersBuilder(context)
                        .addOverride(TrackSelectionOverride(mediaTrackGroup, selectedResolutionIndex))
                        .build()
                    setTrackSelectionParameters(trackSelectorParameters)
                }
            }
        }
    }

    private fun findClosestResolutionIndex(mediaTrackGroup: TrackGroup): Int {
        val totalResolutionCount = mediaTrackGroup.length
        val targetResolution = maximumResolution!!
        var minResolutionDifference = Int.MAX_VALUE
        var selectedResolution = -1
        for (resolution in 0 until totalResolutionCount) {
            val currentResolution = mediaTrackGroup.getFormat(resolution).height
            val resolutionDifference = abs(currentResolution - targetResolution)
            if (resolutionDifference < minResolutionDifference) {
                minResolutionDifference = resolutionDifference
                selectedResolution = resolution
            }
        }
        return selectedResolution
    }

    private fun getVideoTracksGroup(): List<TracksGroup> {
        return getCurrentTrackGroups().filter { it.type == C.TRACK_TYPE_VIDEO }
    }
}

internal interface TpStreamPlayerImplCallBack {
    fun onPlaybackError(parameters: TpInitParams,exception: TPException)
    fun onPlayerPrepare()
}

internal fun interface LoadCompleteListener {
    fun onComplete()
}

internal fun interface MarkerListener {
    fun onMarkerCall(timeInMs: Long)
}