package com.tpstream.player

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*

class VideoPlayerInterceptor(val context: Context, private val params: TpInitParams?) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if (request.url.toString().contains("encryption_key")) {
            if (params != null) {
                KeyCallback(context, request, params).put()
                request = request.newBuilder()
                    .url("https://${params.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
                    .build()
            } else {
                return KeyCallback(context,request,null).get()
            }
        }

        return chain.proceed(request)
    }
}

class KeyCallback(
    private val context: Context,
    private val request: Request,
    private val params: TpInitParams?
    ){

    fun put() {
        val request = Request.Builder()
            .url("https://${params?.orgCode}.testpress.in/api/v2.5/encryption_key/${params?.videoId}/?access_token=${params?.accessToken}")
            .build()
        val response = OkHttpClient().newCall(request).execute()

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreference = context.getSharedPreferences("VIDEO_ACCESS_KEY", Context.MODE_PRIVATE)
            with(sharedPreference.edit()){
                putString(this@KeyCallback.request.url.toString(),response.body?.byteStream()?.readBytes()!!.contentToString())
                apply()
                Log.d("TAG", "get: done")
            }
        }
    }

    private fun saveKey(): ByteArray? {
        val sharedPreference = context.getSharedPreferences("VIDEO_ACCESS_KEY", Context.MODE_PRIVATE)
        val encryptionKey = sharedPreference.getString(request.url.toString(), null)
        if (encryptionKey != null){
            val split = encryptionKey.substring(1,encryptionKey.length - 1).split(", ").toTypedArray()
            val array = ByteArray(split.size)
            for (i in split.indices) {
                array[i] = split[i].toByte()
            }
            return array
        }
        return null
    }

    fun get(): Response {
        val responseBody = ResponseBody.create(request.body?.contentType(), saveKey()!!)

        return Response.Builder()
            .code(200)
            .request(request)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .body(responseBody)
            .build()
    }

}


