package com.tpstream.player

import android.content.Context
import okhttp3.*

class VideoPlayerInterceptor(
    val context: Context,
    private val params: TpInitParams?
    , private val playbackUrl:String? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if (request.url.toString().contains("encryption_key")) {
            if (params != null) {
                request = request.newBuilder()
                    .url("https://${params.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
                    .build()
                if (playbackUrl != null){
                    EncryptionKeyRepository(context).put(params, playbackUrl)
                }
            } else {
                return EncryptionKeyRepository(context).get(request.url.toString())
            }
        }
        return chain.proceed(request)
    }
}


