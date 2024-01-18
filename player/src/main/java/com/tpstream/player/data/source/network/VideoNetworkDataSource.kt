package com.tpstream.player.data.source.network

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.tpstream.player.TPException
import com.tpstream.player.TPStreamsSDK
import okhttp3.*
import java.io.IOException
import java.net.URL

internal class VideoNetworkDataSource<T : Any>(val klass: Class<T>) {
    companion object {
        inline operator fun <reified T : Any>invoke() = VideoNetworkDataSource(T::class.java)
    }

    private var client: OkHttpClient = OkHttpClient();
    private val gson = Gson()

    fun get(url: String, callback: TPResponse<T>? = null): T? {
        val request = Request.Builder()
            .url(URL(url))
            .header(TPStreamsSDK.authenticationHeader.keys.first(),TPStreamsSDK.authenticationHeader.values.first())
            .build()
        return makeRequest(callback, request)
    }

    fun post(url: String, body: RequestBody, callback: TPResponse<T>? = null): T? {
        val request = Request.Builder().url(URL(url)).post(body).build()
        return makeRequest(callback, request)
    }

    private fun makeRequest(
        callback: TPResponse<T>?,
        request: Request
    ): T? {
        return if (callback != null) {
            makeAsyncRequest(request, callback)
            null
        } else {
            makeSyncRequest(request)
        }
    }

    private fun makeAsyncRequest(request: Request, callback: TPResponse<T>) {
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    // If the users provide invalid subdomain we are getting success response.
                    // But we can't able to parse the response.
                    // This try catch block will handle.
                    // Valid response can able to parse for Invalid throw TPException
                    try {
                        val result = gson.fromJson(response.body?.charStream(), klass)
                        callback.onSuccess(result)
                    } catch (e: JsonParseException){
                        callback.onFailure(TPException.httpError(response))
                    }
                } else{
                    callback.onFailure(TPException.httpError(response))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(TPException.networkError(e))
            }
        })
    }

    private fun makeSyncRequest(request: Request): T? {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            return gson.fromJson(response.body?.charStream(), klass)
        }
        throw TPException.httpError(response)
    }

    interface TPResponse<T> {
        fun onSuccess(result: T)
        fun onFailure(exception: TPException)
    }
}