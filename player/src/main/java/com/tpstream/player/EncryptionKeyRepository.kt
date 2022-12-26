package com.tpstream.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*

class EncryptionKeyRepository(private val context: Context) {
    val accessToken = "143a0c71-567e-4ecd-b22d-06177228c25b"
    val videoId = "o7pOsacWaJt"
    val orgCode = "demoveranda"

    val sharedPreference = context.getSharedPreferences("VIDEO_ENCRYPTION_KEY", Context.MODE_PRIVATE)

    val url =
        "https://verandademo-cdn.testpress.in/institute/demoveranda/courses/video-content/videos/transcoded/90b25dc376b9435c8528d9cf789b7b7f/video.m3u8"


    fun put(url1: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val t1 = System.currentTimeMillis()
            Log.d("TAG", "put: $t1")
            val request = Request.Builder()
                .url(url)
                .build()
            val response = OkHttpClient().newCall(request).execute()


            val playlist: HlsPlaylist =
                HlsPlaylistParser().parse(Uri.parse(url), response.body?.byteStream()!!)

            val mediaPlaylist: HlsMultivariantPlaylist = playlist as HlsMultivariantPlaylist

            val request1 = Request.Builder()
                .url(mediaPlaylist.mediaPlaylistUrls[0].toString())
                .build()

            val response1 = OkHttpClient().newCall(request1).execute()

            val playlist1: HlsPlaylist = HlsPlaylistParser().parse(
                Uri.parse(mediaPlaylist.mediaPlaylistUrls[0].toString()),
                response1.body?.byteStream()!!
            )
            val mediaPlaylist1: HlsMediaPlaylist = playlist1 as HlsMediaPlaylist

            val segments: List<HlsMediaPlaylist.Segment> = mediaPlaylist1.segments

            val segment: HlsMediaPlaylist.Segment = segments[0]
            val keyUrl = segment.fullSegmentEncryptionKeyUri.toString()

            with(sharedPreference.edit()) {
                putString(videoId, keyUrl)
                apply()
                Log.d("TAG", "get: done")

            }
            val request3 = Request.Builder()
                .url("https://${orgCode}.testpress.in/api/v2.5/encryption_key/${videoId}/?access_token=${accessToken}")
                .build()

            val response3 = OkHttpClient().newCall(request3).execute()

            with(sharedPreference.edit()) {
                putString(keyUrl, response3.body?.byteStream()?.readBytes()?.contentToString())
                apply()
                Log.d("TAG", "get: done")

            }

            val fetchUrl = sharedPreference.getString(videoId,null)
            Log.d("TAG", "put: $fetchUrl")
            if (fetchUrl != null){
                val key = sharedPreference.getString(fetchUrl,null)
                Log.d("TAG", "put: $key")
            }
            val t2 = System.currentTimeMillis()
            Log.d("TAG", "put: ${(t2-t1)/1000L}")


//        CoroutineScope(Dispatchers.IO).launch {
//            val sharedPreference = context.getSharedPreferences("VIDEO_ACCESS_KEY", Context.MODE_PRIVATE)
//            with(sharedPreference.edit()){
//                putString(this@EncryptionKeyRepository.request?.url.toString(),response.body?.byteStream()?.readBytes()!!.contentToString())
//                apply()
//                Log.d("TAG", "get: done")
//            }
//        }
        }

//    private fun getLocalKey(): ByteArray? {
//        val sharedPreference =
//            context.getSharedPreferences("VIDEO_ACCESS_KEY", Context.MODE_PRIVATE)
//        val encryptionKey = sharedPreference.getString(request?.url.toString(), null)
//        if (encryptionKey != null) {
//            val split =
//                encryptionKey.substring(1, encryptionKey.length - 1).split(", ").toTypedArray()
//            val array = ByteArray(split.size)
//            for (i in split.indices) {
//                array[i] = split[i].toByte()
//            }
//            return array
//        }
//        return null
//    }
//
//    fun getResponseWithKey(): Response {
//        val responseBody = ResponseBody.create(request?.body?.contentType(), getLocalKey()!!)
//
//        return Response.Builder()
//            .code(200)
//            .request(request!!)
//            .message("OK")
//            .protocol(Protocol.HTTP_1_1)
//            .body(responseBody)
//            .build()
//    }


    }
}