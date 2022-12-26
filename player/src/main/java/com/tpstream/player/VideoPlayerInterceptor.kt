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
            if (params != null) {
                request = request.newBuilder()
                    .url("https://${params.orgCode}.testpress.in/api/v2.5/encryption_key/${params.videoId}/?access_token=${params.accessToken}")
                    .build()
            } else {
                return createInternalResponse(request)
            }
        }
        return chain.proceed(request)
    }

    private fun createInternalResponse(request: Request): Response {

        val encryptionKeyRepository = EncryptionKeyRepository(context)

        val responseBody = if (encryptionKeyRepository.get(request.url.toString()) != null) {
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