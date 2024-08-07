package com.tpstream.player.util

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.tpstream.player.TPException
import okhttp3.*
import java.io.IOException
import java.net.URL

internal class NetworkClient<T : Any>(val klass: Class<T>, private val client: OkHttpClient) {
    companion object {
        inline operator fun <reified T : Any>invoke(client: OkHttpClient) = NetworkClient(T::class.java, client = client)

        fun makeHeadRequest(url: String): Int {
            val request = Request.Builder().head().url(url).build()
            OkHttpClient().newCall(request).execute().use { response ->
                return response.code
            }
        }

        fun getOkHttpClient(context: Context): OkHttpClient {
            val okHttpClient = OkHttpClient()
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                val (socketFactory, trustManager) = TrustFactory.getTrustFactoryManager(context)
                okHttpClient.newBuilder().sslSocketFactory(socketFactory, trustManager).build()
            } else {
                okHttpClient
            }
        }
    }

    private val gson = Gson()

    fun get(url: String, callback: TPResponse<T>) {
        val request = Request.Builder().url(URL(url)).build()
        return makeRequest(request, callback)
    }

    fun fetchDRMKey(url: String, requestBody: RequestBody): ByteArray? {
        val request = Request.Builder().url(URL(url)).post(requestBody).build()
        val response = client.newCall(request).execute()
        return response.body?.bytes()
    }

    private fun makeRequest(request: Request, callback: TPResponse<T>) {
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

    interface TPResponse<T> {
        fun onSuccess(result: T)
        fun onFailure(exception: TPException)
    }
}

