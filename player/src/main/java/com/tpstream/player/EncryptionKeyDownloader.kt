package com.tpstream.player

import android.net.Uri
import android.util.Log
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import okhttp3.OkHttpClient
import okhttp3.Request

class EncryptionKeyDownloader {
    private lateinit var mediaPlaylistUrl: String
    lateinit var encryptionKeyUrl: String
    lateinit var encryptionKey: String


    fun put(params: TpInitParams, playbackUrl: String) {
        getMediaPlayListUrl(playbackUrl)
        getEncryptionKeyUrl()
        getEncryptionKey(params)
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

    private fun getEncryptionKeyUrl() {
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
    }

    private fun getEncryptionKey(params: TpInitParams) {
        val request = Request.Builder()
            .url("https://${params.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
            .build()

        val response = OkHttpClient().newCall(request).execute()

        encryptionKey = response.body?.byteStream()?.readBytes()?.contentToString() ?: ""
    }
}