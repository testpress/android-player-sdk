package com.tpstream.player

import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.net.URL

class Network<T : Any>(val klass: Class<T>, val subdomain: String) {
    companion object {
        inline operator fun <reified T : Any>invoke(subdomain: String) = Network(T::class.java, subdomain)
    }

    private val BASE_URL = "https://8a7f-49-204-138-95.in.ngrok.io"
//    private val BASE_URL = "https://${subdomain}.testpress.in"
    private var client: OkHttpClient = OkHttpClient();
    private val gson = Gson()

    private fun makeAsyncRequest(request: Request, callback: TPResponse<T>) {
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val result = gson.fromJson(response.body?.charStream(), klass)
                callback.onSuccess(result)
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure()
            }
        })
    }

    private fun makeRequest(request: Request): T? {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            return gson.fromJson(response.body?.charStream(), klass)
        }
        return null
    }

    fun get(url: String, callback: TPResponse<T>? = null): T? {
        val request = Request.Builder().url(URL("$BASE_URL$url")).build()

        if (callback != null) {
            makeAsyncRequest(request, callback)
        } else {
            return makeRequest(request)
        }
        return null
    }

    fun post(url: String, body: RequestBody, callback: TPResponse<T>? = null): T? {
        val request = Request.Builder().url(URL("$BASE_URL$url")).post(body).build()

        if (callback != null) {
            makeAsyncRequest(request, callback)
        } else {
            return makeRequest(request)
        }
        return null
    }

    interface TPResponse<T> {
        fun onSuccess(result: T)
        fun onFailure()
    }
}