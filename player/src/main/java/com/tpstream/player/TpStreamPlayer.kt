package com.tpstream.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.Format
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import com.google.common.collect.ImmutableList
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.VideoInfo
import com.tpstream.player.models.asOfflineVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

public interface TpStreamPlayer {
    abstract val params: TpInitParams
    abstract val videoInfo: VideoInfo?
    fun load(parameters: TpInitParams)
    fun setPlayWhenReady(canPlay: Boolean)
    fun getPlayWhenReady(): Boolean
    fun getPlaybackState(): Int
    fun getCurrentTime(): Long
    fun getBufferedTime(): Long
    fun setPlaybackSpeed(speed: Float)
    fun seekTo(seconds: Long)
    fun release()
    fun getVideoFormat(): Format?
    fun getCurrentTrackGroups(): ImmutableList<Tracks.Group>
    fun getDuration(): Long
}

class TpStreamPlayerImpl(val player: ExoPlayer, val context: Context) : TpStreamPlayer {
    override lateinit var params: TpInitParams
    override lateinit var videoInfo: VideoInfo
    var offlineVideoInfo: OfflineVideoInfo? = null

    internal fun load(url: String,startPosition: Long = 0) {
        player.setMediaSource(getMediaSourceFactory().createMediaSource(getMediaItem(url)))
        player.seekTo(startPosition)
        player.prepare()
    }

    private fun getMediaSourceFactory(): MediaSource.Factory {
        var downloadTask: DownloadTask? = null
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(VideoDownloadManager(context).build())
        if (offlineVideoInfo != null) {
            downloadTask = DownloadTask(offlineVideoInfo?.dashUrl!!, context)
        }
        if (downloadTask == null || !downloadTask.isDownloaded()) {
            mediaSourceFactory.setDrmSessionManagerProvider {
                DefaultDrmSessionManager.Builder().build(
                    CustomHttpDrmMediaCallback(context, params)
                )
            }
        }
        return mediaSourceFactory
    }

    internal fun getMediaItem(url: String): MediaItem {
        var mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setMultiSession(true)
                    .build()
            )
            .build()
        return setDownloadedMediaItem(url,mediaItem)
    }

    private fun setDownloadedMediaItem(url:String,mediaItem: MediaItem):MediaItem{
        val downloadTask = DownloadTask(url, context)
        val downloadRequest: DownloadRequest? = VideoDownload.getDownloadRequest(url, context)
        if (downloadTask.isDownloaded() && downloadRequest != null) {
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
        return mediaItem
    }

    private fun checkVideoIsDownloaded(parameters: TpInitParams):Boolean{
        runBlocking(Dispatchers.IO) {
            offlineVideoInfo = try {
                TPStreamsDatabase.invoke(context).offlineVideoInfoDao().getOfflineVideoInfoByVideoId(parameters.videoId!!)
            } catch (exception: Exception){
                null
            }
        }
        if(offlineVideoInfo != null){
            return true
        }
        return false
    }

    override fun load(parameters: TpInitParams) {
        params = parameters
        if (checkVideoIsDownloaded(parameters)){
            Handler(Looper.getMainLooper()).post {
                if (offlineVideoInfo != null) {
                    load(offlineVideoInfo?.dashUrl!!)
                }
            }
            return
        }
        val url =
            "/api/v2.5/video_info/${parameters.videoId}/?access_token=${parameters.accessToken}"
        Network<VideoInfo>(parameters.orgCode).get(url, object : Network.TPResponse<VideoInfo> {
            override fun onSuccess(result: VideoInfo) {
                videoInfo = result
                result.dashUrl?.let {
                    Handler(Looper.getMainLooper()).post {
                        load(it)
                    }
                }
            }

            override fun onFailure(exception: TPException) {
                Log.d("TAG", "onFailure: ")
            }
        })
    }

    override fun setPlayWhenReady(canPlay: Boolean) {
        player.playWhenReady = canPlay
    }

    override fun getPlayWhenReady() = player.playWhenReady
    override fun getPlaybackState(): Int = player.playbackState
    override fun getCurrentTime(): Long = player.currentPosition
    override fun getBufferedTime(): Long = player.bufferedPosition

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    override fun seekTo(seconds: Long) {
        player.seekTo(seconds)
    }

    override fun release() {
        player.release()
    }

    override fun getVideoFormat(): Format? = player.videoFormat
    override fun getCurrentTrackGroups(): ImmutableList<Tracks.Group> = player.currentTracks.groups
    override fun getDuration(): Long = player.duration
}