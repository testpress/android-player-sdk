package com.tpstream.player

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class VideoPlayerInterceptor(private val context: Context, private val params: TpInitParams?) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if (request.url.toString().contains("encryption_key")) {
            if (isVideoBeingDownloaded(params)) {
                val response = buildResponseWithDownloadedEncryptionKey(request)
                EncryptionKeyRepository(context).delete(request.url.toString())
                return response
            } else {
                request = request.newBuilder()
                    .url("https://${params?.orgCode}.testpress.in/api/v2.5/encryption_key/${params?.videoId}/?access_token=${params?.accessToken}")
                    .build()
            }
        }
        return chain.proceed(request)
    }

    private fun isVideoBeingDownloaded(params: TpInitParams?): Boolean {
        /* Parameters are null when download is being downloaded.
         This is because VideoDownloadManager will be initialized only once and it will be
         used for all subsequent downloads. VideoInterceptor will be initialized in
         VideoDownloadManager, since it will be initialized only for first downloaded
         video `params` will be null */
        return params == null
    }

    private fun buildResponseWithDownloadedEncryptionKey(request: Request): Response {
        val encryptionKeyRepository = EncryptionKeyRepository(context)

        val responseBody = if (encryptionKeyRepository.hasEncryptionKey(request.url.toString())) {
            encryptionKeyRepository.get(request.url.toString())!!
                .toResponseBody("binary/octet-stream".toMediaType())
        } else {
            "".toResponseBody("binary/octet-stream".toMediaType())
        }

        return Response.Builder()
            .code(200)
            .request(request)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .body(responseBody)
            .build()
    }
}