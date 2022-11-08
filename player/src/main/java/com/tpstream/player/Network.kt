package com.tpstream.player

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.net.URL

class Network<T : Any>(val klass: Class<T>) {
    companion object {
        inline operator fun <reified T : Any>invoke() = Network(T::class.java)
    }

    private val BASE_URL = "https://ae2f-49-204-138-95.in.ngrok.io"
    private var client: OkHttpClient = OkHttpClient();
    private val gson = Gson()

    private fun makeRequest() {

    }

    fun get() {

    }

    fun post(url: String, body: RequestBody, callback: TPResponse<T>? = null): T? {
        val request = Request.Builder().url(URL("$BASE_URL$url")).post(body).build()

        if (callback != null) {
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    val result = gson.fromJson(response.body?.charStream(), klass)
                    Log.d("TAG", "onResponse: $result")
                    callback.onSuccess(result)
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure()
                }
            })
        } else {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                return gson.fromJson(response.body?.charStream(), klass)
            }
        }

        return null
    }

    interface TPResponse<T> {
        fun onSuccess(result: T)
        fun onFailure()
    }
}