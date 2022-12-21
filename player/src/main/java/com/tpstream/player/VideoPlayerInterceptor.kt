package com.tpstream.player

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody

class VideoPlayerInterceptor(val context: Context, private val params: TpInitParams?) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        Log.d("TAG", "intercept: ${request.url}")

        if (request.url.toString().contains("encryption_key")) {
            if (params != null) {
                KeyCallback(context, request.url.toString(), params).get()
                request = request.newBuilder()
                    .url("https://${params.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
                    .build()
            } else {
                val sharedPreference =
                    context.applicationContext.getSharedPreferences("player", Context.MODE_PRIVATE)
                val pausedAt = sharedPreference.getString("Key", "Empty")
                return Response.Builder().body(pausedAt?.toResponseBody()).build()
            }
        }

        return chain.proceed(request)
    }
}

class KeyCallback(
    private val context: Context,
    private val url: String,
    private val params: TpInitParams?
    ){

    fun get() {
        val request = Request.Builder()
            .url("https://${params?.orgCode}.testpress.in/api/v2.5/encryption_key/${params?.videoId}/?access_token=${params?.accessToken}")
            .build()
        val response = OkHttpClient().newCall(request).execute()

        //Log.d("TAG", "get: ${response.body?.byteStream()?.readBytes()!!.contentToString()}")

//        val inputStream = response.body?.byteStream()
//        Log.d("TAG", "get1: ${inputStream?.readBytes().contentToString()}")
//        val byteArray = inputStream?.readBytes()
//        Log.d("TAG", "get: ${byteArray?.size}")
//        val saveThis: String = Arrays.toString(byteArray).toString()
//
//        Log.d("TAG", "get: $saveThis")

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreference = context.applicationContext.getSharedPreferences("player", Context.MODE_PRIVATE)
            with(sharedPreference.edit()){
                putString("Key",response.body?.byteStream()?.readBytes()!!.contentToString())
                apply()
                Log.d("TAG", "get: done")
            }
        }
    }

}


