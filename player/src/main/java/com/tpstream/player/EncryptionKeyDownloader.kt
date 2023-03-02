package com.tpstream.player

import android.net.Uri
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal class EncryptionKeyDownloader {

    fun getEncryptionKeyUrl(playbackUrl: String): String {
        val response = getResponse(playbackUrl)
        val mediaPlaylistUrl = getMediaPlayListUrl(playbackUrl, response)
        val mediaPlaylistUrlResponse = getResponse(mediaPlaylistUrl)
        return getEncryptionKeyUrlUsingMediaPlaylistUrl(mediaPlaylistUrl, mediaPlaylistUrlResponse)
    }

    fun getMediaPlayListUrl(playbackUrl: String,response: Response): String {
        val playlist: HlsPlaylist =
            HlsPlaylistParser().parse(Uri.parse(playbackUrl), response.body?.byteStream()!!)
        val mediaPlaylist: HlsMultivariantPlaylist = playlist as HlsMultivariantPlaylist
        return mediaPlaylist.mediaPlaylistUrls[0].toString()
    }

    fun getResponse(url:String):Response{
        val request = Request.Builder()
            .url(url)
            .build()
        return OkHttpClient().newCall(request).execute()
    }

    fun getEncryptionKeyUrlUsingMediaPlaylistUrl(mediaPlaylistUrl: String,response: Response): String {
        val playlist: HlsPlaylist = HlsPlaylistParser().parse(
            Uri.parse(mediaPlaylistUrl),
            response.body?.byteStream()!!
        )
        val mediaPlaylist: HlsMediaPlaylist = playlist as HlsMediaPlaylist
        val segments: List<HlsMediaPlaylist.Segment> = mediaPlaylist.segments
        val segment: HlsMediaPlaylist.Segment = segments[0]
        return segment.fullSegmentEncryptionKeyUri.toString()
    }

     fun getEncryptionKey(url : String): String {
        val response = getResponse(url)
        return response.body?.byteStream()?.readBytes()?.contentToString() ?: ""
    }
}