package com.tpstream.player.util

import android.content.Context
import com.tpstream.player.*
import com.tpstream.player.HttpMediaDrmCallback
import com.tpstream.player.MediaDrmCallback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class CustomHttpDrmMediaCallback(val context: Context, private val drmLicenseURL: String):MediaDrmCallback {
    private val httpMediaDrmCallback = HttpMediaDrmCallback("", DefaultHttpDataSourceFactory())

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrmProvisionRequest
    ): ByteArray {
        return httpMediaDrmCallback.executeProvisionRequest(uuid, request)
    }

    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrmKeyRequest): ByteArray {
        return try {
            val requestBody = request.data.toRequestBody("application/octet-stream".toMediaTypeOrNull())
            NetworkClient<ByteArray>(NetworkClient.getOkHttpClient(context)).fetchDRMKey(
                drmLicenseURL,
                requestBody
            ) ?: byteArrayOf()
        } catch (e: Exception) {
            e.printStackTrace()
            byteArrayOf()
        }
    }
}
