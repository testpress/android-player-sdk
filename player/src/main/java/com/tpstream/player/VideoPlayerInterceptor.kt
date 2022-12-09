package com.tpstream.player

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class VideoPlayerInterceptor(val context: Context) : Interceptor {

    private lateinit var params: TpInitParams

    override fun intercept(chain: Interceptor.Chain): Response {
        params = Params.params

        var request = chain.request()

        if(request.url.toString().contains("encryption_key")) {
            request = request.newBuilder()
                .url( "https://${params.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
                .build()
        }
        return chain.proceed(request)
    }
}