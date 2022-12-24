package com.tpstream.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import okhttp3.*
import java.util.Arrays

class VideoPlayerInterceptor(val context: Context, private val params: TpInitParams?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if (request.url.toString().contains("encryption_key")) {
            //if (params != null) {
           // EncryptionKeyRepository(context, request, params).putKeyInLocal()
                request = request.newBuilder()
                    .url("https://${params?.orgCode}.testpress.in/api/v2.5/encryption_key/${params?.videoId}/?access_token=${params?.accessToken}")
                    .build()
            //} else {
            //    return EncryptionKeyRepository(context,request,null).getResponseWithKey()
            //}
        }

        Log.d("TAG", "intercept: ${request.url}")

        return chain.proceed(request)
    }
}


