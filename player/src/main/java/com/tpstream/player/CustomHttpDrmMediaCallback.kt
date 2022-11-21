package com.tpstream.player

import android.content.Context
import android.util.Log
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallback
import com.tpstream.player.models.DRMLicenseURL
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*


class CustomHttpDrmMediaCallback(context: Context,val orgCode: String, val videoUUID: String, val accessToken: String):MediaDrmCallback {
    private val httpMediaDrmCallback = HttpMediaDrmCallback("", VideoDownloadManager(context).getHttpDataSourceFactory())

    private fun fetchDRMLicenseURL(): String {
        val url = "/api/v2.5/drm_license/${videoUUID}/?access_token=${accessToken}"
        val body: RequestBody = ("{\"download\":true}").toRequestBody("application/json".toMediaTypeOrNull())
        return try {
            val result = Network<DRMLicenseURL>(orgCode).post(url, body)
            result?.licenseUrl ?: ""
        } catch (exception:TPException){
            ""
        }
    }

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        return httpMediaDrmCallback.executeProvisionRequest(uuid, request)
    }

    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        val licenseURL = fetchDRMLicenseURL()
        Log.d("TAG", "executeKeyRequest: $licenseURL")
        val updatedRequest = ExoMediaDrm.KeyRequest(request.data, licenseURL)
        return httpMediaDrmCallback.executeKeyRequest(uuid, updatedRequest)
    }
}