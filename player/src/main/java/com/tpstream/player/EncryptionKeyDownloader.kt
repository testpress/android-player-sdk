package com.tpstream.player

import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request

internal class EncryptionKeyDownloader {

    fun getEncryptionKeyUrl(playbackUrl: String) :String {
        val mediaPlaylistUrl = getMediaPlayListUrl(playbackUrl)
        return getEncryptionKeyUrlUsingMediaPlaylistUrl(mediaPlaylistUrl)
    }

     private fun getMediaPlayListUrl(playbackUrl: String): String {
        val request = Request.Builder()
            .url(playbackUrl)
            .build()
        val response = OkHttpClient().newCall(request).execute()
        val playlist: HlsPlaylist =
            HlsPlaylistParser().parse(Uri.parse(playbackUrl), response.body?.byteStream()!!)
        val mediaPlaylist: HlsMultivariantPlaylist = playlist as HlsMultivariantPlaylist
        return mediaPlaylist.mediaPlaylistUrls[0].toString()
    }

     private fun getEncryptionKeyUrlUsingMediaPlaylistUrl(mediaPlaylistUrl: String): String {
        val request = Request.Builder()
            .url(mediaPlaylistUrl)
            .build()
        val response = OkHttpClient().newCall(request).execute()
        val playlist: HlsPlaylist = HlsPlaylistParser().parse(
            Uri.parse(mediaPlaylistUrl),
            response.body?.byteStream()!!
        )
        val mediaPlaylist: HlsMediaPlaylist = playlist as HlsMediaPlaylist
        val segments: List<HlsMediaPlaylistSegment> = mediaPlaylist.segments
        val segment: HlsMediaPlaylistSegment = segments[0]
        return segment.fullSegmentEncryptionKeyUri.toString()
    }

     fun getEncryptionKey(params: TpInitParams): String {
        val request = Request.Builder()
            .url("https://${TPStreamsSDK.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
            .header(TPStreamsSDK.getAuthenticationHeader().keys.first(),TPStreamsSDK.getAuthenticationHeader().values.first())
            .build()
        val response = OkHttpClient().newCall(request).execute()
        return response.body?.byteStream()?.readBytes()?.contentToString() ?: ""
    }
}