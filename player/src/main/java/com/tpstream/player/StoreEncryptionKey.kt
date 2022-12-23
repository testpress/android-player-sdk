package com.tpstream.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.StreamKey
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.hls.offline.HlsDownloader
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.offline.Downloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.*

class StoreEncryptionKey(
    private val context: Context,
    private val request: Request?,
    private val params: TpInitParams?
) :Downloader.ProgressListener{
    fun putKeyInLocal() {
        val request = Request.Builder()
            .url("https://${params?.orgCode}.testpress.in/api/v2.5/encryption_key/${params?.videoId}/?access_token=${params?.accessToken}")
            .build()
        val response = OkHttpClient().newCall(request).execute()

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreference = context.getSharedPreferences("VIDEO_ACCESS_KEY", Context.MODE_PRIVATE)
            with(sharedPreference.edit()){
                putString(this@StoreEncryptionKey.request?.url.toString(),response.body?.byteStream()?.readBytes()!!.contentToString())
                apply()
                Log.d("TAG", "get: done")
            }
        }
    }

    private fun getLocalKey(): ByteArray? {
        val sharedPreference = context.getSharedPreferences("VIDEO_ACCESS_KEY", Context.MODE_PRIVATE)
        val encryptionKey = sharedPreference.getString(request?.url.toString(), null)
        if (encryptionKey != null){
            val split = encryptionKey.substring(1,encryptionKey.length - 1).split(", ").toTypedArray()
            val array = ByteArray(split.size)
            for (i in split.indices) {
                array[i] = split[i].toByte()
            }
            return array
        }
        return null
    }

    fun getResponseWithKey(): Response {
        val responseBody = ResponseBody.create(request?.body?.contentType(), getLocalKey()!!)

        return Response.Builder()
            .code(200)
            .request(request!!)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .body(responseBody)
            .build()
    }


    fun downloadMultipleVideo(){

  val url = "https://verandademo-cdn.testpress.in/institute/demoveranda/courses/video-content/videos/transcoded/c3785c76604048f2afd0b382e68f7dd2/video.m3u8"

        val hlsDownload = HlsDownloader(MediaItem.Builder()
            .setUri(url)
            .setStreamKeys(
                Collections.singletonList(StreamKey(HlsMultivariantPlaylist.GROUP_INDEX_VARIANT, 0)))
            .build()
        ,VideoDownloadManager(context).build())

        hlsDownload.download(this)

//        HlsMediaSource.Factory(VideoDownloadManager(context).build()).createMediaSource(
//        MediaItem.Builder()
//        .setUri(playlistUri)
//            .setStreamKeys(
//                Collections.singletonList(StreamKey(HlsMultivariantPlaylist.GROUP_INDEX_VARIANT, 0)))
//            .build())
    }

    override fun onProgress(contentLength: Long, bytesDownloaded: Long, percentDownloaded: Float) {
        Log.d("TAG", "onProgress: $percentDownloaded")
    }

    fun play(){

    }


}