package com.tpstream.player

import android.content.Context
import android.net.Uri
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

class EncryptionKeyRepository(context: Context) {

    private val sharedPreference = context.getSharedPreferences(
        "VIDEO_ENCRYPTION_KEY",
        Context.MODE_PRIVATE
    )
    private lateinit var encryptionKeyUrl: String
    private lateinit var mediaPlaylistUrl: String

    fun put(params: TpInitParams, playbackUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getMediaPlayListUrl(playbackUrl)
            saveEncryptionKeyUrl(params)
            saveEncryptionKey(params)
        }
    }

    private fun getMediaPlayListUrl(playbackUrl: String) {
        val request = Request.Builder()
            .url(playbackUrl)
            .build()
        val response = OkHttpClient().newCall(request).execute()

        val playlist: HlsPlaylist =
            HlsPlaylistParser().parse(Uri.parse(playbackUrl), response.body?.byteStream()!!)

        val mediaPlaylist: HlsMultivariantPlaylist = playlist as HlsMultivariantPlaylist

        mediaPlaylistUrl = mediaPlaylist.mediaPlaylistUrls[0].toString()
    }

    private fun saveEncryptionKeyUrl(params: TpInitParams) {
        val request = Request.Builder()
            .url(mediaPlaylistUrl)
            .build()

        val response = OkHttpClient().newCall(request).execute()

        val playlist: HlsPlaylist = HlsPlaylistParser().parse(
            Uri.parse(mediaPlaylistUrl),
            response.body?.byteStream()!!
        )

        val mediaPlaylist: HlsMediaPlaylist = playlist as HlsMediaPlaylist

        val segments: List<HlsMediaPlaylist.Segment> = mediaPlaylist.segments

        val segment: HlsMediaPlaylist.Segment = segments[0]

        encryptionKeyUrl = segment.fullSegmentEncryptionKeyUri.toString()

        with(sharedPreference.edit()) {
            putString(params.videoId, encryptionKeyUrl)
            apply()
        }
    }

    private fun saveEncryptionKey(params: TpInitParams) {
        val request = Request.Builder()
            .url("https://${params.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
            .build()

        val response = OkHttpClient().newCall(request).execute()

        with(sharedPreference.edit()) {
            putString(encryptionKeyUrl, response.body?.byteStream()?.readBytes()?.contentToString())
            apply()
        }
    }

    private fun getLocalKey(encryptionKeyUrl: String): ByteArray? {
        val encryptionKey = sharedPreference.getString(encryptionKeyUrl, null)
        if (encryptionKey != null) {
            val split =
                encryptionKey.substring(1, encryptionKey.length - 1).split(", ").toTypedArray()
            val array = ByteArray(split.size)
            for (i in split.indices) {
                array[i] = split[i].toByte()
            }
            return array
        }
        return null
    }

    fun get(encryptionKeyUrl: String): Response {
        val request = Request.Builder()
            .url(encryptionKeyUrl)
            .build()
        val responseBody = if (getLocalKey(encryptionKeyUrl) != null){
            getLocalKey(encryptionKeyUrl)!!.toResponseBody("binary/octet-stream".toMediaType())
        } else {
            "".toResponseBody("binary/octet-stream".toMediaType())
        }

        return Response.Builder()
            .code(200)
            .request(request)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .body(responseBody)
            .build()
    }
}