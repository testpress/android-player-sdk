package com.tpstream.player

import android.content.Context
import android.os.Handler
import android.os.Looper
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
}

internal class TpStreamPlayerImpl(val context: Context) : TpStreamPlayer {
    lateinit var params: TpInitParams
    lateinit var videoInfo: VideoInfo
    var offlineVideoInfo: OfflineVideoInfo? = null
    lateinit var exoPlayer: ExoPlayer

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

    internal fun load(url: String,startPosition: Long = 0) {
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
            Handler(Looper.getMainLooper()).post {
                load(offlineVideoInfo?.url!!, parameters.startAt * 1000L)
            }
            return
        }
        val url =
            "/api/v2.5/video_info/${parameters.videoId}/?access_token=${parameters.accessToken}"
        Network<VideoInfo>(parameters.orgCode).get(url, object : Network.TPResponse<VideoInfo> {
            override fun onSuccess(result: VideoInfo) {
                videoInfo = result
                Handler(Looper.getMainLooper()).post {
                    load(result.getPlaybackURL(), parameters.startAt * 1000L)
                }
            }

            override fun onFailure(exception: TPException) {
                onError(exception)
            }
        })
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

    fun getTrackSelectionParameters(): TrackSelectionParameters = exoPlayer.trackSelectionParameters

    fun setTrackSelectionParameters(parameters: TrackSelectionParameters){
        exoPlayer.trackSelectionParameters = parameters
    }

    fun getTrackSelector(): TrackSelector? = exoPlayer.trackSelector
}