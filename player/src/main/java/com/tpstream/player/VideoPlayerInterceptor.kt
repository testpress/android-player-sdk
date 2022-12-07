package com.tpstream.player

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response


class VideoPlayerInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if(request.url.toString().contains("encryption_key")) {
            request = request.newBuilder().addHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6MSwidXNlcl9pZCI6MSwiaW5zdGl0dXRlIjoxNTg1LCJpZCI6MSwiZXhwIjoxNjcwNDI3NzYwLCJlbWFpbCI6InN1cHBvcnRAdmVyYW5kYXJhY2UuY29tIn0.2ChALE2LK7YeNfe_1sqCAEwZp2XTEW6VUab8Jb6dTGw").build()
        }
        return chain.proceed(request)
    }
}