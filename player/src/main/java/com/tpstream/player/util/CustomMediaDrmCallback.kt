package com.tpstream.player.util

import android.content.Context
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallback
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import java.util.*

class CustomHttpDrmMediaCallback(val context: Context,val params: TpInitParams):MediaDrmCallback {
    private val httpMediaDrmCallback = HttpMediaDrmCallback("", DefaultHttpDataSource.Factory())

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        return httpMediaDrmCallback.executeProvisionRequest(uuid, request)
    }

    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        val drmLicenseURL = TPStreamsSDK.constructDRMLicenseUrl(params.videoId, params.accessToken)
        return NetworkClient<ByteArray>(NetworkClient.getOkHttpClient(context)).fetchDRMKey(drmLicenseURL) ?: byteArrayOf()
    }
}
