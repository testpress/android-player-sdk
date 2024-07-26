package com.tpstream.player.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import com.tpstream.player.*
import com.tpstream.player.offline.VideoDownload.getDownloadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Long.min

internal object OfflineDRMLicenseHelper {

    fun renewLicense(
        url: String,
        tpInitParams: TpInitParams,
        context: Context,
        callback: DRMLicenseFetchCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataSource =
                VideoDownloadManager(context).getHttpDataSourceFactory().createDataSource()
            val dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(url))
            val drmInitData =
                DashUtil.loadFormatWithDrmInitData(dataSource, dashManifest.getPeriod(0))
            fetchLicense(context,tpInitParams,drmInitData!!,callback)
        }
    }

    fun replaceKeysInExistingDownloadedVideo(
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
        return DownloadRequestBuilder(
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

    fun isOfflineLicenseExpired(
        tpInitParams: TpInitParams,
        context: Context,
        downloadRequest: DownloadRequest
    ): Boolean {
        val drmLicenseURL = TPStreamsSDK.constructOfflineDRMLicenseUrl(
            tpInitParams.videoId,
            tpInitParams.accessToken,
            tpInitParams.rentalDurationSeconds
        )
        val offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
            drmLicenseURL,
            VideoDownloadManager.invoke(context).getHttpDataSourceFactory(),
            DrmSessionEventListenerEventDispatcher()
        )
        val licenseDurationRemainingSec =
            offlineLicenseHelper.getLicenseDurationRemainingSec(downloadRequest.keySetId!!)
        offlineLicenseHelper.release()
        return min(licenseDurationRemainingSec.first, licenseDurationRemainingSec.second) <= 60

    }

    fun fetchLicense(
        context: Context,
        tpInitParams: TpInitParams,
        format: Format,
        callback: DRMLicenseFetchCallback
    ) {
        val drmLicenseURL = TPStreamsSDK.constructOfflineDRMLicenseUrl(tpInitParams.videoId,tpInitParams.accessToken, tpInitParams.rentalDurationSeconds)
        val offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
            drmLicenseURL,
            VideoDownloadManager.invoke(context).getHttpDataSourceFactory(),
            DrmSessionEventListenerEventDispatcher()
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val keySetId = offlineLicenseHelper.downloadLicense(format)
                callback.onLicenseFetchSuccess(keySetId)
            } catch (e: DrmSessionException) {
                callback.onLicenseFetchFailure(e)
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
    fun onLicenseFetchFailure(error: DrmSessionException)
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