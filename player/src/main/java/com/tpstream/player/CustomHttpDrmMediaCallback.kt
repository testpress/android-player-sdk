package com.tpstream.player

import android.util.Log
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallback
import com.google.gson.Gson
import com.tpstream.player.models.DRMLicenseURL
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.EMPTY_REQUEST
import java.net.URL
import java.util.*


class CustomHttpDrmMediaCallback(val orgCode: String, val videoUUID: String, val accessToken: String):MediaDrmCallback {
    private val httpMediaDrmCallback = HttpMediaDrmCallback("", DefaultHttpDataSource.Factory())

    fun fetchDRMLicenseURL(): String {
        val url = "/api/v2.5/drm_license/${videoUUID}/?access_token=${accessToken}"
        val result = Network<DRMLicenseURL>(orgCode).post(url, EMPTY_REQUEST)
        return result?.licenseUrl ?: ""
    }

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        return httpMediaDrmCallback.executeProvisionRequest(uuid, request)
    }

    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        val licenseURL = fetchDRMLicenseURL()
        val updatedRequest = ExoMediaDrm.KeyRequest(request.data, licenseURL)
        return httpMediaDrmCallback.executeKeyRequest(uuid, updatedRequest)
    }
}