package com.tpstream.player

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class VideoPlayerInterceptor(val context: Context, private val params: TpInitParams?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if(request.url.toString().contains("encryption_key")) {
            request = request.newBuilder()
                .url( "https://${params?.orgCode}.testpress.in/api/v2.5/encryption_key/${params?.videoId}/?access_token=${params?.accessToken}")
                .build()
        }
        return chain.proceed(request)
    }
}