package com.tpstream.player

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import androidx.media3.common.Format
import androidx.media3.exoplayer.dash.DashUtil
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.drm.OfflineLicenseHelper
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import com.tpstream.player.VideoDownload.getDownloadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal const val OFFLINE_DRM_LICENSE_PARAMS = "&drm_type=widevine&download=true"

internal object OfflineDRMLicenseHelper {

    fun renewLicense(
        url: String,
        tpInitParams: TpInitParams,
        context: Context,
        callback: DRMLicenseFetchCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val keySetId = downloadDRMKeySetId(context, tpInitParams, url,callback)
            replaceKeysInExistingDownloadedVideo(url, context, keySetId!!)
            callback.onLicenseFetchSuccess(keySetId)
        }
    }

    private fun downloadDRMKeySetId(
        context: Context,
        tpInitParams: TpInitParams,
        url: String,
        callback: DRMLicenseFetchCallback
    ): ByteArray? {
        val dataSource =
            VideoDownloadManager(context).getHttpDataSourceFactory().createDataSource()
        val dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(url))
        val drmInitData =
            DashUtil.loadFormatWithDrmInitData(dataSource, dashManifest.getPeriod(0))
        val drmLicenseURL = "${BuildConfig.DRM_LICENSE_URL.format(tpInitParams.videoId,tpInitParams.accessToken)}$OFFLINE_DRM_LICENSE_PARAMS"
        return try {
            OfflineLicenseHelper.newWidevineInstance(
                drmLicenseURL,
                VideoDownloadManager.invoke(context).getHttpDataSourceFactory(),
                DrmSessionEventListener.EventDispatcher()
            ).downloadLicense(drmInitData!!)
        } catch (e : Exception){
            callback.onLicenseFetchFailure()
            null
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
            val newDownload = cloneDownloadWithNewDownloadRequest(
                VideoDownload.getDownload(url, context)!!,
                newDownloadRequest
            )
            VideoDownloadManager(context).getDownloadIndex().putDownload(newDownload)
        }
    }

    private fun cloneDownloadRequestWithNewKeys(
        downloadRequest: DownloadRequest,
        keySetId: ByteArray
    ): DownloadRequest {
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

    private fun cloneDownloadWithNewDownloadRequest(
        download: Download,
        downloadRequest: DownloadRequest
    ): Download {
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

    fun fetchLicense(
        context: Context,
        tpInitParams: TpInitParams,
        downloadHelper: DownloadHelper,
        callback: DRMLicenseFetchCallback
    ) {
        val drmLicenseURL = "${BuildConfig.DRM_LICENSE_URL.format(tpInitParams.videoId,tpInitParams.accessToken)}$OFFLINE_DRM_LICENSE_PARAMS"
        val offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
            drmLicenseURL,
            VideoDownloadManager.invoke(context).getHttpDataSourceFactory(),
            DrmSessionEventListener.EventDispatcher()
        )
        val format = VideoPlayerUtil.getAudioOrVideoInfoWithDrmInitData(downloadHelper)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val keySetId = offlineLicenseHelper.downloadLicense(format!!)
                callback.onLicenseFetchSuccess(keySetId)
            } catch (e: DrmSession.DrmSessionException) {
                callback.onLicenseFetchFailure()
            } finally {
                offlineLicenseHelper.release()
            }
        }
    }
}

internal object VideoPlayerUtil {
    @JvmStatic
    fun getAudioOrVideoInfoWithDrmInitData(helper: DownloadHelper): Format? {
        for (periodIndex in 0 until helper.periodCount) {
            val mappedTrackInfo = helper.getMappedTrackInfo(periodIndex)
            for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
                for (trackGroupIndex in 0 until trackGroups.length) {
                    val trackGroup = trackGroups[trackGroupIndex]
                    for (formatIndex in 0 until trackGroup.length) {
                        val format = trackGroup.getFormat(formatIndex)
                        if (format.drmInitData != null) {
                            return format
                        }
                    }
                }
            }
        }
        return null
    }
}

internal interface DRMLicenseFetchCallback {
    fun onLicenseFetchSuccess(keySetId: ByteArray)
    fun onLicenseFetchFailure()
}

internal object InternetConnectivityChecker {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val infos = connectivity.allNetworkInfo
            if (infos != null) {
                for (info in infos) {
                    if (info.state == NetworkInfo.State.CONNECTED) return true
                }
            }
        }
        return false
    }
}