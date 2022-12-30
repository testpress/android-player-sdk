package com.tpstream.player

import android.content.Context
import androidx.lifecycle.LiveData
import com.tpstream.player.models.OfflineVideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TpStreamDownloadManager(val context: Context) {

    private val offlineVideoInfoRepository = OfflineVideoInfoRepository(context)

    fun getAllDownloads(): LiveData<List<OfflineVideoInfo>?> {
        return offlineVideoInfoRepository.getAllDownloadsInLiveData()
    }

    fun pauseDownload(offlineVideoInfo: OfflineVideoInfo) {
        DownloadTask(context).pause(getDownloadUrl(offlineVideoInfo))
    }

    fun resumeDownload(offlineVideoInfo: OfflineVideoInfo) {
        DownloadTask(context).resume(getDownloadUrl(offlineVideoInfo))
    }

    fun cancelDownload(offlineVideoInfo: OfflineVideoInfo) {
        deleteDownload(offlineVideoInfo)
    }

    fun deleteDownload(offlineVideoInfo: OfflineVideoInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            DownloadTask(context).delete(getDownloadUrl(offlineVideoInfo))
            ImageSaver(context).delete(offlineVideoInfo.videoId)
            offlineVideoInfoRepository.delete(offlineVideoInfo)
        }
    }

    private fun getDownloadUrl(offlineVideoInfo: OfflineVideoInfo): String {
        return offlineVideoInfo.url
    }

    private fun startDownloads(paramsList: List<TpInitParams>,videoResolution:Int){





    }
}