package com.tpstream.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.TrackSelector
import com.google.common.collect.ImmutableList
import com.tpstream.player.data.Video
import com.tpstream.player.data.VideoRepository
import com.tpstream.player.data.source.network.VideoNetworkDataSource
import com.tpstream.player.offline.DownloadTask
import com.tpstream.player.offline.VideoDownload
import com.tpstream.player.offline.VideoDownloadManager

interface TPStreamPlayerListener {
    fun onTracksChanged(tracks: Tracks) {}
    fun onMetadata(metadata: Metadata) {}
    fun onIsPlayingChanged(playing: Boolean) {}
    fun onIsLoadingChanged(loading: Boolean) {}
    fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {}
    fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {}
    fun onEvents(player: TpStreamPlayer?, events: Player.Events) {}
    fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {}
    fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {}
    fun onVideoSizeChanged(videoSize: VideoSize) {}
    fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {}
    fun onPlayerErrorChanged(error: PlaybackException?) {}
    fun onTimelineChanged(timeline: Timeline, reason: Int) {}
    fun onPlaybackStateChanged(playbackState: Int) {}
    fun onPlayerError(error: PlaybackException) {}
}

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
    fun getCurrentTrackGroups(): ImmutableList<Tracks.Group>
    fun getDuration(): Long
    fun setListener(listener: TPStreamPlayerListener?)
    fun play()
    fun pause()
}

internal class TpStreamPlayerImpl(val context: Context) : TpStreamPlayer {
    lateinit var params: TpInitParams
    var video: Video? = null
    var _listener: TPStreamPlayerListener? = null
    lateinit var exoPlayer: ExoPlayer
    private val exoPlayerListener:ExoPlayerListenerWrapper = ExoPlayerListenerWrapper(this)
    private var videoRepository: VideoRepository = VideoRepository(context)
    private var tpStreamPlayerImplCallBack : TpStreamPlayerImplCallBack? = null

    init {
        initializeExoplayer()
    }

    private fun initializeExoplayer() {
        exoPlayer = ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
            }
    }

    fun load(parameters: TpInitParams, onError:(exception: TPException) -> Unit) {
        params = parameters
        videoRepository.getVideo(parameters, object : VideoNetworkDataSource.TPResponse<Video> {
            override fun onSuccess(result: Video) {
                video = result
                playVideoInUIThread(result.url, parameters.startPositionInMilliSecs)
            }

            override fun onFailure(exception: TPException) {
                onError(exception)
            }
        })
    }

    private fun playVideoInUIThread(url: String,startPosition: Long = 0) {
        Handler(Looper.getMainLooper()).post {
            playVideo(url, startPosition)
        }
    }

    internal fun playVideo(url: String,startPosition: Long = 0) {
        exoPlayer.setMediaSource(getMediaSourceFactory().createMediaSource(getMediaItem(url)))
        exoPlayer.seekTo(startPosition)
        exoPlayer.prepare()
    }

    private fun getMediaSourceFactory(): MediaSource.Factory {
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
        val drmLicenseURL = BuildConfig.DRM_LICENSE_URL.format(params.orgCode,params.videoId,params.accessToken)
        return MediaItem.Builder()
            .setUri(url)
            .setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .setLicenseUri(drmLicenseURL)
                    .build()
            ).build()
    }

    private fun buildDownloadedMediaItem(downloadRequest: DownloadRequest):MediaItem{
        val builder = MediaItem.Builder()
        builder
            .setMediaId(downloadRequest.id)
            .setUri(downloadRequest.uri)
            .setCustomCacheKey(downloadRequest.customCacheKey)
            .setMimeType(downloadRequest.mimeType)
            .setStreamKeys(downloadRequest.streamKeys)
            .setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setKeySetId(downloadRequest.keySetId)
                    .build()
            )
        return builder.build()
    }
    fun getPlayWhenReady() = exoPlayer.playWhenReady

    fun setPlayWhenReady(canPlay: Boolean) {
        exoPlayer.playWhenReady = canPlay
    }

    fun getTrackSelectionParameters(): TrackSelectionParameters = exoPlayer.trackSelectionParameters

    fun setTrackSelectionParameters(parameters: TrackSelectionParameters){
        exoPlayer.trackSelectionParameters = parameters
    }

    fun getTrackSelector(): TrackSelector? = exoPlayer.trackSelector

    fun release() {
        exoPlayer.release()
    }

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

    override fun getCurrentTrackGroups(): ImmutableList<Tracks.Group> = exoPlayer.currentTracks.groups

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

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    fun setTpStreamPlayerImplCallBack(tpStreamPlayerImplCallBack: TpStreamPlayerImplCallBack){
        this.tpStreamPlayerImplCallBack = tpStreamPlayerImplCallBack
    }
}

internal interface TpStreamPlayerImplCallBack {

    fun updateDownloadButtons(parameters: TpInitParams)
}