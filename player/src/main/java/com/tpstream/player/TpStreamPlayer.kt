package com.tpstream.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.TrackSelector
import com.google.common.collect.ImmutableList
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.VideoInfo
import com.tpstream.player.models.asVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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
}

internal class TpStreamPlayerImpl(val context: Context) : TpStreamPlayer {
    lateinit var params: TpInitParams
    lateinit var videoInfo: VideoInfo
    var offlineVideoInfo: OfflineVideoInfo? = null
    var _listener: TPStreamPlayerListener? = null
    lateinit var exoPlayer: ExoPlayer
    private val exoPlayerListener:ExoPlayerListenerWrapper = ExoPlayerListenerWrapper(this)

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

    internal fun playVideo(url: String,startPosition: Long = 0) {
        exoPlayer.setMediaSource(getMediaSourceFactory().createMediaSource(getMediaItem(url)))
        exoPlayer.seekTo(startPosition)
        exoPlayer.prepare()
    }

    private fun getMediaSourceFactory(): MediaSource.Factory {
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(VideoDownloadManager(context).build(params))
        if (offlineVideoInfo == null) {
            mediaSourceFactory.setDrmSessionManagerProvider {
                DefaultDrmSessionManager.Builder().build(
                    CustomHttpDrmMediaCallback(context, params)
                )
            }
        }
        return mediaSourceFactory
    }

    private fun getMediaItem(url: String): MediaItem {
        val downloadRequest: DownloadRequest? = VideoDownload.getDownloadRequest(url, context)
        if (DownloadTask(context).isDownloaded(url) && downloadRequest != null) {
            return buildDownloadedMediaItem(downloadRequest)
        }
        return buildMediaItem(url)
    }

    private fun buildMediaItem(url: String): MediaItem {
        return MediaItem.Builder()
            .setUri(url)
            .setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
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

    fun load(parameters: TpInitParams, onError:(exception: TPException) -> Unit) {
        params = parameters
        populateOfflineVideoInfo(parameters)
        if (checkIsVideoDownloaded()){
            videoInfo = offlineVideoInfo?.asVideoInfo()!!
            playVideoInUIThread(offlineVideoInfo?.url!!, parameters.startPositionInMilliSecs)
            return
        }
        fetchVideoInfoAndPlay(parameters, onError)
    }

    private fun fetchVideoInfoAndPlay(
        parameters: TpInitParams,
        onError: (exception: TPException) -> Unit
    ) {
        NetworkClass(parameters,"testpress").fetch(object :NetworkClass.VideoInfoCallback{
            override fun onSuccess(result: VideoInfo) {
                videoInfo = result
                playVideoInUIThread(result.getPlaybackURL(), parameters.startPositionInMilliSecs)
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

    private fun populateOfflineVideoInfo(parameters: TpInitParams){
        runBlocking(Dispatchers.IO) {
            offlineVideoInfo = OfflineVideoInfoRepository(context)
                .getOfflineVideoInfoByVideoId(parameters.videoId!!)
        }
    }

    private fun checkIsVideoDownloaded():Boolean{
        if (offlineVideoInfo != null && DownloadTask(context).isDownloaded(offlineVideoInfo?.url!!)){
            return true
        }
        return false
    }

    fun setPlayWhenReady(canPlay: Boolean) {
        exoPlayer.playWhenReady = canPlay
    }

    fun getPlayWhenReady() = exoPlayer.playWhenReady
    override fun getPlaybackState(): Int = exoPlayer.playbackState
    override fun getCurrentTime(): Long = exoPlayer.currentPosition
    override fun getBufferedTime(): Long = exoPlayer.bufferedPosition

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    override fun seekTo(seconds: Long) {
        exoPlayer.seekTo(seconds)
    }

    fun release() {
        exoPlayer.release()
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

    fun getTrackSelectionParameters(): TrackSelectionParameters = exoPlayer.trackSelectionParameters

    fun setTrackSelectionParameters(parameters: TrackSelectionParameters){
        exoPlayer.trackSelectionParameters = parameters
    }

    fun getTrackSelector(): TrackSelector? = exoPlayer.trackSelector
}