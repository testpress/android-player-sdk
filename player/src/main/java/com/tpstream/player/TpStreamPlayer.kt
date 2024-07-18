package com.tpstream.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.google.common.collect.ImmutableList
import com.tpstream.player.data.Asset
import com.tpstream.player.data.AssetRepository
import com.tpstream.player.offline.DownloadTask
import com.tpstream.player.offline.VideoDownload
import com.tpstream.player.offline.VideoDownloadManager
import com.tpstream.player.util.NetworkClient
import java.lang.Exception
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
    fun load(parameters: TpInitParams, metadata: Map<String, String>? = null)
    fun release()
    fun getPlayBackSpeed(): Float
    fun getPlayWhenReady(): Boolean
    fun setPlayWhenReady(playWhenReady: Boolean)
    fun getExoplayer(): ExoPlayer

    class Builder(private val context: Context) {
        fun build(): TpStreamPlayer {
            return TpStreamPlayerImpl(context)
        }
    }
}

@OptIn(UnstableApi::class)
internal class TpStreamPlayerImpl(val context: Context) : TpStreamPlayer {
    lateinit var params: TpInitParams
    var asset: Asset? = null
    var _listener: TPStreamPlayerListener? = null
    lateinit var exoPlayer: ExoPlayer
    private val exoPlayerListener:ExoPlayerListenerWrapper = ExoPlayerListenerWrapper(this)
    private var assetRepository: AssetRepository = AssetRepository(context)
    private var tpStreamPlayerImplCallBack : TpStreamPlayerImplCallBack? = null
    private var loadCompleteListener : LoadCompleteListener? = null
    private var markerListener: MarkerListener? = null
    var maximumResolution: Int? = null

    init {
        initializeExoplayer()
    }

    private fun initializeExoplayer() {
        val rf = DefaultRenderersFactory(context)
        rf.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        exoPlayer = ExoPlayerBuilder(context)
            .setSeekForwardIncrementMs(context.resources.getString(R.string.tp_streams_player_seek_forward_increment_ms).toLong())
            .setSeekBackIncrementMs(context.resources.getString(R.string.tp_streams_player_seek_back_increment_ms).toLong())
            .setRenderersFactory(rf)
            .build()
            .also { exoPlayer ->
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
            }
    }

    fun isParamsInitialized(): Boolean {
        return this::params.isInitialized
    }

    override fun load(parameters: TpInitParams, metadata: Map<String, String>?) {
        params = parameters
        exoPlayer.playWhenReady = parameters.autoPlay
        if (parameters.videoId == "exoplayer_drm"){
            playVideoInUIThread("https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd")
            return
        }
        if (parameters.videoId == "exoplayer_non_drm"){
            playVideoInUIThread("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8")
            return
        }
        assetRepository.getAsset(parameters, object : NetworkClient.TPResponse<Asset> {
            override fun onSuccess(result: Asset) {
                asset = result
                asset?.metadata = metadata
                asset!!.getPlaybackURL()?.let {
                    playVideoInUIThread(it, parameters.startPositionInMilliSecs)
                }
                loadCompleteListener?.onComplete(result)
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
            .setSubtitleConfigurations(buildSubTitleConfiguration())
            .setDrmConfiguration(
                DrmConfigurationBuilder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .setLicenseUri(drmLicenseURL)
                    .build()
            ).build()
    }

    private fun buildSubTitleConfiguration(): List<SubtitleConfiguration> {
        return asset?.video?.tracks?.filter { it.type == "Subtitle" }?.map { track ->
            SubtitleConfigurationBuilder(Uri.parse(track.url))
                .setLanguage(track.language)
                .setMimeType(MimeTypes.TEXT_VTT)
                .build()
        } ?: emptyList()
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

    override fun getExoplayer(): ExoPlayer {
        return exoPlayer
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
            exoPlayer.addAnalyticsListener(object : AnalyticsListener {
                val TAG = "TAG"
                override fun onVideoCodecError(
                    eventTime: AnalyticsListener.EventTime,
                    videoCodecError: Exception
                ) {
                    super.onVideoCodecError(eventTime, videoCodecError)
                    Log.d(TAG, "onVideoCodecError: ")
                }

                override fun onVideoDecoderInitialized(
                    eventTime: AnalyticsListener.EventTime,
                    decoderName: String,
                    initializedTimestampMs: Long,
                    initializationDurationMs: Long
                ) {
                    super.onVideoDecoderInitialized(
                        eventTime,
                        decoderName,
                        initializedTimestampMs,
                        initializationDurationMs
                    )
                    Log.d(TAG, "onVideoDecoderInitialized: ")
                }

                override fun onVideoDecoderReleased(
                    eventTime: AnalyticsListener.EventTime,
                    decoderName: String
                ) {
                    super.onVideoDecoderReleased(eventTime, decoderName)
                    Log.d(TAG, "onVideoDecoderReleased: ")
                }

                override fun onVideoDisabled(
                    eventTime: AnalyticsListener.EventTime,
                    decoderCounters: DecoderCounters
                ) {
                    super.onVideoDisabled(eventTime, decoderCounters)
                    Log.d(TAG, "onVideoDisabled: ")
                }

                override fun onVideoEnabled(
                    eventTime: AnalyticsListener.EventTime,
                    decoderCounters: DecoderCounters
                ) {
                    super.onVideoEnabled(eventTime, decoderCounters)
                    Log.d(TAG, "onVideoEnabled: ")
                }

                override fun onVideoFrameProcessingOffset(
                    eventTime: AnalyticsListener.EventTime,
                    totalProcessingOffsetUs: Long,
                    frameCount: Int
                ) {
                    super.onVideoFrameProcessingOffset(
                        eventTime,
                        totalProcessingOffsetUs,
                        frameCount
                    )
                    Log.d(TAG, "onVideoFrameProcessingOffset: ")
                }

                override fun onVideoInputFormatChanged(
                    eventTime: AnalyticsListener.EventTime,
                    format: Format,
                    decoderReuseEvaluation: DecoderReuseEvaluation?
                ) {
                    super.onVideoInputFormatChanged(eventTime, format, decoderReuseEvaluation)
                    Log.d(TAG, "onVideoInputFormatChanged: ")
                }

                override fun onVideoSizeChanged(
                    eventTime: AnalyticsListener.EventTime,
                    videoSize: VideoSize
                ) {
                    super.onVideoSizeChanged(eventTime, videoSize)
                    Log.d(TAG, "onVideoSizeChanged: ")
                }

                override fun onVideoDecoderInitialized(
                    eventTime: AnalyticsListener.EventTime,
                    decoderName: String,
                    initializationDurationMs: Long
                ) {
                    super.onVideoDecoderInitialized(
                        eventTime,
                        decoderName,
                        initializationDurationMs
                    )
                    Log.d(TAG, "onVideoDecoderInitialized: $decoderName")
                }

                override fun onVideoSizeChanged(
                    eventTime: AnalyticsListener.EventTime,
                    width: Int,
                    height: Int,
                    unappliedRotationDegrees: Int,
                    pixelWidthHeightRatio: Float
                ) {
                    super.onVideoSizeChanged(
                        eventTime,
                        width,
                        height,
                        unappliedRotationDegrees,
                        pixelWidthHeightRatio
                    )
                    Log.d(TAG, "onVideoSizeChanged: ")
                }

                override fun onDroppedVideoFrames(
                    eventTime: AnalyticsListener.EventTime,
                    droppedFrames: Int,
                    elapsedMs: Long
                ) {
                    super.onDroppedVideoFrames(eventTime, droppedFrames, elapsedMs)
                    Log.d(TAG, "onDroppedVideoFrames: ")
                }
            })
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
    fun onComplete(asset: Asset)
}

internal fun interface MarkerListener {
    fun onMarkerCall(timeInMs: Long)
}