package com.tpstream.player

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response


class VideoPlayerInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if(request.url.toString().contains("encryption_key")) {
            request = request.newBuilder().addHeader("Authorization", "token").build()
        }
        return chain.proceed(request)
    }
}