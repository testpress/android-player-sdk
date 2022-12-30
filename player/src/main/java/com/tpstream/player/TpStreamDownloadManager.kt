package com.tpstream.player

import android.content.Context
import androidx.lifecycle.LiveData
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.VideoInfo
import kotlinx.coroutines.*
import okhttp3.internal.wait

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

    fun startDownloads(paramsList: List<TpInitParams>,videoResolution:Int){
        CoroutineScope(Dispatchers.IO).launch {
            val downloadedList = offlineVideoInfoRepository.getAllDownloads()
            val paramsList1 = if (downloadedList != null){
                (paramsList.filter { big -> !downloadedList.map { it.videoId }.contains ( big.videoId!! )  }).toMutableList()
            } else {
                paramsList.toMutableList()
            }
            for (params in paramsList1){
                val videoDownloadRequestCreationHandler = VideoDownloadRequestCreationHandler(context,null,params,videoResolution).init()
                videoDownloadRequestCreationHandler.setOnDownloadRequestCreation { downloadRequest, videoInfo ->
                    DownloadTask(context).start(downloadRequest)
                    saveOfflineVideoInfo(params,videoInfo)
                }
            }
        }
    }

    private fun saveOfflineVideoInfo(params: TpInitParams,videoInfo: VideoInfo){
        CoroutineScope(Dispatchers.IO).launch {
            val offlineVideoInfo = videoInfo.asOfflineVideoInfo()
            offlineVideoInfo.videoId = params.videoId!!
            ImageSaver(context).save(offlineVideoInfo.thumbnail,offlineVideoInfo.videoId)
            OfflineVideoInfoRepository(context).insert(offlineVideoInfo)
        }
    }
}