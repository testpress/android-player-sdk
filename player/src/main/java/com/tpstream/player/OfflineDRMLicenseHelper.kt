package com.tpstream.player

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.media3.exoplayer.dash.DashUtil
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.drm.OfflineLicenseHelper
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import com.tpstream.player.VideoDownload.getDownloadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object OfflineDRMLicenseHelper {
    @JvmStatic
    fun renewLicense(url:String, tpInitParams: TpInitParams, context: Context, callback: DRMLicenseFetchCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataSource = VideoDownloadManager(context).getHttpDataSourceFactory().createDataSource()
            val dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(url))
            val sessionManager = DefaultDrmSessionManager.Builder().build(CustomHttpDrmMediaCallback(tpInitParams.orgCode,tpInitParams.videoId!!,tpInitParams.accessToken!!))
            val drmInitData = DashUtil.loadFormatWithDrmInitData(dataSource, dashManifest.getPeriod(0))
            val keySetId = OfflineLicenseHelper(
                sessionManager,
                DrmSessionEventListener.EventDispatcher()
            ).downloadLicense(
                drmInitData!!
            )

            replaceKeysInExistingDownloadedVideo(url, context, keySetId)
            callback.onLicenseFetchSuccess(keySetId)
        }
    }

    private fun replaceKeysInExistingDownloadedVideo(
        url: String,
        context: Context,
        keySetId: ByteArray
    ) {
        val downloadRequest = getDownloadRequest(url, context)
        if (downloadRequest != null) {
            val newDownloadRequest: DownloadRequest =
                cloneDownloadRequestWithNewKeys(downloadRequest, keySetId)
            val download = VideoDownload.getDownload(url, context)
            val newDownload = cloneDownloadWithNewDownloadRequest(download!!, newDownloadRequest)
            val dowloadIndex = VideoDownloadManager(context).getDownloadIndex()
            dowloadIndex.putDownload(newDownload)
        }
    }

    private fun cloneDownloadRequestWithNewKeys(downloadRequest: DownloadRequest, keySetId: ByteArray): DownloadRequest {
        return DownloadRequest.Builder(
            downloadRequest.id,
            downloadRequest.uri
        )
            .setStreamKeys(downloadRequest.streamKeys)
            .setCustomCacheKey(downloadRequest.customCacheKey)
            .setKeySetId(keySetId)
            .setData(downloadRequest.data)
            .setMimeType(downloadRequest.mimeType)
            .build()
    }

    fun cloneDownloadWithNewDownloadRequest(download: Download, downloadRequest: DownloadRequest): Download {
        return Download(
            download.request.copyWithMergedRequest(downloadRequest),
            download.state,
            download.startTimeMs,
            download.updateTimeMs,
            download.contentLength,
            download.stopReason,
            download.failureReason
        )
    }

    fun fetchLicense(tpInitParams: TpInitParams, downloadHelper: DownloadHelper, callback: DRMLicenseFetchCallback) {
        Log.d("TAG", "fetchLicense1: 11111111111111111111")
        val sessionManager = DefaultDrmSessionManager.Builder()
            .build(CustomHttpDrmMediaCallback(tpInitParams.orgCode,tpInitParams.videoId!!,tpInitParams.accessToken!!))
        val offlineLicenseHelper = OfflineLicenseHelper(
            sessionManager, DrmSessionEventListener.EventDispatcher()
        )
        val s = VideoPlayerUtil.getAudioOrVideoInfoWithDrmInitData(downloadHelper)
        Log.d("TAG", "fetchLicense:----------------------${s?.drmInitData!=null} ")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val keySetId = offlineLicenseHelper.downloadLicense(
                    VideoPlayerUtil.getAudioOrVideoInfoWithDrmInitData(
                        downloadHelper
                    )!!)
                Log.d("TAG", "fetchLicense: 222222222222222")
                callback.onLicenseFetchSuccess(keySetId)
            } catch (e: DrmSession.DrmSessionException) {
                callback.onLicenseFetchFailure()
            } finally {
                offlineLicenseHelper.release()
            }
        }
    }
}

interface DRMLicenseFetchCallback {
    fun onLicenseFetchSuccess(keySetId: ByteArray)
    fun onLicenseFetchFailure()
}